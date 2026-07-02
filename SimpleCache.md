## Code Review

You are reviewing the following code submitted as part of a task to implement an item cache in a highly concurrent application. The anticipated load includes: thousands of reads per second, hundreds of writes per second, tens of concurrent threads.
Your objective is to identify and explain the issues in the implementation that must be addressed before deploying the code to production. Please provide a clear explanation of each issue and its potential impact on production behaviour.

```java
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache<K, V> {
    private final ConcurrentHashMap<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final long ttlMs = 60000; // 1 minute

    public static class CacheEntry<V> {
        private final V value;
        private final long timestamp;

        public CacheEntry(V value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public V getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public void put(K key, V value) {
        cache.put(key, new CacheEntry<>(value, System.currentTimeMillis()));
    }

    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null) {
            if (System.currentTimeMillis() - entry.getTimestamp() < ttlMs) {
                return entry.getValue();
            }
        }
        return null;
    }

    public int size() {
        return cache.size();
    }
}
```

---

## Code Review Findings

### 1. Unbounded Memory Growth Due to Passive-Only Expiration (Memory Leak)

The `get()` method only *checks* if an entry has expired via timestamp comparison, but never *removes* it from the underlying `ConcurrentHashMap`. Expired entries remain physically in memory indefinitely unless the exact same key is written again via `put()`.

**Impact:**
Under sustained write load (hundreds of writes/sec) with unique or rarely-reused keys, heap usage grows monotonically over time, eventually leading to `OutOfMemoryError` in production.

**💡 Suggested Fix:**
Actively evict on read using the atomic 2-argument `cache.remove(key, entry)` when expiration is detected, complemented by a background `ScheduledExecutorService` that periodically sweeps and removes all expired entries (covers keys that are never queried again).

---

### 2. No Maximum Size Limit or Eviction Policy

There is no cap on the number of distinct entries (no `maxSize`, no LRU/LFU eviction). Combined with Issue #1, the cache has no upper bound on memory consumption.

**Impact:**
A burst of unique keys — even well within the 60-second TTL window — can spike memory usage uncontrollably, creating a potential Denial-of-Service (DoS) vector if keys are influenced by user input.

**💡 Suggested Fix:**
Delegate size-bounding and eviction to a production-grade library such as Caffeine (`Caffeine.newBuilder().maximumSize(...).expireAfterWrite(...)`), rather than reimplementing eviction logic manually.

---

### 3. Cache Stampede / "Thundering Herd" Risk

`SimpleCache` does not expose an atomic "get-or-compute" operation (e.g., `computeIfAbsent`). Consumers are forced to implement the classic racy pattern: `get()` → if null/expired, compute expensive value → `put()`. Under concurrent load, when a hot key expires, many threads will simultaneously miss the cache and hit the expensive backend (database/API) at once.

**Impact:**
With "thousands of reads/sec," an expiring hot key can trigger a stampede of simultaneous backend calls, overwhelming downstream systems and causing latency spikes or cascading failures.

**💡 Suggested Fix:**
Expose a `getOrCompute(key, loader)` method built on `ConcurrentHashMap.compute(key, ...)`, which is atomic per key — guaranteeing only one thread executes the expensive loader per key while concurrent callers wait for that single result.

---

### 4. Use of Wall-Clock Time Instead of Monotonic Time

TTL validation relies on `System.currentTimeMillis()`, which reflects wall-clock time and can jump forward or backward due to NTP synchronization or manual clock adjustments.

**Impact:**
Clock adjustments in production (common in cloud/VM environments) can cause entries to expire prematurely or live longer than intended, producing inconsistent caching behavior.

**💡 Suggested Fix:**
Store `System.nanoTime()` at insertion time instead of `currentTimeMillis()`, and compare elapsed duration via `System.nanoTime() - insertedAtNanos`, which is guaranteed monotonic and immune to wall-clock adjustments.

---

### 5. Misleading `size()` Metric

`size()` delegates directly to `ConcurrentHashMap.size()`, which counts all entries regardless of expiration status.

**Impact:**
Monitoring dashboards and capacity-planning decisions based on this metric will be inaccurate, as the reported size includes "zombie" entries that are logically expired but not yet evicted.

**💡 Suggested Fix:**
This is automatically resolved once Issue #1's active eviction is implemented — with expired entries proactively removed, the underlying map (and therefore `size()`) will always reflect only live entries.

---

### 6. No Manual Invalidation API

There is no `remove()` or `invalidate()` method exposed, only implicit overwrite via `put()`.

**Impact:**
When the underlying data source changes before the TTL expires, there is no way to proactively evict the stale entry, forcing stale reads until the TTL naturally lapses.

**💡 Suggested Fix:**
Expose a public `invalidate(K key)` method that delegates to `cache.remove(key)`, allowing callers to explicitly evict a key when the source data changes.
