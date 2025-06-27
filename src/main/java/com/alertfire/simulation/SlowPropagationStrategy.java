package com.alertfire.simulation;

import com.alertfire.model.TreeNode;
import com.alertfire.model.enums.NodeState;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation d'une stratégie de propagation lente du feu
 */
public class SlowPropagationStrategy implements PropagationStrategy {

    private static final double BASE_PROBABILITY = 0.3;   // Probabilité de base faible
    private static final double WIND_FACTOR = 0.05;       // Influence du vent modérée
    private static final double HUMIDITY_FACTOR = 0.2;    // Influence importante de l'humidité
    private static final double BURN_TIME = 15.0;         // Temps de combustion plus long (en secondes)
    private static final double CHECK_RADIUS = 1.0;       // Rayon de vérification limité (1 cellule)

    @Override
    public double calculatePropagationProbability(TreeNode source, TreeNode target,
                                                  double windSpeed, double windDirection) {
        if (target.getState() != NodeState.TREE) {
            return 0.0; // Seuls les arbres peuvent prendre feu
        }

        // Distance entre source et cible
        int sourceRow = source.getRow();
        int sourceCol = source.getCol();
        int targetRow = target.getRow();
        int targetCol = target.getCol();
        double distance = Math.sqrt(Math.pow(targetRow - sourceRow, 2) + Math.pow(targetCol - sourceCol, 2));

        if (distance > 1.5) {
            return 0.0; // Trop loin pour une propagation directe
        }

        // Probabilité de base qui diminue avec la distance
        double probability = BASE_PROBABILITY / distance;

        // Influence du vent (direction et vitesse)
        if (windSpeed > 0) {
            // Calculer l'angle entre la direction du vent et la direction source->cible
            double windRadians = Math.toRadians(windDirection);
            double targetRadians = Math.atan2(targetRow - sourceRow, targetCol - sourceCol);

            // Convertir en degrés et normaliser
            double windDegrees = Math.toDegrees(windRadians);
            double targetDegrees = Math.toDegrees(targetRadians);

            // Calculer la différence d'angle
            double angleDiff = Math.abs(windDegrees - targetDegrees);
            while (angleDiff > 180) angleDiff = 360 - angleDiff;

            // Le feu se propage mieux dans la direction du vent
            double windAlignment = 1.0 - (angleDiff / 180.0);
            probability += WIND_FACTOR * windSpeed * windAlignment;
        }

        // Influence de l'humidité (humidité élevée = propagation réduite)
        probability -= (target.getHumidity() / 100.0) * HUMIDITY_FACTOR;

        // Assurer que la probabilité reste dans les limites valides
        return Math.max(0.0, Math.min(1.0, probability));
    }

    @Override
    public void propagateFire(TreeNode[][] grid, double elapsedTime,
                              double windSpeed, double windDirection) {
        // Déterminer les nouveaux nœuds qui vont prendre feu
        List<TreeNode> newFireNodes = determineNewFireNodes(grid, windSpeed, windDirection);

        // Mettre le feu aux nouveaux nœuds
        for (TreeNode node : newFireNodes) {
            if (node.getState() == NodeState.TREE) {
                node.setState(NodeState.BURNING);
                node.setFireIntensity(0.2); // Intensité initiale plus faible
                node.setBurningTime(0.0);   // Réinitialiser le temps de combustion
            }
        }

        // Mettre à jour tous les nœuds existants
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                TreeNode node = grid[r][c];
                if (node.getState() == NodeState.BURNING) {
                    // Ajouter le temps écoulé au temps de combustion
                    node.setBurningTime(node.getBurningTime() + elapsedTime);

                    // Augmenter l'intensité du feu lentement
                    node.setFireIntensity(Math.min(1.0, node.getFireIntensity() + 0.05 * elapsedTime));

                    // Si le temps de combustion dépasse le seuil, l'arbre devient brûlé
                    if (node.getBurningTime() > BURN_TIME) {
                        node.setState(NodeState.BURNT);
                        System.out.println("Arbre brûlé en position (" + r + "," + c + ")");
                    }
                }
            }
        }
    }

    @Override
    public List<TreeNode> determineNewFireNodes(TreeNode[][] grid,
                                                double windSpeed,
                                                double windDirection) {
        List<TreeNode> newFireNodes = new ArrayList<>();
        int rows = grid.length;
        int cols = grid[0].length;

        // Pour chaque nœud en feu, vérifier les voisins
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (grid[r][c].getState() == NodeState.BURNING) {
                    // Vérifier uniquement les voisins immédiats (propagation plus limitée)
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            // Ignorer le nœud lui-même
                            if (dr == 0 && dc == 0) continue;

                            int nr = r + dr;
                            int nc = c + dc;

                            // Vérifier les limites de la grille
                            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                                TreeNode neighbor = grid[nr][nc];
                                double distance = Math.sqrt(dr*dr + dc*dc);

                                // Ne vérifier que les cellules adjacentes
                                if (distance <= CHECK_RADIUS) {
                                    // Calculer la probabilité de propagation
                                    double prob = calculatePropagationProbability(
                                            grid[r][c], neighbor, windSpeed, windDirection);

                                    // Déterminer si le feu se propage (probabilité standard)
                                    if (Math.random() < prob * 0.8) {
                                        newFireNodes.add(neighbor);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return newFireNodes;
    }
}
