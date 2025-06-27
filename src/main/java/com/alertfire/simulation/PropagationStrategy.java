package com.alertfire.simulation;

import com.alertfire.model.TreeNode;
import com.alertfire.model.enums.NodeState;
import java.util.List;

/**
 * Interface définissant la stratégie de propagation du feu
 */
public interface PropagationStrategy {
    
    /**
     * Calcule la probabilité de propagation du feu d'un nœud source à un nœud cible
     * 
     * @param source Nœud source (en feu)
     * @param target Nœud cible (potentiellement inflammable)
     * @param windSpeed Vitesse du vent en m/s
     * @param windDirection Direction du vent en degrés (0-360)
     * @return Probabilité de propagation (0.0-1.0)
     */
    double calculatePropagationProbability(TreeNode source, TreeNode target, 
                                         double windSpeed, double windDirection);
    
    /**
     * Détermine quels nœuds devraient prendre feu dans l'étape suivante
     * 
     * @param grid Grille de nœuds
     * @param windSpeed Vitesse du vent
     * @param windDirection Direction du vent
     * @return Liste des nœuds qui vont prendre feu
     */
    List<TreeNode> determineNewFireNodes(TreeNode[][] grid, 
                                       double windSpeed, 
                                       double windDirection);
    
    /**
     * Applique les règles de propagation du feu pour une étape de temps
     * 
     * @param grid Grille de nœuds
     * @param elapsedTime Temps écoulé depuis la dernière mise à jour
     * @param windSpeed Vitesse du vent
     * @param windDirection Direction du vent
     */
    void propagateFire(TreeNode[][] grid, double elapsedTime, 
                      double windSpeed, double windDirection);
}
