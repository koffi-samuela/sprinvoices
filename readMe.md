# SprinVoices 🧾

Application web de **facturation et gestion financière B2B** développée avec Spring Boot.  
Elle couvre l'ensemble du cycle de vie commercial : devis → facture → paiement, avec notifications email automatiques et génération de PDF.

---

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 17 · Spring Boot 3 |
| Sécurité | Spring Security · BCrypt |
| Persistance | Spring Data JPA · Hibernate · Oracle DB |
| Templates | Thymeleaf |
| PDF | iText |
| Emails | JavaMailSender · Spring `@Async` |
| Build | Maven |

---

## Architecture

L'application suit une **architecture MVC en couches strictes** :

```
Controller  →  Service  →  Repository  →  Entity (JPA)
     ↓
  Thymeleaf (vues HTML)
```

Deux espaces séparés par rôle Spring Security :

- `/admin/**` — accessible avec `ROLE_ADMIN`
- `/client/**` — accessible avec `ROLE_CLIENT`
- `/api/**` — accessible aux deux rôles, authentification HTTP Basic

---

## Fonctionnalités

### Factures
- Création avec lignes de produits (quantité × prix unitaire)
- Cycle de statuts : `DRAFT` → `INVOICED` → `PAID`
- Passage de statut via actions dédiées (mark-invoiced, mark-paid)
- Pagination et filtres (par client, par statut)

### Devis
- Création et envoi de devis
- Conversion automatique devis → facture
- Statuts : `DRAFT` → `SENT` → `ACCEPTED` / `REFUSED`

### PDF
- Génération dynamique avec iText
- Aperçu en ligne (nouvel onglet)
- Téléchargement direct

### Emails transactionnels
Tous les emails sont envoyés **de façon asynchrone** (`@Async`) pour ne pas bloquer la requête HTTP.

| Déclencheur | Email envoyé |
|---|---|
| Facture passée en `INVOICED` | Facture disponible + PDF en pièce jointe |
| Facture passée en `PAID` | Confirmation de paiement + PDF en pièce jointe |
| Devis envoyé | Devis disponible dans l'espace client |
| Devis converti en facture | Notification de transformation |
| Envoi manuel depuis l'admin | Renvoi de la facture au client |

### Sécurité
- Authentification par formulaire Spring Security
- Encodage des mots de passe BCrypt
- Réinitialisation de mot de passe par email :
  - Token UUID aléatoire, **expiration 30 minutes**, **usage unique**
  - Lien envoyé par email avec bouton d'action
  - Aucune révélation si l'email existe ou non (protection contre l'énumération)
- CSRF désactivé uniquement sur `/api/**` (endpoints REST stateless)

---

## Installation

### Prérequis
- Java 17+
- Maven 3.8+
- Oracle Database
- Serveur SMTP (ex : Gmail, Mailtrap)

### Configuration

Créez ou modifiez `src/main/resources/application.properties` :

```properties
# Base de données Oracle
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_user
spring.datasource.password=your_password
spring.datasource.driver-class-name=oracle.jdbc.OracleDriver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.database-platform=org.hibernate.dialect.OracleDialect

# Email SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Async (nécessaire pour @Async)
spring.task.execution.pool.core-size=2
```

### Lancement

```bash
# Cloner le projet
git clone https://github.com/your-username/sprinvoices.git
cd sprinvoices

# Compiler et lancer
mvn spring-boot:run
```

L'application est disponible sur `http://localhost:8080`.

---

## Structure du projet

```
src/main/java/com/example/sprinvoices/
├── controller/
│   ├── AuthController.java          # Login, dashboard, reset password
│   ├── AdminController.java         # Espace admin (factures, devis, clients)
│   └── ClientController.java        # Espace client
├── service/
│   ├── InvoiceService.java
│   ├── QuoteService.java
│   ├── CustomerService.java
│   ├── ProductService.java
│   ├── EmailService.java            # Emails HTML asynchrones
│   ├── PdfService.java              # Génération PDF iText
│   └── PasswordResetService.java    # Gestion reset mot de passe
├── models/
│   ├── Invoice.java
│   ├── Quote.java
│   ├── Customer.java
│   ├── Product.java
│   ├── InvoiceRow.java
│   ├── UserAccount.java
│   └── PasswordResetToken.java
├── repository/
│   ├── InvoiceRepository.java
│   ├── QuoteRepository.java
│   ├── UserAccountRepository.java
│   └── PasswordResetTokenRepository.java
└── security/
    ├── SecurityConfig.java
    └── CustomUserDetailsService.java

src/main/resources/
├── templates/
│   ├── login.html
│   ├── forgot-password.html
│   ├── reset-password.html
│   ├── admin/
│   │   ├── invoices/
│   │   └── quotes/
│   └── client/
└── application.properties
```

---

## Flux principal

```
Création devis  →  Envoi devis  →  Conversion en facture
                                          ↓
                                   Envoi facture (email + PDF)
                                          ↓
                                   Marquage payé (email confirmation)
```

---

## Sécurité — Reset mot de passe

```
/forgot-password  →  saisie email
      ↓
Génération token UUID  →  envoi email avec lien (valable 30 min)
      ↓
/reset-password?token=xxx  →  validation token
      ↓
Nouveau mot de passe encodé BCrypt  →  token supprimé
      ↓
Redirection /login
```

---

## Auteur

Développé par **[Votre nom]**  
Projet personnel / formation — 2026