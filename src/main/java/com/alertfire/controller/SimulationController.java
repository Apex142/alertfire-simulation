package com.alertfire.controller;

import com.alertfire.ui.GridView;
import com.alertfire.model.TreeNode;
import com.alertfire.model.enums.NodeState;
import com.alertfire.simulation.PropagationStrategy;
import com.alertfire.config.SimulationConfig;
import com.alertfire.device.ProjectNode;
import com.alertfire.model.enums.ProjectType;
import com.alertfire.communication.LoRaReceiver;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur principal pour la simulation de feu de forêt
 */
public class SimulationController {

    private GridView gridView;
    private PropagationStrategy propagationStrategy;
    private SimulationConfig config;
    private AnimationTimer simulationTimer;
    private boolean isRunning = false;
    private long lastUpdate = 0;
    private double windSpeed;
    private double windDirection;
    private List<ProjectNode> projects = new ArrayList<>();
    private Random random = new Random();

    // Historique des états pour le bouton Back
    private Stack<SimulationState> history = new Stack<>();
    private double totalSimulationTime = 0.0;

    /**
     * Constructeur du contrôleur de simulation
     * @param gridView Vue de la grille
     * @param strategy Stratégie de propagation
     * @param config Configuration de la simulation
     */
    public SimulationController(GridView gridView, PropagationStrategy strategy, SimulationConfig config) {
        this.gridView = gridView;
        this.propagationStrategy = strategy;
        this.config = config;

        // Initialiser les paramètres météorologiques
        this.windSpeed = config.getInitialWindSpeed();
        this.windDirection = config.getInitialWindDirection();

        // S'abonner aux messages LoRa
        LoRaReceiver.getInstance().addListener(message -> {
            if (message.isFireDetected()) {
                Platform.runLater(() -> {
                    System.out.println("Alerte de feu reçue de Projet " + message.getUuid() +
                            " à (" + message.getRow() + "," + message.getCol() + ")");
                });
            }
        });

        // Initialiser le minuteur de simulation
        initSimulationTimer();

        // Sauvegarder l'état initial
        saveCurrentState();
    }

