# SprinVoices — Module de Facturation
 
Application web de facturation développée avec Spring Boot, Oracle Database et Thymeleaf.
 
---
 
## Prérequis
 
- Java 17+
- Maven 3.x
- Oracle Database XE (local) ou accès à une instance Oracle
- Un compte Gmail avec la validation en deux étapes activée
---
 
## Installation
 
### 1. Cloner le projet
 
```bash
git clone https://github.com/ton-repo/sprinvoices.git
cd sprinvoices
```
 
### 2. Créer la base de données
 
Connecte-toi à ton instance Oracle et exécute le script :
 
```bash
sqlplus ton_user/ton_mdp@localhost:1521/FREEPDB1 @schema_oracle.sql
```
 
### 3. Configurer `application.properties`
 
Ouvre `src/main/resources/application.properties` et renseigne tes informations :
 
```properties
# Oracle
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1
spring.datasource.username=TON_USER
spring.datasource.password=TON_MOT_DE_PASSE
 
# Mail Gmail
spring.mail.username=TON_EMAIL@gmail.com
spring.mail.password=TON_MOT_DE_PASSE_APPLICATION  # ⚠️ mot de passe d'application Gmail, pas ton vrai mdp
```
 
> **Important :** Le mot de passe mail n'est pas ton mot de passe Gmail habituel.
> Va sur [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords), génère un mot de passe d'application pour "SprinVoices" et colle le code de 16 caractères ici.
 
### 4. Compiler et lancer
 
```bash
mvn clean package
java -jar target/sprinvoices-0.0.1-SNAPSHOT.jar
```
 
L'application tourne sur [http://localhost:8080](http://localhost:8080).
 
---
 
## Connexion
 
| Rôle | Identifiant | Mot de passe |
|------|-------------|--------------|
| Admin | `admin` | `admin123` |
| Client | Email saisi à la création | Mot de passe saisi à la création |
 
> Pour créer d'autres comptes admin, insère directement en base avec un hash BCrypt généré sur [bcrypt-generator.com](https://bcrypt-generator.com).
 
---
 
## Structure du projet
 
```
src/main/java/com/example/sprinvoices/
├── controller/     # Controllers Spring MVC + REST
├── models/         # Entités JPA
├── repository/     # Interfaces Spring Data
├── service/        # Logique métier
├── security/       # Config Spring Security
└── dto/            # Objets de transfert (API REST)
 
src/main/resources/
├── templates/      # Vues Thymeleaf
│   ├── admin/      # Espace administrateur
│   ├── client/     # Espace client
│   └── fragments/  # Sidebar + head réutilisables
└── static/css/     # CSS global
```
 
---
 
## API REST
 
Authentification HTTP Basic Auth (email + mot de passe).
 
```bash
# Liste des factures
curl -u email@client.com:motdepasse http://localhost:8080/api/invoices
 
# Détail d'une facture
curl -u email@client.com:motdepasse http://localhost:8080/api/invoices/1
```
 
---
 
## Stack technique
 
`Spring Boot 4` · `Spring Security` · `Spring Data JPA` · `Oracle DB` · `Thymeleaf` · `iText PDF` · `JavaMailSender` · `Lombok` · `Maven`