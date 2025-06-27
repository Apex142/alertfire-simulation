package com.alertfire.ui;

import com.alertfire.model.TreeNode;
import com.alertfire.model.enums.NodeState;
import com.alertfire.device.ProjectNode;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import java.util.Random;

/**
 * Affichage graphique de la grille de simulation
 */
public class GridView {

    private GridPane gridPane;
    private TreeNode[][] grid;
    private int width;
    private int height;
    private double cellSize;
    private Random random = new Random();

    /**
     * Constructeur de la vue de grille
     * @param width Largeur de la grille en cellules
     * @param height Hauteur de la grille en cellules
     * @param cellSize Taille de chaque cellule en pixels
     */
    public GridView(int width, int height, double cellSize) {
        this.width = width;
        this.height = height;
        this.cellSize = cellSize;

        gridPane = new GridPane();
        grid = new TreeNode[height][width];

        // Initialiser la grille
        initGrid();
    }

    /**
     * Initialise la grille avec des cellules vides
     */
    private void initGrid() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                TreeNode node = new TreeNode(row, col, cellSize, cellSize);

                // Ajouter la cellule à la grille
                grid[row][col] = node;
                gridPane.add(node, col, row);

                // Ajouter un gestionnaire d'événements de clic
                final int r = row;
                final int c = col;
                node.setOnMouseClicked(e -> handleCellClick(r, c));
            }
        }

        // Ajouter une bordure et un padding pour mieux visualiser la grille
        gridPane.setStyle("-fx-border-color: black; -fx-border-width: 1px;");
        gridPane.setHgap(1);
        gridPane.setVgap(1);
    }

    /**
     * Gère le clic sur une cellule de la grille
     */
    private void handleCellClick(int row, int col) {
        // Cette méthode est maintenant gérée par SimulationMenu
        System.out.println("Clic sur la cellule (" + row + ", " + col + ")");
    }

    /**
     * Réinitialise la grille à son état initial (toutes les cellules vides)
     */
    public void resetGrid() {
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                grid[row][col].setState(NodeState.EMPTY);
            }
        }
    }

    /**
     * Met à jour l'interface utilisateur
     */
    public void updateUI() {
        // Rien à faire ici car les nœuds mettent à jour leur propre couleur
    }

    /**
     * Génère une forêt aléatoire avec une densité donnée
     * @param density Densité de la forêt (0.0 - 1.0)
     */
    public void generateRandomForest(double density) {
        resetGrid();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (random.nextDouble() < density) {
                    grid[row][col].setState(NodeState.TREE);

                    // Variation aléatoire de l'humidité
                    grid[row][col].setHumidity(30 + random.nextDouble() * 40); // 30-70% d'humidité
                }
            }
        }
    }

    /**
     * Ajoute un projet à la grille
     * @param project Le projet à ajouter
     * @param row La ligne
     * @param col La colonne
     */
    public void addProject(ProjectNode project, int row, int col) {
        // Créer un conteneur pour le projet
        Pane projectPane = new Pane();
        projectPane.getChildren().add(project);

        // Positionner le projet au centre de la cellule
        project.setCenterX(cellSize / 2);
        project.setCenterY(cellSize / 2);

        // Ajouter le projet à la grille
        gridPane.add(projectPane, col, row);

        // S'assurer que le projet est visible au-dessus des cellules
        projectPane.toFront();
    }

    /**
     * Supprime tous les projets de la grille
     */
    public void clearProjects() {
        // Conserver uniquement les TreeNodes dans la grille
        gridPane.getChildren().removeIf(node -> node instanceof ProjectNode);
    }

    /**
     * Retourne le panneau de grille
     */
    public GridPane getGridPane() {
        return gridPane;
    }

    /**
     * Retourne la grille de nœuds
     */
    public TreeNode[][] getGrid() {
        return grid;
    }

    /**
     * Retourne la largeur de la grille
     */
    public int getWidth() {
        return width;
    }

    /**
     * Retourne la hauteur de la grille
     */
    public int getHeight() {
        return height;
    }
}
