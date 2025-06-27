package com.alertfire.device;

import com.alertfire.model.enums.ProjectType;
import com.alertfire.communication.LoRaTransmitter;
import com.alertfire.model.LoRaMessage;
import javafx.animation.FadeTransition;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.Random;
import java.util.UUID;

/**
 * Représente un projet de prévention des incendies (maître ou esclave)
 */
public class ProjectNode extends Circle {

    private ProjectType type;
    private int row;
    private int col;
    private double detectionRadius;
    private LoRaTransmitter transmitter;
    private UUID uuid;
    private static int nextNodeId = 1;

    // Gestion de l'énergie
    private double lastActivationTime = 0;
    private boolean isActive = false;
    private double lastTransmissionTime = 0;
    private double activationTimeRemaining = 0;

    // Génération de données capteurs
    private Random random = new Random();
    private double temperature = 25.0; // température ambiante par défaut
    private double co2Level = 400.0;   // niveau de CO2 ambiant par défaut

    // UUIDs prédéfinis pour les projets
    private static final UUID[] MASTER_UUIDS = {
            UUID.fromString("c0e855b8-a65f-4bc4-bc1d-d5f4d592fa1b"),
            UUID.fromString("1d8f2306-4c27-41b0-8921-607b313749a9"),
            UUID.fromString("3c6e07ae-4ce1-4c24-bf67-a7da01a09b05")
    };

    private static final UUID[] SLAVE_UUIDS = {
            UUID.fromString("bc005016-1edf-4a47-a3b9-74de75ab7e97"),
            UUID.fromString("49e0d82b-a259-4c2a-9aff-36d933d31db3"),
            UUID.fromString("1b48a1a0-5229-433f-b83e-213ec81acc5f"),
            UUID.fromString("a5f29634-b400-4724-a791-9b4a0d05b13b")
    };

    private static int masterUuidIndex = 0;
    private static int slaveUuidIndex = 0;

    public ProjectNode(int row, int col, double cellSize, ProjectType type) {
        super(cellSize / 2);
        this.row = row;
        this.col = col;
        this.type = type;

        // Attribuer un UUID à partir des tableaux prédéfinis
        if (type == ProjectType.MASTER) {
            if (masterUuidIndex < MASTER_UUIDS.length) {
                this.uuid = MASTER_UUIDS[masterUuidIndex++];
            } else {
                this.uuid = UUID.randomUUID(); // Fallback si tous les UUIDs maîtres sont utilisés
            }
            setFill(Color.PINK);
            detectionRadius = 10; // 10 cellules de rayon pour un maître
        } else {
            if (slaveUuidIndex < SLAVE_UUIDS.length) {
                this.uuid = SLAVE_UUIDS[slaveUuidIndex++];
            } else {
                this.uuid = UUID.randomUUID(); // Fallback si tous les UUIDs esclaves sont utilisés
            }
            setFill(Color.LIGHTBLUE);
            detectionRadius = 5;  // 5 cellules de rayon pour un esclave
        }

        setStroke(Color.BLACK);
        setStrokeWidth(1.0);

        // Créer un transmetteur LoRa pour ce nœud
        transmitter = new LoRaTransmitter();
    }

