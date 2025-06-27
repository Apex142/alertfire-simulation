package com.alertfire.model;

import com.alertfire.model.enums.NodeState;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Représente un nœud dans la grille de simulation
 */
public class TreeNode extends Rectangle {

    private NodeState state;
    private double fireIntensity;
    private double humidity;
    private double burningTime;
    private int row;
    private int col;

    public TreeNode(int row, int col, double width, double height) {
        super(width, height);
        this.row = row;
        this.col = col;
        this.state = NodeState.EMPTY;
        this.fireIntensity = 0.0;
        this.humidity = 50.0; // valeur par défaut d'humidité (%)
        this.burningTime = 0.0;

        // Style initial
        setStroke(Color.BLACK);
        setStrokeWidth(0.5);
        updateColor();
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
        updateColor();
    }

    public double getFireIntensity() {
        return fireIntensity;
    }

    public void setFireIntensity(double fireIntensity) {
        this.fireIntensity = fireIntensity;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getBurningTime() {
        return burningTime;
    }

    public void setBurningTime(double burningTime) {
        this.burningTime = burningTime;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    /**
     * Met à jour la couleur du nœud en fonction de son état
     */
    private void updateColor() {
        switch (state) {
            case EMPTY:
                setFill(Color.LIGHTGRAY);
                break;
            case TREE:
                setFill(Color.GREEN);
                break;
            case BURNING:
                setFill(Color.RED);
                break;
            case BURNT:
                setFill(Color.BROWN); // Changé de BLACK à BROWN pour les arbres brûlés
                break;
        }
    }

    /**
     * Incrémente le temps de combustion et vérifie si l'arbre est complètement brûlé
     * @param elapsedTime Temps écoulé en secondes
     * @return true si l'état a changé, false sinon
     */
    public boolean update(double elapsedTime) {
        if (state == NodeState.BURNING) {
            burningTime += elapsedTime;

            // Si le temps de combustion dépasse un seuil, l'arbre devient brûlé
            if (burningTime > 10.0) { // 10 secondes de combustion par défaut
                setState(NodeState.BURNT);
                fireIntensity = 0.0;
                return true;
            }

            // Variation de l'intensité du feu en fonction du temps
            fireIntensity = Math.min(1.0, fireIntensity + elapsedTime * 0.1);
        }

        return false;
    }

    /**
     * Copie l'état d'un autre nœud vers celui-ci
     * @param source Nœud source
     */
    public void copyStateFrom(TreeNode source) {
        this.setState(source.getState());
        this.setFireIntensity(source.getFireIntensity());
        this.setBurningTime(source.getBurningTime());
        this.setHumidity(source.getHumidity());
        // Copier toutes les autres propriétés pertinentes
    }
}