    /**
     * Initialise le minuteur d'animation pour la simulation
     */
    private void initSimulationTimer() {
        simulationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Calculer le temps écoulé en secondes
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double elapsedTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                // Mettre à jour la simulation
                update(elapsedTime);
            }
        };
    }

    /**
     * Démarre la simulation
     */
    public void startSimulation() {
        if (!isRunning) {
            isRunning = true;
            lastUpdate = 0;
            simulationTimer.start();
        }
    }

    /**
     * Arrête la simulation
     */
    public void stopSimulation() {
        if (isRunning) {
            isRunning = false;
            simulationTimer.stop();
        }
    }

    /**
     * Met en pause la simulation
     */
    public void pauseSimulation() {
        stopSimulation();
    }

    /**
     * Redémarre la simulation depuis le début
     */
    public void resetSimulation() {
        stopSimulation();
        gridView.resetGrid();
        projects.clear();
        gridView.clearProjects();
    }

    /**
     * Effectue une étape unique de simulation
     */
    public void stepSimulation() {
        if (!isRunning) {
            update(config.getStepTime());
        }
    }

    /**
     * Met à jour la simulation pour un pas de temps
     * @param elapsedTime Temps écoulé depuis la dernière mise à jour
     */
    private void update(double elapsedTime) {
        // Incrémenter le temps total de simulation
        totalSimulationTime += elapsedTime;

        // Sauvegarder l'état actuel avant modifications
        saveCurrentState();

        // Propager le feu
        propagationStrategy.propagateFire(gridView.getGrid(), elapsedTime, windSpeed, windDirection);

        // Mettre à jour les projets et détecter les incendies
        updateProjects(elapsedTime);

        // Varier légèrement les conditions météorologiques
        updateWeatherConditions(elapsedTime);

        // Mettre à jour l'interface graphique
        gridView.updateUI();
    }

    /**
     * Met à jour les projets et leur détection d'incendie
     * @param elapsedTime Temps écoulé depuis la dernière mise à jour
     */
    private void updateProjects(double elapsedTime) {
        // Créer une carte de feu pour la détection efficace
        boolean[][] fireGrid = new boolean[config.getGridHeight()][config.getGridWidth()];
        TreeNode[][] grid = gridView.getGrid();

        for (int r = 0; r < config.getGridHeight(); r++) {
            for (int c = 0; c < config.getGridWidth(); c++) {
                fireGrid[r][c] = grid[r][c].getState() == NodeState.BURNING;
            }
        }

        // Faire détecter les incendies par les projets
        double temperature = 20.0 + random.nextDouble() * 5.0; // 20-25°C
        double humidity = Math.max(0, Math.min(100, config.getHumidity() + random.nextDouble() * 10 - 5)); // ±5%

        for (ProjectNode project : projects) {
            // Mettre à jour l'état énergétique du projet
            project.update(elapsedTime, config.getCellSizeKm());

            // Détecter et signaler les incendies
            project.detectAndReport(fireGrid, temperature, humidity, totalSimulationTime, config.getCellSizeKm());
        }
    }

    /**
     * Retourne à l'état précédent de la simulation
     */
    public void goBack() {
        if (history.size() > 1) {
            // Supprimer l'état actuel
            history.pop();

            // Restaurer l'état précédent
            SimulationState previousState = history.peek();
            restoreState(previousState);

            // Mettre à jour l'interface
            gridView.updateUI();
            System.out.println("Retour à l'état précédent");
        } else {
            System.out.println("Impossible de revenir en arrière - pas d'historique disponible");
        }
    }

    /**
     * Sauvegarde l'état actuel de la simulation
     */
    private void saveCurrentState() {
        // Limiter la taille de l'historique pour éviter les problèmes de mémoire
        if (history.size() >= 20) {
            // Garder seulement les 10 états les plus récents
            Stack<SimulationState> tempStack = new Stack<>();
            for (int i = 0; i < 10; i++) {
                tempStack.push(history.pop());
            }
            history.clear();
            while (!tempStack.isEmpty()) {
                history.push(tempStack.pop());
            }
        }

        // Créer une copie profonde de l'état actuel
        SimulationState currentState = new SimulationState(
                gridView.getGrid(),
                projects,
                windSpeed,
                windDirection,
                totalSimulationTime
        );

        history.push(currentState);
    }

    /**
     * Met à jour les conditions météorologiques avec de petites variations
     * @param elapsedTime Temps écoulé depuis la dernière mise à jour
     */
    private void updateWeatherConditions(double elapsedTime) {
        // Petites variations aléatoires dans la vitesse et la direction du vent
        windSpeed += (random.nextDouble() - 0.5) * elapsedTime;
        windSpeed = Math.max(0, Math.min(config.getMaxWindSpeed(), windSpeed));

        windDirection += (random.nextDouble() - 0.5) * 10 * elapsedTime;
        windDirection = (windDirection + 360) % 360;
    }

    /**
     * Restaure un état précédent de la simulation
     * @param state État à restaurer
     */
    private void restoreState(SimulationState state) {
        // Restaurer la grille
        copyGridState(state.getGrid(), gridView.getGrid());

        // Restaurer les projets
        projects = state.getProjects();
        gridView.clearProjects();
        for (ProjectNode project : projects) {
            gridView.addProject(project, project.getRow(), project.getCol());
        }

        // Restaurer les conditions météo
        windSpeed = state.getWindSpeed();
        windDirection = state.getWindDirection();

        // Restaurer le temps de simulation
        totalSimulationTime = state.getSimulationTime();
    }

    /**
     * Copie l'état d'une grille vers une autre
     * @param source Grille source
     * @param target Grille cible
     */
    private void copyGridState(TreeNode[][] source, TreeNode[][] target) {
        for (int r = 0; r < source.length; r++) {
            for (int c = 0; c < source[0].length; c++) {
                target[r][c].copyStateFrom(source[r][c]);
            }
        }
    }

    /**
     * Démarre un incendie à une position spécifiée
     * @param row Ligne
     * @param col Colonne
     */
    public void startFire(int row, int col) {
        TreeNode node = gridView.getGrid()[row][col];
        if (node.getState() == NodeState.TREE) {
            node.setState(NodeState.BURNING);
            node.setFireIntensity(0.5);
            System.out.println("Feu démarré en position (" + row + "," + col + ")");
        } else {
            System.out.println("Impossible de démarrer un feu à cette position - pas d'arbre");
        }
    }

    /**
     * Génère une forêt aléatoire avec une certaine densité
     */
    public void generateRandomForest(double density) {
        resetSimulation();
        gridView.generateRandomForest(density);
    }

    /**
     * Ajoute un projet à la simulation
     * @param row Ligne
     * @param col Colonne
     * @param type Type de projet (MASTER ou SLAVE)
     */
    public void addProject(int row, int col, ProjectType type) {
        // Vérifier si un projet existe déjà à cette position
        for (ProjectNode existingProject : projects) {
            if (existingProject.getRow() == row && existingProject.getCol() == col) {
                System.out.println("Un projet existe déjà à cette position");
                return;
            }
        }

        // Créer un nouveau projet
        ProjectNode project = new ProjectNode(row, col, config.getCellSize(), type);
        projects.add(project);
        gridView.addProject(project, row, col);

        System.out.println("Projet " + type + " ajouté en position (" + row + "," + col + ")");
    }

    // Getters et setters

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindDirection(double windDirection) {
        this.windDirection = windDirection;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setPropagationStrategy(PropagationStrategy strategy) {
        this.propagationStrategy = strategy;
    }

    // Ajouter une méthode pour récupérer la vue de la grille
    public GridView getGridView() {
        return gridView;
    }

    /**
     * Classe interne pour stocker l'état de la simulation
     */
    private static class SimulationState {
        private TreeNode[][] grid;
        private List<ProjectNode> projects;
        private double windSpeed;
        private double windDirection;
        private double simulationTime;

        public SimulationState(TreeNode[][] grid, List<ProjectNode> projects,
                               double windSpeed, double windDirection, double simulationTime) {
            // Copie profonde de la grille
            this.grid = new TreeNode[grid.length][grid[0].length];
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[0].length; c++) {
                    // Utiliser le constructeur avec 4 arguments en récupérant la taille des cellules
                    double cellWidth = grid[r][c].getWidth();
                    double cellHeight = grid[r][c].getHeight();
                    this.grid[r][c] = new TreeNode(r, c, cellWidth, cellHeight);
                    this.grid[r][c].copyStateFrom(grid[r][c]);
                }
            }

            // Copie de la liste des projets
            this.projects = new ArrayList<>(projects);

            this.windSpeed = windSpeed;
            this.windDirection = windDirection;
            this.simulationTime = simulationTime;
        }

        public TreeNode[][] getGrid() {
            return grid;
        }

        public List<ProjectNode> getProjects() {
            return projects;
        }

        public double getWindSpeed() {
            return windSpeed;
        }

        public double getWindDirection() {
            return windDirection;
        }

        public double getSimulationTime() {
            return simulationTime;
        }
    }
}
