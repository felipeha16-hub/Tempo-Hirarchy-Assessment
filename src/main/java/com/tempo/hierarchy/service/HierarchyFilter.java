package com.tempo.hierarchy.service;

import com.tempo.hierarchy.domain.model.ArrayBasedHierarchy;
import com.tempo.hierarchy.domain.model.Hierarchy;

public class HierarchyFilter {

        public static Hierarchy filter(Hierarchy hierarchy, java.util.function.IntPredicate nodeIdPredicate) {
            // todo implement
            return new ArrayBasedHierarchy(new int[0], new int[0]);
        }
    }

