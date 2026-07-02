package com.tempo.hierarchy.domain.model;

import java.util.Arrays;
import java.util.Objects;

public class ArrayBasedHierarchy implements Hierarchy {

    private final int[] nodeIds;
    private final int[] depths;


    public ArrayBasedHierarchy(int[] nodeIds, int[] depths) {

        //Null Checks
        Objects.requireNonNull(nodeIds,  "nodeIds array must not be null");
        Objects.requireNonNull(depths, "depths array must not be null");

        // Arrays must have the same length
        if (nodeIds.length != depths.length) {
            throw new IllegalArgumentException(String.format("nodeIds and depths arrays must have same length: nodeIds.length=%d, depths.length=%d",
                    nodeIds.length, depths.length));
        }
        // Defensive copy to ensure immutability
        this.nodeIds = Arrays.copyOf(nodeIds, nodeIds.length);
        this.depths = Arrays.copyOf(depths, depths.length);

        // Validate hierarchy invariants
        validateInvariants();

    }

    private void validateInvariants() {
        if (depths.length == 0) {
            return; // Empty hierarchy is valid
        }

        // Invariant 1: First element must have depth 0
        if (depths[0] != 0) {
            throw new IllegalArgumentException(
                    String.format("First element must have depth 0, but has depth %d", depths[0])
            );
        }

        // Invariant 2: All depths must be non-negative
        for (int i = 0; i < depths.length; i++) {
            if (depths[i] < 0) {
                throw new IllegalArgumentException(
                        String.format("Depth at index %d is negative: %d", i, depths[i])
                );
            }
        }

        // Invariant 3: Depth transitions must be valid
        // Next depth can be: current + 1 (child), current (sibling), or < current (backtrack)
        for (int i = 0; i < depths.length - 1; i++) {
            int currentDepth = depths[i];
            int nextDepth = depths[i + 1];

            if (nextDepth > currentDepth + 1) {
                throw new IllegalArgumentException(
                        String.format("Invalid depth transition at index %d: depth jumps from %d to %d (max allowed: %d)",
                                i, currentDepth, nextDepth, currentDepth + 1)
                );
            }
        }
    }


    @Override
    public int size() {
        return depths.length;
    }

    @Override
    public int nodeId(int index) {
        // Validate that the index is neither negative nor exceeding the upper bound.
        if (index < 0 || index >= depths.length) {
            throw new IndexOutOfBoundsException(
                    String.format("Index %d out of bounds for hierarchy of size %d", index, depths.length)
            );
        }
        return nodeIds[index];
    }

    @Override
    public int depth(int index) {
        // Validate that the index is neither negative nor exceeding the upper bound.
        if (index < 0 || index >= depths.length) {
            throw new IndexOutOfBoundsException(
                    String.format("Index %d out of bounds for hierarchy of size %d", index, depths.length)
            );
        }
        return depths[index];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayBasedHierarchy that = (ArrayBasedHierarchy) o;
        return Arrays.equals(nodeIds, that.nodeIds) && Arrays.equals(depths, that.depths);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(nodeIds);
        result = 31 * result + Arrays.hashCode(depths);
        return result;
    }

    @Override
    public String toString() {
        return "ArrayBasedHierarchy{size=" + size() + ", data=" + formatString() + "}";
    }



}
