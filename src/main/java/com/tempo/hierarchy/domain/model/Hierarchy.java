package com.tempo.hierarchy.domain.model;


public interface Hierarchy {

    int size(); // Returns the total number of nodes.
    int nodeId(int index); //Returns the node ID at the position specified by the index.
    int depth(int index); //Returns the depth of the node at the position specified by the index.


    // Default method for visualizing the hierarchy.
    default String formatString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(nodeId(i)).append(":").append(depth(i));
        }
        sb.append("]");
        return sb.toString();
    }
    // Returns true if the hierarchy has no nodes, false otherwise
    default boolean isEmpty() {
        return size() == 0;
    }
}