    public ProjectType getType() {
        return type;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public double getDetectionRadius() {
        return detectionRadius;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Met à jour l'état énergétique du projet et ses capteurs
     * @param elapsedTime Temps écoulé depuis la dernière mise à jour
     * @param cellSizeKm Taille d'une cellule en kilomètres
     */
    public void update(double elapsedTime, double cellSizeKm) {
        // Gestion du cycle d'activation (toutes les 10 minutes)
        lastActivationTime += elapsedTime;

        // Si le projet est actif, décrémenter le temps d'activité restant
        if (isActive) {
            activationTimeRemaining -= elapsedTime;

            if (activationTimeRemaining <= 0) {
                isActive = false;
                setFill(type == ProjectType.MASTER ? Color.PINK : Color.LIGHTBLUE);
            }
        }
        // Si le projet n'est pas actif, vérifier s'il doit s'activer
        else if (lastActivationTime >= 600.0) { // 10 minutes = 600 secondes
            isActive = true;
            lastActivationTime = 0;
            activationTimeRemaining = 5.0; // Actif pendant 5 secondes

            // Changer la couleur pour indiquer l'activation
            Color activeColor = type == ProjectType.MASTER ?
                    Color.rgb(255, 150, 200) : // Rose plus vif pour maître
                    Color.rgb(150, 200, 255);  // Bleu plus vif pour esclave
            setFill(activeColor);
        }
    }

    /**
     * Détecte un incendie dans son rayon et envoie un message LoRa si nécessaire
     * @param fireGrid Grille indiquant la présence de feu
     * @param ambientTemperature Température ambiante
     * @param humidity Humidité ambiante
     * @param simulationTime Temps total de simulation
     * @param cellSizeKm Taille d'une cellule en kilomètres
     */
    public void detectAndReport(boolean[][] fireGrid, double ambientTemperature, double humidity,
                                double simulationTime, double cellSizeKm) {
        // Si c'est un esclave et qu'il n'est pas actif, ne rien faire
        if (type == ProjectType.SLAVE && !isActive) {
            return;
        }

        boolean fireDetected = false;
        double actualRadius = detectionRadius * cellSizeKm; // Convertir en km réels

        // Mettre à jour les données des capteurs
        updateSensorData(fireGrid, ambientTemperature, humidity);

        // Vérifier s'il y a un feu dans le rayon de détection
        for (int r = Math.max(0, row - (int)detectionRadius); r <= Math.min(fireGrid.length - 1, row + (int)detectionRadius); r++) {
            for (int c = Math.max(0, col - (int)detectionRadius); c <= Math.min(fireGrid[0].length - 1, col + (int)detectionRadius); c++) {
                // Calculer la distance entre ce point et le projet
                double distance = Math.sqrt(Math.pow(r - row, 2) + Math.pow(c - col, 2)) * cellSizeKm;

                // Si un feu est détecté dans le rayon
                if (distance <= actualRadius && fireGrid[r][c]) {
                    fireDetected = true;
                    // Augmenter la température et le CO2 en fonction de la proximité du feu
                    double fireInfluence = 1.0 - (distance / actualRadius);
                    temperature += 50 * fireInfluence;
                    co2Level += 1500 * fireInfluence;
                    break;
                }
            }
            if (fireDetected) break;
        }

        // Vérifier les seuils de transmission
        boolean shouldTransmit = (temperature > 60.0 || co2Level > 1500.0) &&
                (simulationTime - lastTransmissionTime > 5.0); // Cooldown de 5 secondes

        if (shouldTransmit) {
            // Créer et envoyer un message LoRa
            LoRaMessage message = new LoRaMessage(uuid, row, col, temperature, co2Level, fireDetected);
            transmitter.sendMessage(message);
            lastTransmissionTime = simulationTime;

            // Effet visuel pour l'envoi LoRa
            visualizeTransmission();
        }
    }

    /**
     * Met à jour les données des capteurs en fonction de l'environnement
     */
    private void updateSensorData(boolean[][] fireGrid, double ambientTemperature, double humidity) {
        // Régression lente vers la température ambiante
        temperature = temperature * 0.9 + ambientTemperature * 0.1;

        // Régression lente vers le niveau de CO2 ambiant (400 ppm)
        co2Level = co2Level * 0.9 + 400.0 * 0.1;

        // Ajout de légères variations aléatoires
        temperature += (random.nextDouble() - 0.5) * 0.5;
        co2Level += (random.nextDouble() - 0.5) * 10;
    }

    /**
     * Crée un effet visuel pour représenter la transmission LoRa
     */
    private void visualizeTransmission() {
        // Créer un effet de halo
        Glow glow = new Glow();
        glow.setLevel(0.8);
        setEffect(glow);

        // Animation de clignotement
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), this);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.3);
        fadeTransition.setCycleCount(2);
        fadeTransition.setAutoReverse(true);
        fadeTransition.setOnFinished(e -> setEffect(null));
        fadeTransition.play();
    }

    /**
     * Retourne la température actuelle mesurée
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * Retourne le niveau de CO2 actuel mesuré
     */
    public double getCO2Level() {
        return co2Level;
    }

    /**
     * Vérifie si le projet est actuellement actif
     */
    public boolean isActive() {
        return isActive;
    }
}
