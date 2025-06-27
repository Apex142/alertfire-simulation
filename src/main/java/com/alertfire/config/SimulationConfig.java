package com.alertfire.config;

/**
 * Configuration de la simulation de feu de forêt
 */
public class SimulationConfig {

    // Dimensions de la grille
    private int gridWidth = 20;
    private int gridHeight = 20;
    private double cellSize = 30;
    private double cellSizeKm = 0.1; // Taille d'une cellule en kilomètres (100m par défaut)

    // Paramètres de simulation
    private double stepTime = 0.5;  // secondes par étape en mode manuel
    private double initialWindSpeed = 2.0;  // m/s
    private double initialWindDirection = 45.0;  // degrés (0 = Est, 90 = Nord)
    private double maxWindSpeed = 10.0;  // m/s
    private double humidity = 50.0;  // pourcentage

    // Paramètres de propagation du feu
    private double burnTimeMin = 8.0;  // temps minimum pour qu'un arbre brûle complètement
    private double burnTimeMax = 15.0;  // temps maximum pour qu'un arbre brûle complètement

    // Seuils pour la transmission LoRa
    private double temperatureThreshold = 60.0;  // °C
    private double co2Threshold = 1500.0;        // ppm
    private double transmissionCooldown = 5.0;   // secondes

    // Rayon de détection en cellules (converti en km dans les calculs)
    private double masterDetectionRadius = 10.0; // Rayon de détection pour les projets maîtres
    private double slaveDetectionRadius = 5.0;   // Rayon de détection pour les projets esclaves

    // Paramètres de gestion énergétique
    private double activationInterval = 600.0;   // 10 minutes en secondes
    private double activeTime = 5.0;             // Temps d'activité après activation en secondes

    // URL du backend Flask
    private String backendUrl = "http://localhost:5000/receive-alert";

    // Constructeur par défaut
    public SimulationConfig() {
        // Utilise les valeurs par défaut
    }

    // Getters et setters

    public int getGridWidth() {
        return gridWidth;
    }

    public void setGridWidth(int gridWidth) {
        this.gridWidth = gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }

    public void setGridHeight(int gridHeight) {
        this.gridHeight = gridHeight;
    }

    public double getCellSize() {
        return cellSize;
    }

    public void setCellSize(double cellSize) {
        this.cellSize = cellSize;
    }

    public double getStepTime() {
        return stepTime;
    }

    public void setStepTime(double stepTime) {
        this.stepTime = stepTime;
    }

    public double getInitialWindSpeed() {
        return initialWindSpeed;
    }

    public void setInitialWindSpeed(double initialWindSpeed) {
        this.initialWindSpeed = initialWindSpeed;
    }

    public double getInitialWindDirection() {
        return initialWindDirection;
    }

    public void setInitialWindDirection(double initialWindDirection) {
        this.initialWindDirection = initialWindDirection;
    }

    public double getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public void setMaxWindSpeed(double maxWindSpeed) {
        this.maxWindSpeed = maxWindSpeed;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getBurnTimeMin() {
        return burnTimeMin;
    }

    public void setBurnTimeMin(double burnTimeMin) {
        this.burnTimeMin = burnTimeMin;
    }

    public double getBurnTimeMax() {
        return burnTimeMax;
    }

    public void setBurnTimeMax(double burnTimeMax) {
        this.burnTimeMax = burnTimeMax;
    }

    // Getters et setters pour les seuils de transmission

    public double getTemperatureThreshold() {
        return temperatureThreshold;
    }

    public void setTemperatureThreshold(double temperatureThreshold) {
        this.temperatureThreshold = temperatureThreshold;
    }

    public double getCo2Threshold() {
        return co2Threshold;
    }

    public void setCo2Threshold(double co2Threshold) {
        this.co2Threshold = co2Threshold;
    }

    public double getTransmissionCooldown() {
        return transmissionCooldown;
    }

    public void setTransmissionCooldown(double transmissionCooldown) {
        this.transmissionCooldown = transmissionCooldown;
    }

    public double getCellSizeKm() {
        return cellSizeKm;
    }

    public void setCellSizeKm(double cellSizeKm) {
        this.cellSizeKm = cellSizeKm;
    }

    public double getMasterDetectionRadius() {
        return masterDetectionRadius;
    }

    public void setMasterDetectionRadius(double masterDetectionRadius) {
        this.masterDetectionRadius = masterDetectionRadius;
    }

    public double getSlaveDetectionRadius() {
        return slaveDetectionRadius;
    }

    public void setSlaveDetectionRadius(double slaveDetectionRadius) {
        this.slaveDetectionRadius = slaveDetectionRadius;
    }

    public double getActivationInterval() {
        return activationInterval;
    }

    public void setActivationInterval(double activationInterval) {
        this.activationInterval = activationInterval;
    }

    public double getActiveTime() {
        return activeTime;
    }

    public void setActiveTime(double activeTime) {
        this.activeTime = activeTime;
    }

    public String getBackendUrl() {
        return backendUrl;
    }

    public void setBackendUrl(String backendUrl) {
        this.backendUrl = backendUrl;
    }

    /**
     * Calcule le temps de combustion pour un arbre en fonction de son humidité
     * @param humidity Niveau d'humidité de l'arbre (0-100)
     * @return Temps de combustion en secondes
     */
    public double calculateBurnTime(double humidity) {
        // Plus l'humidité est élevée, plus le temps de combustion est long
        double humidityFactor = humidity / 100.0;
        return burnTimeMin + (burnTimeMax - burnTimeMin) * humidityFactor;
    }
}
