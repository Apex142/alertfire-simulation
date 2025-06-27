package com.alertfire.model;

import java.util.UUID;

/**
 * Représente un message transmis par LoRa entre les projets
 */
public class LoRaMessage {
    private UUID uuid;
    private int row;
    private int col;
    private double temperature;
    private double co2Level;
    private boolean fireDetected;
    private long timestamp;

    /**
     * Crée un nouveau message LoRa
     *
     * @param uuid UUID du projet émetteur
     * @param row Ligne du projet dans la grille
     * @param col Colonne du projet dans la grille
     * @param temperature Température mesurée
     * @param co2Level Niveau de CO2 mesuré
     * @param fireDetected Indique si un feu a été détecté
     */
    public LoRaMessage(UUID uuid, int row, int col, double temperature, double co2Level, boolean fireDetected) {
        this.uuid = uuid;
        this.row = row;
        this.col = col;
        this.temperature = temperature;
        this.co2Level = co2Level;
        this.fireDetected = fireDetected;
        this.timestamp = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getCO2Level() {
        return co2Level;
    }

    public boolean isFireDetected() {
        return fireDetected;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Convertit le message en format JSON pour l'API
     */
    public String toJson() {
        return "{"
                + "\"uuid\":\"" + uuid + "\","
                + "\"temperature\":" + temperature + ","
                + "\"co2_level\":" + co2Level + ","
                + "\"source\":\"simulated\""
                + "}";
    }
}
