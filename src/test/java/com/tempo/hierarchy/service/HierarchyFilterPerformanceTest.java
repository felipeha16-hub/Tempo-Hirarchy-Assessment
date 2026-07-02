package com.tempo.hierarchy.service;

import com.tempo.hierarchy.domain.model.ArrayBasedHierarchy;
import com.tempo.hierarchy.domain.model.Hierarchy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


 // Performance tests for HierarchyFilter to validate O(n) algorithmic complexity.

@DisplayName("HierarchyFilter Performance Tests")
class HierarchyFilterPerformanceTest {

    @Nested
    @DisplayName("Performance Validation")
    class PerformanceTests {

        @Test
        @DisplayName("Given large flat forest, when filtering, then completes in O(n) time")
        void givenLargeFlatForest_whenFiltering_thenCompletesInLinearTime() {
            // Given: Large flat forest with 1000 root nodes
            // This validates the worst case for depth backtracking:
            // Every node causes the while loop to reset the stack to size 0
            // Expected: O(n) = 1,000 operations (1 push per node)
            int size = 1000;
            int[] nodeIds = new int[size];
            int[] depths = new int[size];
            for (int i = 0; i < size; i++) {
                nodeIds[i] = i + 1;
                depths[i] = 0;  // All roots
            }
            Hierarchy largeFlat = new ArrayBasedHierarchy(nodeIds, depths);

            // When: Filter (should be fast - O(n))
            long startTime = System.nanoTime();
            Hierarchy filtered = HierarchyFilter.filter(largeFlat, nodeId -> nodeId % 2 == 0);
            long durationNs = System.nanoTime() - startTime;
            long durationMs = durationNs / 1_000_000;

            // Then: Should complete quickly (< 10ms for 1000 nodes)
            assertEquals(500, filtered.size(), "Half the nodes should pass the predicate");
            assertTrue(durationNs < 10_000_000L,
                    String.format("Expected < 10ms, but took %dms for 1000 flat nodes", durationMs));

            System.out.printf("✓ Flat forest (1000 roots): %dms%n", durationMs);
        }

        @Test
        @DisplayName("Given deep linear hierarchy, when filtering, then completes in O(n) time")
        void givenDeepLinearHierarchy_whenFiltering_thenCompletesInLinearTime() {
            // Given: Deep linear chain (1 -> 2 -> 3 -> ... -> 1000)
            // This validates that deep hierarchies don't cause:
            // 1. Stack overflow (we use iteration, not recursion)
            // 2. Performance degradation due to deep stacks
            int size = 1000;
            int[] nodeIds = new int[size];
            int[] depths = new int[size];
            for (int i = 0; i < size; i++) {
                nodeIds[i] = i + 1;
                depths[i] = i;  // Linearly increasing depth
            }
            Hierarchy deepChain = new ArrayBasedHierarchy(nodeIds, depths);

            // When: Filter
            long startTime = System.nanoTime();
            Hierarchy filtered = HierarchyFilter.filter(deepChain, nodeId -> nodeId % 2 != 0);
            long durationNs = System.nanoTime() - startTime;
            long durationMs = durationNs / 1_000_000;

            // Then: Should complete quickly and not cause stack overflow
            assertTrue(filtered.size() > 0, "Some nodes should pass the predicate");
            assertTrue(durationNs < 10_000_000L,
                    String.format("Expected < 10ms, but took %dms for 1000-level deep chain", durationMs));

            System.out.printf("✓ Deep chain (1000 levels): %dms%n", durationMs);
        }

        @Test
        @DisplayName("Given mixed forest with wide and deep trees, when filtering, then maintains O(n) performance")
        void givenMixedForest_whenFiltering_thenMaintainsLinearPerformance() {
            // Given: Complex forest with varied structure (~300 nodes total)
            // Tree 1: Wide (1 root + 100 children at depth 1)
            // Tree 2: Deep (chain of 100 nodes, depths 0..99)
            // Tree 3: Another wide tree (1 root + 100 children)
            // This represents a realistic production scenario with mixed depths
            int wideChildren = 100;
            int deepChain = 100;
            int totalSize = 1 + wideChildren + 1 + deepChain + 1 + wideChildren;

            int[] nodeIds = new int[totalSize];
            int[] depths = new int[totalSize];
            int index = 0;

            // Tree 1: Root with 100 children
            nodeIds[index] = index + 1;
            depths[index] = 0;
            index++;
            for (int i = 0; i < wideChildren; i++) {
                nodeIds[index] = index + 1;
                depths[index] = 1;
                index++;
            }

            // Tree 2: Deep chain
            for (int i = 0; i < deepChain + 1; i++) {
                nodeIds[index] = index + 1;
                depths[index] = i;
                index++;
            }

            // Tree 3: Another wide tree
            nodeIds[index] = index + 1;
            depths[index] = 0;
            index++;
            for (int i = 0; i < wideChildren; i++) {
                nodeIds[index] = index + 1;
                depths[index] = 1;
                index++;
            }

            Hierarchy complexForest = new ArrayBasedHierarchy(nodeIds, depths);

            // When: Filter
            long startTime = System.nanoTime();
            Hierarchy filtered = HierarchyFilter.filter(complexForest, nodeId -> nodeId % 3 != 0);
            long durationNs = System.nanoTime() - startTime;
            long durationMs = durationNs / 1_000_000;

            // Then: Should complete in O(n) time
            assertTrue(filtered.size() > 0, "Some nodes should pass the predicate");
            assertTrue(durationNs < 20_000_000L,
                    String.format("Expected < 20ms, but took %dms for ~300-node mixed forest", durationMs));

            System.out.printf("✓ Mixed forest (wide + deep, ~300 nodes): %dms%n", durationMs);
        }
    }
}