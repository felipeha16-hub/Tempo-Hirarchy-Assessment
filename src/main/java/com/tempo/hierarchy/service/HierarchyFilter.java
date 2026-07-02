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



            return new ArrayBasedHierarchy(new int[0], new int[0]);
        }
    }

