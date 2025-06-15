package com.alertfire;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.alertfire.ui.SimulationMenu;
import com.alertfire.ui.GridView;
import com.alertfire.controller.SimulationController;
import com.alertfire.config.SimulationConfig;
import com.alertfire.simulation.PropagationFactory;
import com.alertfire.simulation.PropagationStrategy;

public class App extends Application {

    private SimulationController controller;

    @Override
    public void start(Stage primaryStage) {
        // Configuration initiale de la simulation
        SimulationConfig config = new SimulationConfig();

        // Créer la stratégie de propagation par défaut
        PropagationStrategy strategy = PropagationFactory.createStrategy("SLOW");

        // Créer la vue de la grille avec une taille de cellule appropriée
        GridView gridView = new GridView(config.getGridWidth(), config.getGridHeight(), config.getCellSize());

        // Créer le contrôleur de simulation
        controller = new SimulationController(gridView, strategy, config);

        // Générer une forêt aléatoire pour le test initial
        gridView.generateRandomForest(0.6); // 60% de densité

        // Créer le menu de simulation
        SimulationMenu menu = new SimulationMenu(controller);

        // Configurer la scène avec une taille suffisante pour afficher toute la grille
        int sceneWidth = config.getGridWidth() * (int)config.getCellSize() + 40;
        int sceneHeight = config.getGridHeight() * (int)config.getCellSize() + 150; // Plus d'espace pour les boutons

        Scene scene = new Scene(menu.getRoot(), sceneWidth, sceneHeight);

        primaryStage.setTitle("AlertFire - Simulation de Feu de Forêt");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Afficher un message de débogage
        System.out.println("Application démarrée - Grille: " + config.getGridWidth() + "x" + config.getGridHeight() +
                ", Taille de cellule: " + config.getCellSize());
    }

    @Override
    public void stop() {
        // Arrêter la simulation lorsque l'application se ferme
        if (controller != null) {
            controller.stopSimulation();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}