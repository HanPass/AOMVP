# AOMVP

Application Spring Boot de collecte d'appels d'offres (AO), avec scraping, persistance et alertes mail.

## Prérequis

- Java 17
- Maven 3.9+
- PostgreSQL (profil `dev`) ou H2 (profil `aws` tel que configuré actuellement)

## Profils

- `dev` : configuration locale orientée PostgreSQL.
- `aws` : configuration orientée environnement distant (actuellement avec fallback H2 en mémoire).

## Configuration via variables d'environnement

Aucun secret n'est stocké dans le dépôt. Les paramètres sensibles sont lus depuis des variables d'environnement.

### Base de données

- `DB_URL` (ex: `jdbc:postgresql://localhost:5432/postgres`)
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_SCHEMA` (optionnel, défaut `ao_mvp`)

### Mail / alerting

- `MAIL_HOST` (défaut `smtp.gmail.com`)
- `MAIL_PORT` (défaut `587`)
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `ALERT_MAIL_FROM` (optionnel, défaut `MAIL_USERNAME`)
- `ALERT_SUBJECT_PREFIX` (optionnel, défaut `[AO]`)
- `ALERT_MAIL_ENABLED` (optionnel, défaut `false`)
- `MAIL_SMTP_AUTH` (optionnel, défaut `true`)
- `MAIL_SMTP_STARTTLS_ENABLE` (optionnel, défaut `true`)

### Scraper

- `SCRAPER_FIXED_DELAY_MS` (optionnel, défaut `30000`)
- `SCRAPER_MAX_ITEMS_PER_RUN` (optionnel, défaut `100`)

## Lancement local

```bash
export DB_URL=jdbc:postgresql://localhost:5432/postgres
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export MAIL_USERNAME=votre_compte_mail
export MAIL_PASSWORD=votre_mot_de_passe_app

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Endpoint principal

- `GET /api/v1/appel-offre/all` : retourne les AO persistés.
- `GET /api/v1/appel-offre/search` : recherche paginée avec filtres (`region`, `domaine`, `organisme`, `typeMarche`, `budgetMin`, `budgetMax`, `dateFrom`, `dateTo`, `page`, `size`).
- `GET /api/v1/notification-preferences` : liste les préférences de notification.
- `POST /api/v1/notification-preferences` : crée/met à jour une préférence par email.
- `DELETE /api/v1/notification-preferences/{id}` : supprime une préférence.
- `GET /api/v1/users` : liste les utilisateurs.
- `POST /api/v1/users` : crée/met à jour un utilisateur par email.
- `DELETE /api/v1/users/{id}` : supprime un utilisateur.
- `POST /api/v1/price-intelligence/estimate` : estimation de budget compétitif à partir de l'historique.

### Exemple de réponse

```json
[
  {
    "reference": "AO-2026-001",
    "objet": "Travaux d'entretien",
    "organisme": "Commune X",
    "lieuExec": "Rabat",
    "datePublication": "2026-01-10",
    "dateLimite": "2026-02-10T10:00:00",
    "urlDetail": "https://www.marchespublics.gov.ma/..."
  }
]
```

## Notes d'exploitation

- Le pipeline actif est : `fetchAll()` → `enrich()` → `ingestIfNew()`.
- L'envoi réel des mails est volontairement désactivé pour le moment dans `EmailServiceImpl`.


## Préférences de notification (PR4)

- Les notifications sont désormais préparées par destinataire selon des préférences stockées (mots-clés, régions, organismes).
- Les mails réels restent désactivés par défaut (`ALERT_MAIL_ENABLED=false`).


## Améliorations PR5

- Matching des préférences plus robuste : gestion des séparateurs `,` `;` et retour ligne, et normalisation des accents/casse.
- Journal de fin de job avec compteurs (`fetched`, `processed`, `enriched`, `inserted`, `duplicates_or_ignored`, `errors`) et limite de traitement par run.


## Module utilisateurs (PR6+)

- Le module utilisateurs fournit une base de gestion des comptes de notification (`email`, `firstName`, `lastName`, `enabled`).
- Ce module prépare la liaison future utilisateurs <-> préférences de notification.


## Gestion qualité données (PR6+)

- Normalisation des AO à l'entrée (référence, texte, URL) avant persistance.
- Validation métier minimale (`REFERENCE_EMPTY`, `OBJET_EMPTY`, `URL_DETAIL_EMPTY/INVALID`, `DATE_INCOHERENT`).
- Les AO invalides sont ignorées avec journalisation des motifs.


## AO Search & Filter API (PR6+)

- Filtres avancés supportés: région, domaine, organisme, type de marché, budget min/max, plage de date de publication.
- Pagination native via `page` et `size`.


## Price Intelligence (PR6+)

- Estimation de prix compétitif basée sur les budgets historiques (`min`, `max`, `moyenne`, `médiane`, `suggestedBudget`).
- Features d'estimation supportées: domaine, type de marché, région, organisme, période de publication, budgetHint.
