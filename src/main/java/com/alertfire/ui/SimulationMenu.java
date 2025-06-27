package com.alertfire.ui;

import com.alertfire.controller.SimulationController;
import com.alertfire.model.TreeNode;
import com.alertfire.model.enums.NodeState;
import com.alertfire.model.enums.ProjectType;
import com.alertfire.simulation.PropagationFactory;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Interface utilisateur pour contrôler la simulation
 */
public class SimulationMenu {

    private SimulationController controller;
    private BorderPane root;
    private GridView gridView;
    private Label statusLabel;
    private ToggleGroup toolGroup;
    private NodeState currentAction = NodeState.EMPTY;
    private ProjectType projectType = ProjectType.SLAVE;
    private boolean placingProject = false;

    public SimulationMenu(SimulationController controller) {
        this.controller = controller;
        this.gridView = controller.getGridView();

        root = new BorderPane();

        // Créer les barres d'outils
        VBox toolbarContainer = new VBox(10);
        toolbarContainer.setPadding(new Insets(10));

        // Barre d'outils principale
        ToolBar mainToolbar = createMainToolbar();

        // Barre d'outils pour les actions sur les cellules
        ToolBar cellToolbar = createCellToolbar();

        // Barre d'outils pour les conditions météo
        ToolBar weatherToolbar = createWeatherToolbar();

        // Barre d'état
        statusLabel = new Label("Prêt");
        HBox statusBar = new HBox(statusLabel);
        statusBar.setPadding(new Insets(5));

        toolbarContainer.getChildren().addAll(mainToolbar, cellToolbar, weatherToolbar);
        root.setTop(toolbarContainer);

        // Ajouter la grille au centre
        root.setCenter(gridView.getGridPane());

        root.setBottom(statusBar);

        // Configurer les événements de clic sur la grille
        setupGridClickEvents();
    }

