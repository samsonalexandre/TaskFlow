package de.samson_elektronik.service;

import java.util.*;

/**
 * Bildet Aufgaben als gerichteten Graphen ab (Adjazenzliste) und
 * berechnet daraus eine gültige Abarbeitungsreihenfolge mittels
 * Kahn's Algorithmus (topologische Sortierung).
 *
 * Arbeitet bewusst nur mit Task-IDs (int), nicht mit vollen Task-Objekten:
 * das entkoppelt die Graph-Logik vom Domänenmodell und vermeidet
 * Probleme mit veralteten Objektreferenzen bzw. fehlender equals()/hashCode()
 * auf Task.
 */
public class DependencyGraph {
    // Knoten-ID -> Liste der IDs, die von diesem Knoten abhängen
    private final Map<Integer, List<Integer>> adjacencyList = new HashMap<>();

    /**
     * Fügt einen Knoten (Task-ID) hinzu, falls er noch nicht existiert.
     * Wichtig, damit auch Aufgaben ohne jede Abhängigkeit im Graphen
     * auftauchen und in der Sortierung nicht verschwinden.
     */
    public void addTask(int taskId) {
        adjacencyList.putIfAbsent(taskId, new ArrayList<>());
    }

    /**
     * Fügt eine gerichtete Kante hinzu: dependsOnId -> taskId,
     * d.h. dependsOnId muss vor taskId erledigt sein.
     */
    public void addDependency(int taskId, int dependsOnId) {
        addTask(taskId);
        addTask(dependsOnId);
        adjacencyList.get(dependsOnId).add(taskId);
    }

    /**
     * Berechnet eine gültige Abarbeitungsreihenfolge mit Kahn's Algorithmus:
     * 1. In-Degree (Anzahl offener Abhängigkeiten) je Knoten berechnen
     * 2. Alle Knoten ohne offene Abhängigkeiten in eine Warteschlange
     * 3. Warteschlange abarbeiten, dabei In-Degree der Nachbarn verringern
     * 4. Wird 0 erreicht, kommt der Nachbar in die Warteschlange
     *
     * Wirft IllegalStateException, wenn ein Zyklus vorliegt (erkennbar
     * daran, dass am Ende weniger Knoten im Ergebnis stehen als
     * insgesamt im Graphen existieren - diese Knoten konnten nie
     * einen In-Degree von 0 erreichen).
     */
    public List<Integer> topologicalSort() {
        Map<Integer, Integer> inDegree = new HashMap<>();
        for (int taskId : adjacencyList.keySet()) {
            inDegree.put(taskId, 0);
        }

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
