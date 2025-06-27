package com.alertfire.simulation;

/**
 * Factory pour créer des stratégies de propagation du feu
 */
public class PropagationFactory {
    
    /**
     * Crée une stratégie de propagation en fonction du type spécifié
     * 
     * @param type Type de stratégie ("FAST" ou "SLOW")
     * @return Une instance de PropagationStrategy
     */
    public static PropagationStrategy createStrategy(String type) {
        switch (type.toUpperCase()) {
            case "FAST":
                return new FastPropagationStrategy();
            case "SLOW":
                return new SlowPropagationStrategy();
            default:
                System.out.println("Type de stratégie inconnu, utilisation de la stratégie lente par défaut");
                return new SlowPropagationStrategy();
        }
    }
}
