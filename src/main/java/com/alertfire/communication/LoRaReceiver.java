package com.alertfire.communication;

import com.alertfire.model.LoRaMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Récepteur global des messages LoRa (Singleton pattern)
 */
public class LoRaReceiver {
    private static LoRaReceiver instance;
    private List<Consumer<LoRaMessage>> listeners = new ArrayList<>();

    /**
     * Constructeur privé pour le singleton
     */
    private LoRaReceiver() {}

    /**
     * Obtient l'instance unique du récepteur
     */
    public static synchronized LoRaReceiver getInstance() {
        if (instance == null) {
            instance = new LoRaReceiver();
        }
        return instance;
    }

    /**
     * Ajoute un écouteur pour les messages LoRa
     * @param listener Fonction à appeler lors de la réception d'un message
     */
    public void addListener(Consumer<LoRaMessage> listener) {
        listeners.add(listener);
    }

    /**
     * Supprime un écouteur
     * @param listener Écouteur à supprimer
     */
    public void removeListener(Consumer<LoRaMessage> listener) {
        listeners.remove(listener);
    }

    /**
     * Reçoit un message LoRa et notifie les écouteurs
     * @param message Message reçu
     */
    public void receiveMessage(LoRaMessage message) {
        for (Consumer<LoRaMessage> listener : listeners) {
            listener.accept(message);
        }
    }
}
