# 🔥 AlertFire Simulation — JavaFX

**Simulation réaliste d’un réseau de capteurs en forêt pour la détection et la propagation de feux, dans le cadre du projet AlertFire.**

> Développée intégralement par **Mohamed Toujad**  
> Supervision technique et architecturale : **Martin Mehdi**

---

## 🧠 Contexte du projet

**AlertFire** est un système distribué de prévention des incendies de forêt, reposant sur :

- Des capteurs simulés (température, fumée, flamme)
- Un réseau LoRa maître/esclave
- Une intelligence artificielle embarquée
- Une visualisation web en temps réel

Or, aucun feu réel ne pouvant être allumé, **la simulation JavaFX** a été développée pour reproduire virtuellement l’environnement forestier, le réseau capteur, et la dynamique de propagation.

---

## 🎯 Objectifs de la simulation

- Représenter une **forêt virtuelle dynamique**
- Simuler la **propagation d’un feu de forêt** selon des paramètres physiques (vent, densité)
- Générer des **réactions de capteurs** (projets maîtres/esclaves) comme dans un vrai réseau LoRa
- Valider le **comportement des alertes** et la structure du réseau avant tout déploiement

---

## 🧑‍💻 Réalisateur principal

> 👨‍💻 **Mohamed Toujad**
> - Conception de l’architecture logicielle Java
> - Implémentation complète de la simulation
> - Design de l’interface JavaFX
> - Intégration des stratégies de propagation
> - Communication LoRa simulée

> 🧭 **Martin Mehdi**
> - Supervision technique
> - Validation des choix architecturaux
> - Coordination avec les modules IA et backend

---

## 🖼️ Aperçu de l’interface

L'interface JavaFX offre une grille interactive représentant :

| Élément           | Couleur/forme |
|-------------------|---------------|
| Vide               | Gris          |
| 🌲 Arbre          | Vert          |
| 🔥 Feu            | Rouge         |
| 🎗️ Projet maître | **Rose**      |
| 📘 Projet esclave | **Bleu**      |

---

## ⚙️ Fonctionnalités principales

### 🔁 Propagation du feu
- Propagation selon la **densité**, **type de stratégie**, **vent** (direction, force)
- Deux stratégies implémentées :
    - `SlowPropagationStrategy` : propagation prudente
    - `FastPropagationStrategy` : propagation agressive

### 🛰 Réseau LoRa simulé
- Transmission **projet esclave → maître**
- Chaque maître peut être connecté à plusieurs esclaves
- Communication en cascade simulée visuellement

### 🧭 Contrôle dynamique
- Choix de la **densité forestière**
- Activation du feu manuellement
- Mise en pause / relance de la simulation
- Modification des conditions climatiques (vent)

---

## 🧱 Structure technique

### 📦 Architecture orientée design patterns
- **Strategy** : pour la propagation
- **Factory** : pour créer dynamiquement la stratégie choisie
- **Observer** : pour notifier les composants de changement d’état

### 📁 Fichiers clés

| Fichier                      | Rôle                                                                 |
|-----------------------------|----------------------------------------------------------------------|
| `SimulationMain.java`       | Point d'entrée de la simulation                                      |
| `SimulationController.java` | Gère la boucle principale et les règles de propagation              |
| `ProjectNode.java`          | Représente chaque cellule de la grille (arbre, feu, projet, etc.)   |
| `NodeState.java`            | Enumération des états (VIDE, ARBRE, FEU, MAITRE, ESCLAVE)           |
| `SlowPropagationStrategy.java` | Implémentation lente de propagation                              |
| `FastPropagationStrategy.java` | Implémentation rapide de propagation                             |
| `PropagationFactory.java`   | Fabrique la stratégie en fonction du paramétrage                   |
| `LoRaTransmitter.java`      | Simule l’envoi LoRa d’un esclave                                    |
| `SimulationConfig.java`     | Gère les paramètres globaux (taille, vent, densité, etc.)           |

---

## 📊 Tests effectués

- ✅ Simulation avec différentes densités de végétation
- ✅ Tests avec vent nul, faible, fort, orienté
- ✅ Visualisation correcte des cercles de propagation prédite
- ✅ Communication entre esclaves et maître via LoRa simulé
- ✅ Résilience de la simulation à plusieurs cycles de pause / reprise / reset

---

## 🧩 Intégration dans AlertFire

Cette simulation a permis de :

- **Valider les comportements attendus des capteurs** avant leur modélisation dans Firebase
- **Générer des jeux de données réalistes** pour l'entraînement des modèles IA (détection / propagation)
- **Tester visuellement les effets des conditions environnementales** (vent, densité, topologie réseau)

---

## 🚀 Lancer la simulation

### ✅ Prérequis

- **Java 17+**
- **JavaFX SDK** installé localement

### ▶️ Exécution

```bash
Gradle > Application > run
