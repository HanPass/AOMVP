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
- `GET /api/v1/notification-preferences` : liste les préférences de notification.
- `POST /api/v1/notification-preferences` : crée/met à jour une préférence par email.
- `DELETE /api/v1/notification-preferences/{id}` : supprime une préférence.

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
- Journal de fin de job avec compteurs (`fetched`, `enriched`, `inserted`, `duplicates_or_ignored`, `errors`).
