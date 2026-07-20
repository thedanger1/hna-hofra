# Hna Hofra — هنا حفرة

Application Android citoyenne pour **signaler les nids-de-poule de la ville de
Safi (Maroc)**. Tout le monde partage la même carte, en temps réel.

## Fonctionnement

- **Accueil** bilingue **FR / العربية** (bascule en un bouton), logo `هنا حفرة`,
  bouton *Réclamer un nid-de-poule*, mention *developped by MO-CH*.
- **Réclamation** : nom, état (*Nid-de-poule* / *Réparé*), date de découverte,
  emplacement choisi sur une carte **verrouillée sur Safi** (ou « Ma position »),
  puis **photo** du trou et envoi.
- **Carte** : chaque trou est un marqueur — ⚠️ danger (non réparé) ou ✅ (réparé).
  En touchant un marqueur : photo, état, nom du déclarant et **date la plus
  récente**. Un trou non réparé peut être passé à **« Réparé »** par n'importe qui
  (nouvelle photo + nom).
- Les trous **réparés depuis plus de 10 jours** disparaissent automatiquement ;
  seuls les trous non réparés restent visibles.

## Technique

- Kotlin + Jetpack Compose (Material 3), `minSdk` 26.
- **Google Maps** (maps-compose) restreint à Safi.
- **Supabase** (Postgres via API REST) pour les données partagées (positions,
  états, dates). Carte rafraîchie toutes les ~12 s.
- **imgbb** pour l'hébergement des photos (offre gratuite, conservation 6 mois).
- Aucune connexion : identification par simple **nom**.

## Build (sans outil Android local)

Le build se fait dans le cloud via **GitHub Actions**. À chaque `push`, le workflow
*Build Hna Hofra APK* génère `app-debug.apk` (onglet **Actions > Artifacts**).

## Configuration obligatoire

Renseignez les 3 clés (Google Maps, imgbb, Supabase) dans
`app/src/main/res/values/secrets.xml`. Voir **[SETUP.md](SETUP.md)** — pas à pas.

---
developped by MO-CH
