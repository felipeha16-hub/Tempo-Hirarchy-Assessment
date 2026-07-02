package com.tempo.hierarchy.service;

import com.tempo.hierarchy.domain.model.ArrayBasedHierarchy;
import com.tempo.hierarchy.domain.model.Hierarchy;

import java.util.*;
import java.util.function.IntPredicate;

public class HierarchyFilter {


        //Private constructor to prevent instantiation of utility class.
        private HierarchyFilter() {
            throw new UnsupportedOperationException("Utility class cannot be instantiated");
        }

        public static Hierarchy filter(Hierarchy hierarchy, IntPredicate nodeIdPredicate) {

            Objects.requireNonNull(hierarchy, "Hierarchy cannot be null");
            Objects.requireNonNull(nodeIdPredicate, "nodeIdPredicate cannot be null");

            if (hierarchy.isEmpty()) {
                return new ArrayBasedHierarchy(new int[0], new int[0]);
            }


            List<Integer> resultNodeIds = new ArrayList<>();
            List<Integer> resultDepths = new ArrayList<>();

            Deque<Boolean> ancestorValidityStack = new ArrayDeque<>();

            for (int i=0; i < hierarchy.size(); i++) {
                int nodeId = hierarchy.nodeId(i);
                int depth = hierarchy.depth(i);

                //Pop the stack back to the level of this node's parent.

                while(ancestorValidityStack.size() > depth) {
                    ancestorValidityStack.pop();
                }

                //Check if immediate parent is valid.
                boolean parentIsValid = ancestorValidityStack.isEmpty() || ancestorValidityStack.peek();

                //This node is valid if the parent is valid +and it passes the predicate.
                boolean isValid = parentIsValid && nodeIdPredicate.test(nodeId);

                // Stack the result of this node, which will be the parent in the next iteration.
                ancestorValidityStack.push(isValid);

                if(isValid) {
                    resultNodeIds.add(nodeId);
                    resultDepths.add(depth);
                }
            }


            int[] finalNodeIds = resultNodeIds.stream().mapToInt(Integer::intValue).toArray();
            int[] finalDepths = resultDepths.stream().mapToInt(Integer::intValue).toArray();

            return new ArrayBasedHierarchy(finalNodeIds, finalDepths);
        }
    }

