package com.tempo.hierarchy.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArrayBasedHierarchyTest {

    // ==================== CONSTRUCTOR: NULL CHECKS ====================

    @Test
    @DisplayName("constructor throws NullPointerException when nodeIds is null")
    void constructor_whenNodeIdsIsNull_thenThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new ArrayBasedHierarchy(null, new int[]{0}));
    }

    @Test
    @DisplayName("constructor throws NullPointerException when depths is null")
    void constructor_whenDepthsIsNull_thenThrowsNullPointerException() {
        assertThrows(NullPointerException.class,
                () -> new ArrayBasedHierarchy(new int[]{1}, null));
    }

    // ==================== CONSTRUCTOR: ARRAY LENGTH ====================

    @Test
    @DisplayName("constructor throws IllegalArgumentException when array lengths differ")
    void constructor_whenArrayLengthsDiffer_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0}));
    }

    // ==================== CONSTRUCTOR: INVARIANTS ====================

    @Test
    @DisplayName("constructor throws IllegalArgumentException when first depth is not 0")
    void constructor_whenFirstDepthIsNotZero_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{1, 2}));
    }

    @Test
    @DisplayName("constructor throws IllegalArgumentException when a depth is negative")
    void constructor_whenDepthIsNegative_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, -1}));
    }

    @Test
    @DisplayName("constructor throws IllegalArgumentException when depth jumps more than one level")
    void constructor_whenDepthJumpsMoreThanOneLevel_thenThrowsIllegalArgumentException() {
        // Jumps from depth 0 to depth 2 directly (missing level 1)
        assertThrows(IllegalArgumentException.class,
                () -> new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 2}));
    }

    @Test
    @DisplayName("constructor allows empty arrays (empty hierarchy is valid)")
    void constructor_whenArraysAreEmpty_thenCreatesEmptyHierarchy() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[0], new int[0]);

        assertEquals(0, hierarchy.size());
        assertTrue(hierarchy.isEmpty());
    }

    @Test
    @DisplayName("constructor accepts a valid, complex hierarchy without throwing")
    void constructor_whenHierarchyIsValid_thenCreatesSuccessfully() {
        assertDoesNotThrow(() -> new ArrayBasedHierarchy(
                new int[]{1, 2, 3, 4, 5},
                new int[]{0, 1, 2, 1, 0}
        ));
    }

    // ==================== size(), nodeId(), depth() ====================

    @Test
    @DisplayName("size() returns the correct number of nodes")
    void size_whenHierarchyHasNodes_thenReturnsCorrectCount() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{1, 2, 3}, new int[]{0, 1, 2});

        assertEquals(3, hierarchy.size());
    }

    @Test
    @DisplayName("nodeId() returns the correct id at each position")
    void nodeId_whenIndexIsValid_thenReturnsCorrectNodeId() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{10, 20, 30}, new int[]{0, 1, 2});

        assertEquals(10, hierarchy.nodeId(0));
        assertEquals(20, hierarchy.nodeId(1));
        assertEquals(30, hierarchy.nodeId(2));
    }

    @Test
    @DisplayName("depth() returns the correct depth at each position")
    void depth_whenIndexIsValid_thenReturnsCorrectDepth() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{10, 20, 30}, new int[]{0, 1, 2});

        assertEquals(0, hierarchy.depth(0));
        assertEquals(1, hierarchy.depth(1));
        assertEquals(2, hierarchy.depth(2));
    }

    @Test
    @DisplayName("nodeId() throws IndexOutOfBoundsException for a negative index")
    void nodeId_whenIndexIsNegative_thenThrowsIndexOutOfBoundsException() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{1}, new int[]{0});

        assertThrows(IndexOutOfBoundsException.class, () -> hierarchy.nodeId(-1));
    }

    @Test
    @DisplayName("nodeId() throws IndexOutOfBoundsException when index equals size")
    void nodeId_whenIndexEqualsSize_thenThrowsIndexOutOfBoundsException() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{1}, new int[]{0});

        assertThrows(IndexOutOfBoundsException.class, () -> hierarchy.nodeId(1));
    }

    @Test
    @DisplayName("depth() throws IndexOutOfBoundsException for an out-of-range index")
    void depth_whenIndexIsOutOfBounds_thenThrowsIndexOutOfBoundsException() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{1}, new int[]{0});

        assertThrows(IndexOutOfBoundsException.class, () -> hierarchy.depth(5));
    }

    // ==================== isEmpty() ====================

    @Test
    @DisplayName("isEmpty() returns true for a hierarchy with no nodes")
    void isEmpty_whenHierarchyHasNoNodes_thenReturnsTrue() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[0], new int[0]);

        assertTrue(hierarchy.isEmpty());
    }

    @Test
    @DisplayName("isEmpty() returns false for a hierarchy with at least one node")
    void isEmpty_whenHierarchyHasNodes_thenReturnsFalse() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{1}, new int[]{0});

        assertFalse(hierarchy.isEmpty());
    }

    // ==================== equals() and hashCode() ====================

    @Test
    @DisplayName("equals() returns true when both hierarchies have identical content")
    void equals_whenContentIsIdentical_thenReturnsTrue() {
        Hierarchy h1 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});
        Hierarchy h2 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});

        assertEquals(h1, h2);
    }

    @Test
    @DisplayName("equals() returns false when nodeIds differ")
    void equals_whenNodeIdsDiffer_thenReturnsFalse() {
        Hierarchy h1 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});
        Hierarchy h2 = new ArrayBasedHierarchy(new int[]{1, 3}, new int[]{0, 1});

        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("equals() returns false when depths differ")
    void equals_whenDepthsDiffer_thenReturnsFalse() {
        Hierarchy h1 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});
        Hierarchy h2 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 0});

        assertNotEquals(h1, h2);
    }

    @Test
    @DisplayName("equals() returns true when compared to itself")
    void equals_whenComparingSameInstance_thenReturnsTrue() {
        Hierarchy h1 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});

        assertEquals(h1, h1);
    }

    @Test
    @DisplayName("equals() returns false when compared to null")
    void equals_whenComparedWithNull_thenReturnsFalse() {
        Hierarchy h1 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});

        assertNotEquals(null, h1);
    }

    @Test
    @DisplayName("equals() returns false when compared to an object of a different class")
    void equals_whenComparedWithDifferentClass_thenReturnsFalse() {
        Hierarchy h1 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});

        assertNotEquals("a hierarchy", h1);
    }

    @Test
    @DisplayName("hashCode() is consistent for equal objects (equals/hashCode contract)")
    void hashCode_whenObjectsAreEqual_thenHashCodesMatch() {
        Hierarchy h1 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});
        Hierarchy h2 = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});

        assertEquals(h1.hashCode(), h2.hashCode());
    }

    // ==================== toString() ====================

    @Test
    @DisplayName("toString() includes the size and the formatted content")
    void toString_whenCalled_thenContainsSizeAndFormattedData() {
        Hierarchy hierarchy = new ArrayBasedHierarchy(new int[]{1, 2}, new int[]{0, 1});

        String result = hierarchy.toString();

        assertTrue(result.contains("size=2"));
        assertTrue(result.contains("[1:0, 2:1]"));
    }

    // ==================== IMMUTABILITY ====================

    @Test
    @DisplayName("modifying the original array after construction does not affect the hierarchy")
    void constructor_whenOriginalArrayIsModifiedAfterConstruction_thenHierarchyIsUnaffected() {
        int[] nodeIds = {1, 2, 3};
        int[] depths = {0, 1, 2};
        Hierarchy hierarchy = new ArrayBasedHierarchy(nodeIds, depths);

        // Modify the ORIGINAL arrays after the object has been constructed
        nodeIds[0] = 999;
        depths[0] = 999;

        // The hierarchy must keep the original values, not the modified ones
        assertEquals(1, hierarchy.nodeId(0));
        assertEquals(0, hierarchy.depth(0));
    }
}
