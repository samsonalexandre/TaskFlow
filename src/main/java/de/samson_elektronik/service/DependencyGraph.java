package de.samson_elektronik.service;

import java.util.*;

public class DependencyGraph {
    // Adjazenzliste: Task-ID -> Liste der IDs, die von diesem Task abhängen
    private final Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

    public void addTask(int taskId) {
        adjacencyList.putIfAbsent(taskId, new ArrayList<>());
    }

    public void addDependency(int taskId, int dependsOnId) {
        addTask(taskId);
        addTask(dependsOnId);
        adjacencyList.get(dependsOnId).add(taskId);
    }

    public List<Integer> topologicalSort() {
        Map<Integer, Integer> inDegree = new HashMap<>();
        for (int taskId : adjacencyList.keySet()) {
            inDegree.put(taskId, 0);
        }
        // Kahn's Algorithmus
        // Schritt 1: In-Degree für jeden Knoten berechnen
        for (List<Integer> neighbors : adjacencyList.values()) {
            for (int neighbor : neighbors) {
                inDegree.put(neighbor, inDegree.get(neighbor) + 1);
            }
        }

        // Schritt 2: Alle Knoten mit In-Degree 0 in eine Queue
        Queue<Integer> queue = new LinkedList<>();
        for (Map.Entry<Integer, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<Integer> result = new ArrayList<>();

        // Schritt 3: Queue abarbeiten
        while (!queue.isEmpty()) {
            int current = queue.poll();
            result.add(current);

            for (int neighbor : adjacencyList.get(current)) {
                // In-Degree des Nachbarn verringern
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);

                // wenn 0 erreicht: zur Queue hinzufügen
                if (inDegree.get(neighbor) == 0) {
                    queue.add(neighbor);
                }
            }
        }

        // Schritt 4: Zyklen-Check
        if (result.size() != adjacencyList.size()) {
            throw new IllegalStateException("Zyklus in den Abhängigkeiten erkannt!");
        }

        return result;
    }
}
