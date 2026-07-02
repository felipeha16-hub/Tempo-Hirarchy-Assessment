# Hierarchy Filter - Take-Home Assessment

---

## 📋 Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Architecture & Design Decisions](#architecture--design-decisions)
4. [Algorithm Explanation](#algorithm-explanation)
5. [Performance & Complexity Analysis](#performance--complexity-analysis)
6. [Testing Strategy](#testing-strategy)
7. [Trade-offs & Future Improvements](#trade-offs--future-improvements)
8. [Project Structure](#project-structure)

---

# Hierarchy Filter - Technical Assessment

[![Build Status](https://img.shields.io/badge/Build-Success-success?style=flat-square&logo=github)](https://github.com/felipeha16-hub/Tempo-Hirarchy-Assessment)
[![JaCoCo Coverage](https://img.shields.io/badge/JaCoCo%20Coverage-98%25-green?style=flat-square&logo=jacoco)](https://github.com/felipeha16-hub/Tempo-Hirarchy-Assessment)
[![Java Version](https://img.shields.io/badge/Java-21-blue?style=flat-square&logo=java)](https://oracle.com/java)
[![JUnit 5](https://img.shields.io/badge/JUnit%205-40%20Passed-blueviolet?style=flat-square&logo=junit5)](https://junit.org)

## 🎯 Overview

This project implements a **hierarchy filtering algorithm** that operates on a forest data structure (an ordered collection of ordered trees) represented as parallel arrays of node IDs and depths in DFS  (Depth-First Search) traversal order.

### Core Problem

Given a hierarchy and a predicate function, filter the hierarchy such that:
- A node is included **if and only if** its node ID passes the predicate **AND** all of its ancestors also pass the predicate.

### Key Requirements

- **Correctness**: Maintain valid tree structure (no orphaned nodes)
- **Efficiency**: O(n) time complexity where n = number of nodes
- **Clarity**: Clean, maintainable, production-ready code
- **Testing**: Comprehensive test coverage including edge cases

---

## 🚀 Getting Started

### Prerequisites

- **Java 21** (configured via Gradle Toolchain)
- **Gradle** (wrapper included, no installation needed)

### Running the Tests

Execute all unit tests (37 functional + 3 performance):

```bash
# On Windows (PowerShell)
.\gradlew.bat test

# On macOS/Linux
./gradlew test
```

### Generating Code Coverage Report

Run tests with JaCoCo coverage analysis:

```bash
.\gradlew.bat clean test jacocoTestReport
```

View the interactive HTML report at:
```
build/reports/jacoco/test/html/index.html
```

**Current Coverage:** >98% overall (lines, branches, and methods)

### Building the Project

```bash
.\gradlew.bat build
```

---

## 🏗️ Architecture & Design Decisions

### Clean Architecture Principles

The project follows **Clean Architecture** with clear separation of concerns:

```
src/main/java/com/tempo/hierarchy/
├── domain/model/          # Business entities (Hierarchy, ArrayBasedHierarchy)
└── service/               # Business logic (HierarchyFilter)
```

**Key principles applied:**
- **Dependency Rule**: `service` depends on `domain`, never the reverse
- **Single Responsibility**: Each class has one well-defined purpose
- **Open/Closed**: `Hierarchy` interface allows multiple implementations
- **Liskov Substitution**: `ArrayBasedHierarchy` can replace `Hierarchy` anywhere
- **Dependency Inversion**: Code depends on abstractions (`Hierarchy` interface), not concrete classes

### Why This Structure?

Although the challenge doesn't require a REST API, this architecture demonstrates:
- **Extensibility**: Easy to add REST controllers later without changing core logic
- **Testability**: Service layer is pure functions, easy to test in isolation
- **Maintainability**: Domain logic is decoupled from infrastructure concerns

---

## 💡 Algorithm Explanation

### The Core Challenge: Tracking Ancestor Validity

The key insight is that we need to **efficiently track whether the current "path" from root to the current node is valid** as we iterate through the flattened hierarchy.

### Data Structure Decision: `Deque<Boolean>` (Stack)

**Initial Consideration:** Use a `boolean[]` indexed by depth to track validity at each level.

**Why we chose `ArrayDeque` instead:**

| Aspect | `boolean[]` (array) | `ArrayDeque<Boolean>` (stack) |
|--------|---------------------|-------------------------------|
| **Semantic clarity** | Requires manual index management | `push()`/`pop()`/`peek()` are self-documenting |
| **Complexity bug risk** | Easy to introduce O(n²) by clearing too much | Stack size naturally matches depth |
| **Memory efficiency** | Fixed size (must pre-allocate max depth) | Grows dynamically only as needed |
| **Code maintainability** | Index arithmetic is error-prone | Stack operations are obvious |

**Bug Found in Alternative Approach:**

An initial implementation using `boolean[]` contained a critical performance bug:
```
// ❌ BUGGY VERSION (O(n²) in worst case)
for (int d = currentDepth + 1; d < validPath.length; d++) {
    validPath[d] = false;  // Clears entire rest of array every iteration!
}
```


With a flat forest of 1000 roots, this becomes:
- **Buggy approach:** 1000 iterations × ~1000 clears = **1,000,000 operations** (O(n²))
- **Stack approach:** 1000 push + 1000 pop = **2,000 operations** (O(n))

The `ArrayDeque` implementation **guarantees O(n)** because each element is pushed **at most once** and popped **at most once** across the entire algorithm execution (amortized analysis).

### How the Algorithm Works: The "Tower of Boxes" Analogy

Imagine the `ancestorValidityStack` as a **tower of boxes**, where:
- Each box represents a level of the tree we've already processed
- The **height of the tower** = `stack.size()`
- Each box has a label: ✅ (valid) or ❌ (invalid)

**Invariant we maintain:** Before processing a node at depth `D`, the tower must have **exactly `D` boxes**.

#### Visual Example

Processing this hierarchy:
```
nodeIds: [1, 2, 3, 4, 5]
depths:  [0, 1, 2, 3, 1]

Tree structure:
1 (depth 0)
├── 2 (depth 1)
│   └── 3 (depth 2)
│       └── 4 (depth 3)
└── 5 (depth 1)
```

With predicate: `nodeId % 3 != 0` (eliminate multiples of 3)

**Step-by-step execution:**

```
i=0: nodeId=1, depth=0
  Tower BEFORE: [] (empty)
  while: ¿0 > 0? NO → no pops needed
  peek: (empty tower) → parentIsValid = true
  Predicate: 1 % 3 != 0 → TRUE
  isValid = true && true = TRUE ✅
  push(TRUE) → Tower: [1✅]
  → RESULT: Add (1, depth=0)

i=1: nodeId=2, depth=1
  Tower BEFORE: [1✅]
  while: ¿1 > 1? NO
  peek: 1✅ → parentIsValid = true
  Predicate: 2 % 3 != 0 → TRUE
  isValid = true && true = TRUE ✅
  push(TRUE) → Tower: [1✅, 2✅]
  → RESULT: Add (2, depth=1)

i=2: nodeId=3, depth=2
  Tower BEFORE: [1✅, 2✅]
  while: ¿2 > 2? NO
  peek: 2✅ → parentIsValid = true
  Predicate: 3 % 3 != 0 → FALSE ❌
  isValid = true && false = FALSE ❌
  push(FALSE) → Tower: [1✅, 2✅, 3❌]
  → RESULT: NOT added (node 3 fails)

i=3: nodeId=4, depth=3
  Tower BEFORE: [1✅, 2✅, 3❌]
  while: ¿3 > 3? NO
  peek: 3❌ → parentIsValid = false
  Predicate: 4 % 3 != 0 → TRUE (individually would pass)
  isValid = false && true = FALSE ❌
  push(FALSE) → Tower: [1✅, 2✅, 3❌, 4❌]
  → RESULT: NOT added (inherits parent's failure)

i=4: nodeId=5, depth=1 ⭐ (key step: sibling after failed branch)
  Tower BEFORE: [1✅, 2✅, 3❌, 4❌] (height=4, but depth=1)
  
  while: ¿4 > 1? YES → pop() removes 4❌
         ¿3 > 1? YES → pop() removes 3❌
         ¿2 > 1? YES → pop() removes 2✅
         ¿1 > 1? NO → STOP
  
  Tower AFTER while: [1✅]  ← Cleaned the failed branch
  
  peek: 1✅ → parentIsValid = true
  Predicate: 5 % 3 != 0 → TRUE
  isValid = true && true = TRUE ✅
  push(TRUE) → Tower: [1✅, 5✅]
  → RESULT: Add (5, depth=1)
```

**Final output:** `nodeIds=[1, 2, 5], depths=[0, 1, 1]` ✅

**Key insight at i=4:** The `while` loop **cleans up the failed branch** (nodes 2, 3, 4) so that node 5 is evaluated independently against its true parent (node 1), not affected by its "cousin" node 2's subtree failure.

### Why This Guarantees O(n)

Each of the `n` nodes causes:
- **Exactly 1 push** (line: `ancestorValidityStack.push(isValid)`)
- **At most 1 pop** (amortized across all iterations of all `while` loops)

**Total operations:** ≤ 2n → **O(n)** time complexity

**Space complexity:** O(h) where h = maximum depth of the hierarchy

---

## ⚡ Performance & Complexity Analysis

### Algorithmic Complexity

| Operation | Time Complexity | Space Complexity |
|-----------|----------------|------------------|
| **Filter entire hierarchy** | **O(n)** | O(h) |
| **Per-node processing** | O(1) amortized | - |
| **Stack push/pop** | O(1) | - |

Where:
- `n` = total number of nodes in the hierarchy
- `h` = maximum depth (height) of the deepest tree

### Empirical Validation (Performance Tests)

We validate the O(n) claim with three benchmark scenarios:

#### 1. Flat Forest (1000 roots)
- **Structure:** All nodes at depth 0 (worst case for stack resets)
- **Result:** < 10ms
- **Validates:** No O(n²) degradation from excessive stack cleanup

#### 2. Deep Linear Chain (1000 levels)
- **Structure:** Single branch with depth 0 → 999
- **Result:** < 10ms, no `StackOverflowError`
- **Validates:** Iterative approach (not recursive), efficient stack usage

#### 3. Mixed Complex Forest (~300 nodes)
- **Structure:** Wide trees (1 root + 100 children) + deep chain (100 levels)
- **Result:** < 20ms
- **Validates:** Real-world performance with varied topology

All benchmarks run on standard hardware (modern laptop). See `HierarchyFilterPerformanceTest.java` for full implementation.

---

## 🧪 Testing Strategy

### Test Coverage Summary

| Test Suite | Tests | Purpose                                                   |
|------------|-------|-----------------------------------------------------------|
| `HierarchyFilterTest` | 12 | Functional correctness, edge cases, null safety           |
| `ArrayBasedHierarchyTest` | 25 | Domain invariants, immutability, equals/hashCode contract |
| `HierarchyFilterPerformanceTest` | 3 | Algorithmic complexity validation                         |
| **TOTAL** | **40** | **>98% code coverage**                                    |

### Key Test Categories

#### 1. Null Safety & Validation
- Verify `NullPointerException` for null inputs
- Validate constructor invariants (first depth = 0, valid transitions, etc.)

#### 2. Edge Cases
- Empty hierarchy
- Single node
- All nodes pass / all nodes fail
- Deep chains (no stack overflow)

#### 3. Core Algorithm Logic
- Challenge example (11 nodes, multiples of 3)
- Forest with multiple roots
- Ancestor failure propagation
- Sibling independence after failed branch

#### 4. Immutability
- Defensive copy in constructor
- Filter does not mutate original hierarchy

#### 5. Performance
- O(n) scaling validation with large inputs
- No performance degradation in worst-case topologies

### Running Specific Test Classes

```bash
# Run only functional tests
.\gradlew.bat test --tests HierarchyFilterTest

# Run only domain tests
.\gradlew.bat test --tests ArrayBasedHierarchyTest

# Run only performance tests
.\gradlew.bat test --tests HierarchyFilterPerformanceTest
```

---

## 🔄 Trade-offs & Future Improvements

### Current Design Decisions

#### ✅ What We Optimized For
- **Correctness:** Rigorous validation of hierarchy invariants
- **Clarity:** Self-documenting code with clear naming
- **Testability:** Pure functions, no hidden state
- **Performance:** Proven O(n) with empirical tests

#### ⚠️ Known Trade-offs

1. **No Input Validation for Predicate Safety**
    - The `IntPredicate` could throw exceptions; we don't catch them
    - **Justification:** In production, predicate logic is owned by the caller
    - **Future:** Could add a `SafeHierarchyFilter` wrapper with try/catch

2. **No Depth Limit Enforcement**
    - Very deep hierarchies (>10,000 levels) could stress memory
    - **Justification:** Real-world hierarchies rarely exceed 100 levels
    - **Future:** Add configurable max depth validation

3. **Autoboxing Overhead in `Deque<Boolean>`**
    - Each `push(isValid)` boxes a primitive `boolean` into `Boolean` object
    - **Justification:** Negligible compared to algorithm benefits; modern JVMs optimize this
    - **Future:** Could use `BitSet` if profiling shows this as a bottleneck

### Future Enhancements (If This Were a Production API)

#### 1. RESTful API Layer
```java
@RestController
@RequestMapping("/api/v1/hierarchies")
public class HierarchyController {
    
    @PostMapping("/filter")
    public ResponseEntity<HierarchyDTO> filterHierarchy(
        @RequestBody FilterRequest request
    ) {
        // Delegate to HierarchyFilter.filter()
    }
}
```

**Benefits:**
- Expose filtering as a microservice
- Add authentication/authorization
- Enable distributed caching (Redis)

#### 2. OpenAPI/Swagger Documentation
- Auto-generate interactive API docs


#### 3. CI/CD Pipeline
- **CircleCI/GitHub Actions:** Automated test runs on every push
- **Codecov/Coveralls:** Public coverage badges for transparency
- **Deployment:** Heroku, AWS , or Kubernetes

#### 4. Observability
- We will use structured logs to track critical flows and exceptions, and expose filter size and performance metrics for visualization in Grafana dashboards.

#### 5. Advanced Features
- **Caching:** Memoize frequently-used predicates (e.g., "filter out deleted nodes")
- **Pagination:** For very large result sets (>10,000 nodes)
- **Async Processing:** `CompletableFuture<Hierarchy>` for non-blocking calls
- **Batch Operations:** Filter multiple hierarchies in parallel

---

## 📁 Project Structure

```
Tempo-Hirarchy-Assessment/
├── src/
│   ├── main/java/com/tempo/hierarchy/
│   │   ├── domain/model/
│   │   │   ├── Hierarchy.java              # Interface: core abstraction
│   │   │   └── ArrayBasedHierarchy.java    # Immutable implementation
│   │   └── service/
│   │       └── HierarchyFilter.java        # Filtering algorithm (O(n))
│   └── test/java/com/tempo/hierarchy/
│       ├── domain/model/
│       │   └── ArrayBasedHierarchyTest.java  # 25 tests (invariants, equals/hashCode)
│       └── service/
│           ├── HierarchyFilterTest.java      # 12 tests (correctness, edge cases)
│           └── HierarchyFilterPerformanceTest.java  # 3 tests (O(n) validation)
├── build.gradle                            # Gradle config (Java 21, JaCoCo)
├── gradlew, gradlew.bat                   # Gradle wrapper
└── README.md                               # This file
```

### Key Files

| File | Purpose |
|------|---------|
| `HierarchyFilter.java` | Core algorithm |
| `ArrayBasedHierarchy.java` | Domain model + validations |
| `HierarchyFilterTest.java` | Functional tests |
| `ArrayBasedHierarchyTest.java` | Domain tests |
| `HierarchyFilterPerformanceTest.java` | Performance benchmarks |

---

## 📝 Assumptions & Clarifications

### Assumptions Made

1. **Node IDs are unique:** The hierarchy does not validate uniqueness, assuming the caller ensures this.
2. **Depths are valid:** The constructor validates depth invariants, so invalid hierarchies are rejected early.
3. **Thread safety not required:** This implementation is stateless and thread-safe for reads, but concurrent modifications are not considered.
4. **Predicate is deterministic:** We assume `nodeIdPredicate.test(id)` returns the same value for the same `id` across calls.

### Design Decisions Explained

**Q: Why return a new `Hierarchy` instead of modifying in-place?**  
**A:** Immutability ensures thread safety, avoids side effects, and allows safe reuse of the original hierarchy.

**Q: Why not use recursion for DFS?**  
**A:** Iterative approach avoids `StackOverflowError` for deep hierarchies (>1000 levels) and performs better in benchmarks.

**Q: Why no caching of filter results?**  
**A:** Caching requires state management and invalidation logic. As a pure function, the caller can implement caching externally if needed.

---

## 🏆 Conclusion

This implementation demonstrates:
- ✅ **Correctness:** Passes all test cases including the challenge example
- ✅ **Efficiency:** Proven O(n) time complexity with empirical validation
- ✅ **Clarity:** Clean code following SOLID principles
- ✅ **Production-readiness:** Comprehensive testing (>98% coverage), robust error handling

The solution is ready for integration into a production system and easily extensible to support future requirements (REST API, caching, distributed processing, etc.).

---
