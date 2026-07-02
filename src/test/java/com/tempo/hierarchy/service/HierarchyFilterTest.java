package com.tempo.hierarchy.service;


import com.tempo.hierarchy.domain.model.ArrayBasedHierarchy;
import com.tempo.hierarchy.domain.model.Hierarchy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class HierarchyFilterTest {

    @Test
    @DisplayName("filter() It throws a NullPointerException when the hierarchy is null.")
    void filter_whenHierarchyIsNull_thenThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> HierarchyFilter.filter(null, nodeId -> true));
    }


    @Test
    @DisplayName("filter() It throws a NullPointerException when the predicate is null.")
    void filter_whenPredicateIsNull_thenThrowsNullPointerException() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{1}, new int[]{0});
        assertThrows(NullPointerException.class,
                () -> HierarchyFilter.filter(hierarchy, null));
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("filter() It returns an empty hierarchy when the input is empty")
    void filter_whenHierarchyIsEmpty_thenReturnsEmptyHierarchy() {
        Hierarchy empty = new ArrayBasedHierarchy(new int[0], new int[0]);

        Hierarchy result = HierarchyFilter.filter(empty, nodeId -> true);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("filter() It returns the complete hierarchy when all nodes pass the predicate")
    void filter_whenAllNodesPassPredicate_thenReturnsIdenticalHierarchy() {
        Hierarchy original = new ArrayBasedHierarchy(new int[]{1, 2, 3}, new int[]{0, 1, 2});

        Hierarchy result = HierarchyFilter.filter(original, nodeId -> true);

        assertEquals(original, result);
    }

    @Test
    @DisplayName("filter() It returns an empty hierarchy when no nodes pass the predicate")
    void filter_whenNoNodesPassPredicate_thenReturnsEmptyHierarchy() {
        Hierarchy original = new ArrayBasedHierarchy(new int[]{1, 2, 3}, new int[]{0, 1, 2});

        Hierarchy result = HierarchyFilter.filter(original, nodeId -> false);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("filter() It retains a single root node when it passes the predicate")
    void filter_whenSingleRootNodePassesPredicate_thenReturnsSingleNodeHierarchy() {
        Hierarchy original = new ArrayBasedHierarchy(new int[]{42}, new int[]{0});

        Hierarchy result = HierarchyFilter.filter(original, nodeId -> nodeId == 42);

        assertEquals(1, result.size());
        assertEquals(42, result.nodeId(0));
        assertEquals(0, result.depth(0));
    }

    @Test
    @DisplayName("filter() It returns an empty hierarchy when the single root node fails the predicate")
    void filter_whenSingleRootNodeFailsPredicate_thenReturnsEmptyHierarchy() {
        Hierarchy original = new ArrayBasedHierarchy(new int[]{42}, new int[]{0});

        Hierarchy result = HierarchyFilter.filter(original, nodeId -> nodeId != 42);

        assertTrue(result.isEmpty());
    }

    // ======================= PRINCIPAL CASE ==========================

    @Test
    @DisplayName("filter() It excludes multiples of 3 and all their descendants")
    void filter_whenExcludingMultiplesOfThree_thenRemovesInvalidNodesAndDescendants() {
        Hierarchy unfiltered = new ArrayBasedHierarchy(
                new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11},
                new int[]{0, 1, 2, 3, 1, 0, 1, 0, 1, 1, 2}
        );

        Hierarchy result = HierarchyFilter.filter(unfiltered, nodeId -> nodeId % 3 != 0);

        Hierarchy expected = new ArrayBasedHierarchy(
                new int[]{1, 2, 5, 8, 10, 11},
                new int[]{0, 1, 1, 0, 1, 2}
        );

        assertEquals(expected, result);
        assertEquals(expected.formatString(), result.formatString());
    }

    // ==================== STRUCTURE CASES (forest, branches, invalidity inheritance) ====================

    @Test
    @DisplayName("filter() It filters each tree in a forest independently")
    void filter_whenForestHasMultipleRoots_thenFiltersEachTreeIndependently() {
        // 10(root,fails) -> 20(child, excluded due to parent)
        // 30(root,passes)  -> 40(child, passes)
        Hierarchy original = new ArrayBasedHierarchy(
                new int[]{10, 20, 30, 40},
                new int[]{0, 1, 0, 1}
        );

        Hierarchy result = HierarchyFilter.filter(original, nodeId -> nodeId >= 30);

        Hierarchy expected = new ArrayBasedHierarchy(new int[]{30, 40}, new int[]{0, 1});
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("filter() It excludes descendants even if they individually pass the predicate, if an ancestor fails")
    void filter_whenAncestorFails_thenDescendantsAreExcludedEvenIfTheyIndividuallyPass() {
        // Linear chain: 1 -> 2 -> 3(fails) -> 4 -> 5
        Hierarchy original = new ArrayBasedHierarchy(
                new int[]{1, 2, 3, 4, 5},
                new int[]{0, 1, 2, 3, 4}
        );

        Hierarchy result = HierarchyFilter.filter(original, nodeId -> nodeId != 3);

        Hierarchy expected = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});
        assertEquals(expected, result);
    }


    @Test
    @DisplayName("filter() It evaluates a sibling independently after an invalid branch")
    void filter_whenSiblingFollowsInvalidBranch_thenSiblingIsEvaluatedIndependently() {
        // 1(passes) -> 2(fails) -> 3(grandchild, excluded)
        // 1(passes) -> 4(sibling of 2, passes) <- should not be affected
        Hierarchy original = new ArrayBasedHierarchy(
                new int[]{1, 2, 3, 4},
                new int[]{0, 1, 2, 1}
        );

        Hierarchy result = HierarchyFilter.filter(original, nodeId -> nodeId != 2);

        Hierarchy expected = new ArrayBasedHierarchy(new int[]{1, 4}, new int[]{0, 1});
        assertEquals(expected, result);
    }

    // ==================== IMMUTABILITY ====================

    @Test
    @DisplayName("filter() does not modify the original hierarchy")
    void filter_whenCalled_thenDoesNotMutateOriginalHierarchy() {
        Hierarchy original = new ArrayBasedHierarchy(new int[]{1, 2, 3}, new int[]{0, 1, 2});
        String originalSnapshot = original.formatString();

        HierarchyFilter.filter(original, nodeId -> nodeId != 2);

        assertEquals(originalSnapshot, original.formatString());
    }


}