    /**
     * Configure les événements de clic sur la grille
     */
    private void setupGridClickEvents() {
        // Récupérer toutes les cellules de la grille
        TreeNode[][] grid = gridView.getGrid();

        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                TreeNode node = grid[row][col];
                final int r = row;
                final int c = col;

                // Ajouter un gestionnaire d'événements de clic pour chaque cellule
                node.setOnMouseClicked(e -> {
                    if (placingProject) {
                        // Placement d'un projet (maître ou esclave)
                        controller.addProject(r, c, projectType);
                        updateStatus("Projet " + projectType + " ajouté en position (" + r + "," + c + ")");
                    } else {
                        // Modification de l'état de la cellule
                        switch (currentAction) {
                            case EMPTY:
                                node.setState(NodeState.EMPTY);
                                updateStatus("Cellule vidée en position (" + r + "," + c + ")");
                                break;
                            case TREE:
                                node.setState(NodeState.TREE);
                                updateStatus("Arbre ajouté en position (" + r + "," + c + ")");
                                break;
                            case BURNING:
                                if (node.getState() == NodeState.TREE) {
                                    controller.startFire(r, c);
                                    updateStatus("Feu démarré en position (" + r + "," + c + ")");
                                } else {
                                    updateStatus("Impossible de démarrer un feu ici - besoin d'un arbre");
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        }
    }

    /**
     * Crée la barre d'outils principale avec les boutons de contrôle de simulation
     */
    private ToolBar createMainToolbar() {
        ToolBar toolbar = new ToolBar();

        Button playBtn = new Button("▶ Démarrer");
        playBtn.setOnAction(e -> {
            controller.startSimulation();
            updateStatus("Simulation en cours...");
        });

        Button pauseBtn = new Button("⏸ Pause");
        pauseBtn.setOnAction(e -> {
            controller.pauseSimulation();
            updateStatus("Simulation en pause");
        });

        Button stopBtn = new Button("⏹ Arrêter");
        stopBtn.setOnAction(e -> {
            controller.stopSimulation();
            updateStatus("Simulation arrêtée");
        });

        Button stepBtn = new Button("⏭ Étape");
        stepBtn.setOnAction(e -> {
            controller.stepSimulation();
            updateStatus("Étape unique exécutée");
        });

        Button resetBtn = new Button("↺ Réinitialiser");
        resetBtn.setOnAction(e -> {
            controller.resetSimulation();
            updateStatus("Simulation réinitialisée");
        });

        // Slider pour la densité de la forêt
        Label densityLabel = new Label("Densité de la forêt:");
        Slider densitySlider = new Slider(0, 100, 50);
        densitySlider.setShowTickLabels(true);
        densitySlider.setShowTickMarks(true);

        Button generateBtn = new Button("Générer forêt");
        generateBtn.setOnAction(e -> {
            controller.generateRandomForest(densitySlider.getValue() / 100.0);
            updateStatus("Forêt générée avec densité " + (int)densitySlider.getValue() + "%");
        });

        // Choix de la stratégie de propagation
        Label strategyLabel = new Label("Stratégie:");
        ComboBox<String> strategyCombo = new ComboBox<>();
        strategyCombo.getItems().addAll("SLOW", "FAST");
        strategyCombo.setValue("SLOW");
        strategyCombo.setOnAction(e -> {
            controller.setPropagationStrategy(
                    PropagationFactory.createStrategy(strategyCombo.getValue())
            );
            updateStatus("Stratégie de propagation: " + strategyCombo.getValue() +
                    (strategyCombo.getValue().equals("FAST") ?
                            " (propagation étendue, combustion rapide)" :
                            " (propagation limitée, combustion lente)"));
        });

        toolbar.getItems().addAll(
                playBtn, pauseBtn, stopBtn, stepBtn, resetBtn,
                new Separator(),
                densityLabel, densitySlider, generateBtn,
                new Separator(),
                strategyLabel, strategyCombo
        );

        return toolbar;
    }

    /**
     * Crée la barre d'outils pour les actions sur les cellules
     */
    private ToolBar createCellToolbar() {
        ToolBar toolbar = new ToolBar();
        toolGroup = new ToggleGroup();

        // Bouton pour placer des cellules vides
        ToggleButton emptyBtn = createToolButton("Vide", NodeState.EMPTY, Color.LIGHTGRAY);
        emptyBtn.setSelected(true); // Sélectionné par défaut

        // Bouton pour placer des arbres
        ToggleButton treeBtn = createToolButton("Arbre", NodeState.TREE, Color.GREEN);

        // Bouton pour démarrer un feu
        ToggleButton fireBtn = createToolButton("Feu", NodeState.BURNING, Color.RED);

        // Bouton pour placer un projet esclave
        ToggleButton slaveBtn = new ToggleButton("Projet esclave");
        slaveBtn.setToggleGroup(toolGroup);
        slaveBtn.setOnAction(e -> {
            placingProject = true;
            projectType = ProjectType.SLAVE;
            updateStatus("Placement d'un projet esclave");
        });

        // Bouton pour placer un projet maître
        ToggleButton masterBtn = new ToggleButton("Projet maître");
        masterBtn.setToggleGroup(toolGroup);
        masterBtn.setOnAction(e -> {
            placingProject = true;
            projectType = ProjectType.MASTER;
            updateStatus("Placement d'un projet maître");
        });

        toolbar.getItems().addAll(
                emptyBtn, treeBtn, fireBtn,
                new Separator(),
                slaveBtn, masterBtn
        );

        return toolbar;
    }

    /**
     * Crée la barre d'outils pour les conditions météorologiques
     */
    private ToolBar createWeatherToolbar() {
        ToolBar toolbar = new ToolBar();

        // Contrôle de la vitesse du vent
        Label windSpeedLabel = new Label("Vitesse du vent (m/s):");
        Slider windSpeedSlider = new Slider(0, 10, controller.getWindSpeed());
        windSpeedSlider.setShowTickLabels(true);
        windSpeedSlider.setShowTickMarks(true);
        windSpeedSlider.setMajorTickUnit(2);
        windSpeedSlider.setBlockIncrement(0.5);

        // Mettre à jour la vitesse du vent quand le slider change
        windSpeedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            controller.setWindSpeed(newVal.doubleValue());
            updateStatus("Vitesse du vent: " + String.format("%.1f", newVal.doubleValue()) + " m/s");
        });

        // Contrôle de la direction du vent
        Label windDirLabel = new Label("Direction du vent (°):");
        Slider windDirSlider = new Slider(0, 359, controller.getWindDirection());
        windDirSlider.setShowTickLabels(true);
        windDirSlider.setShowTickMarks(true);
        windDirSlider.setMajorTickUnit(90);
        windDirSlider.setBlockIncrement(15);

        // Mettre à jour la direction du vent quand le slider change
        windDirSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            controller.setWindDirection(newVal.doubleValue());
            updateStatus("Direction du vent: " + String.format("%.0f", newVal.doubleValue()) + "°");
        });

        // Bouton pour revenir à l'état précédent
        Button backButton = new Button("↩ Retour arrière");
        backButton.setOnAction(e -> {
            controller.goBack();
            updateStatus("Retour à l'état précédent");
        });

        toolbar.getItems().addAll(
                windSpeedLabel, windSpeedSlider,
                new Separator(),
                windDirLabel, windDirSlider,
                new Separator(),
                backButton
        );

        return toolbar;
    }

    /**
     * Crée un bouton d'outil avec une couleur indicative
     */
    private ToggleButton createToolButton(String text, NodeState action, Color color) {
        ToggleButton button = new ToggleButton(text);
        button.setToggleGroup(toolGroup);

        // Indicateur visuel de la couleur
        Rectangle colorRect = new Rectangle(10, 10, color);
        colorRect.setStroke(Color.BLACK);

        button.setGraphic(colorRect);
        button.setOnAction(e -> {
            currentAction = action;
            placingProject = false;
            updateStatus("Action sélectionnée: " + text);
        });

        return button;
    }

    /**
     * Met à jour le message de statut
     */
    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Retourne le conteneur racine de l'interface utilisateur
     */
    public BorderPane getRoot() {
        return root;
    }

    /**
     * Obtient l'action actuellement sélectionnée
     */
    public NodeState getCurrentAction() {
        return currentAction;
    }

    /**
     * Vérifie si l'utilisateur place un projet
     */
    public boolean isPlacingProject() {
        return placingProject;
    }

    /**
     * Obtient le type de projet actuellement sélectionné
     */
    public ProjectType getProjectType() {
        return projectType;
    }
}
