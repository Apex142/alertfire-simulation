package com.alertfire.model.enums;

/**
 * Énumération des états possibles pour un nœud
 */
public enum NodeState {
    EMPTY,    // Cellule vide
    TREE,     // Arbre sain
    BURNING,  // Arbre en feu
    BURNT     // Arbre brûlé
}
