package de.samson_elektronik.service;

import java.util.*;

public class DependencyGraph {
    // Adjazenzliste: Task-ID -> Liste der IDs, die von diesem Task abhängen
    private final Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

    public void addTask(int taskId) {
        adjacencyList.putIfAbsent(taskId, new ArrayList<>());
    }

    public void addDependency(int taskId, int dependsOnId) {
        // Kante: dependsOnId -> taskId
        // (dependsOnId muss zuerst erledigt werden, "zeigt auf" taskId)
    }

    public List<Integer> topologicalSort() {
        // kommt gleich
    }
}
