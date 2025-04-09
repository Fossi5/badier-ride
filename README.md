Badier Ride - Application de Gestion des Livraisons
Description
Badier Ride est une application de gestion des livraisons qui permet aux administrateurs,
répartiteurs et chauffeurs de gérer efficacement les tournées, les points de livraison et les adresses.
L'application utilise Spring Boot pour le backend et MySQL comme base de données.

Fonctionnalités
Authentification et Autorisation :

Gestion des utilisateurs avec rôles (ADMIN, DISPATCHER, DRIVER).
Sécurisation des endpoints avec Spring Security et JWT.
Gestion des Adresses :
CRUD (Créer, Lire, Mettre à jour, Supprimer) pour les adresses.

Gestion des Points de Livraison :
Ajout, modification et suppression des points de livraison.

Gestion des Tournées :
Création et mise à jour des tournées.
Attribution des chauffeurs et répartiteurs.
Mise à jour de l'ordre des points de livraison.

Prérequis
Avant de commencer, assurez-vous d'avoir les éléments suivants installés sur votre machine :

Java 21 ou une version compatible.
Maven (gestionnaire de dépendances).
MySQL (base de données).
Un outil comme Postman ou cURL pour tester les API.

Installation
1. Clonez le dépôt
git clone https://github.com/badier/badier-ride.git
cd badier-ride
2. Configurez la base de données
Créez une base de données MySQL :
CREATE DATABASE badier_ride;

Mettez à jour les informations de connexion dans le fichier application.properties :
spring.datasource.url=jdbc:mysql://localhost:3306/badier_ride
spring.datasource.username=VOTRE_UTILISATEUR
spring.datasource.password=VOTRE_MOT_DE_PASSE
spring.jpa.hibernate.ddl-auto=update

3. Installez les dépendances
Exécutez la commande suivante pour télécharger les dépendances Maven :
mvn clean install

4. Lancez l'application
Démarrez l'application avec Maven :
mvn spring-boot:run

L'application sera disponible à l'adresse suivante :
http://localhost:8080

Endpoints API
Authentification
POST /api/auth/login : Authentification et génération de JWT.
POST /api/auth/register : Inscription d'un nouvel utilisateur.

Gestion des Adresses
GET /api/addresses : Récupérer toutes les adresses.
POST /api/addresses : Ajouter une nouvelle adresse.
PUT /api/addresses/{id} : Mettre à jour une adresse.
DELETE /api/addresses/{id} : Supprimer une adresse.

Gestion des Points de Livraison
GET /api/delivery-points : Récupérer tous les points de livraison.
POST /api/delivery-points : Ajouter un point de livraison.
PUT /api/delivery-points/{id} : Mettre à jour un point de livraison.
DELETE /api/delivery-points/{id} : Supprimer un point de livraison.

Gestion des Tournées
GET /api/routes : Récupérer toutes les tournées.
POST /api/routes : Créer une nouvelle tournée.
PUT /api/routes/{id} : Mettre à jour une tournée.
DELETE /api/routes/{id} : Supprimer une tournée.
PUT /api/routes/{routeId}/delivery-points/order : Mettre à jour l'ordre des points de livraison.

Sécurité
L'application utilise Spring Security et JWT pour sécuriser les endpoints :

Les rôles disponibles sont :
ADMIN : Accès complet à toutes les fonctionnalités.
DISPATCHER : Gestion des tournées et des points de livraison.
DRIVER : Accès limité aux tournées qui leur sont assignées.

Structure du Projet
Voici un aperçu de la structure du projet :
src/main/java/com/badier/badier_ride
├── controller      # Contrôleurs REST
├── dto             # Objets de transfert de données
├── entity          # Entités JPA
├── enumeration     # Enumérations (statuts, rôles, etc.)
├── repository      # Interfaces pour accéder à la base de données
├── service         # Logique métier
├── security        # Configuration de la sécurité
└── BadierRideApplication.java # Classe principale

Technologies Utilisées
Backend : Spring Boot (Spring Data JPA, Spring Security, Spring Web).
Base de Données : MySQL.
Authentification : JWT (JSON Web Tokens).
Validation : Hibernate Validator.
Logging : SLF4J et Logback.
