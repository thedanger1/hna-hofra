# Configuration de Hna Hofra (هنا حفرة)

L'application a besoin de **3 clés** que vous seul pouvez créer (comptes gratuits).
Il faut ensuite les coller dans **un seul fichier** :
`app/src/main/res/values/secrets.xml`.

Tant que les clés ne sont pas renseignées, l'app se lance mais affiche un bandeau
« Configuration manquante » et ne peut pas enregistrer de signalements.

---

## 1) Clé Google Maps  → `maps_api_key`

1. Ouvrez https://console.cloud.google.com/ et créez (ou choisissez) un projet.
2. Menu **APIs & Services > Library** : activez **Maps SDK for Android**.
3. Menu **APIs & Services > Credentials > Create credentials > API key**.
4. Copiez la clé et collez-la dans `secrets.xml` à la place de `METTRE_CLE_GOOGLE_MAPS`.

> **Restriction (facultatif mais recommandé).** Pour éviter tout abus, restreignez
> la clé à « Android apps » avec :
> - Package : `com.hnahofra.app`
> - SHA-1 : lancez le workflow **Generate keystore** (onglet Actions), copiez le
>   SHA-1 affiché, placez le fichier `hnahofra.jks` téléchargé dans `keystore/` et
>   commitez-le. Sinon, laissez la clé **sans restriction** pour un test rapide.

## 2) Clé imgbb  → `imgbb_api_key`

1. Créez un compte sur https://imgbb.com/ (gratuit).
2. Allez sur https://api.imgbb.com/ et cliquez **Get API key**.
3. Copiez la clé dans `secrets.xml` à la place de `METTRE_CLE_IMGBB`.

> Les photos sont conservées **6 mois** (limite gratuite d'imgbb), configuré
> automatiquement par l'app.

## 3) Firebase Firestore  → `firebase_project_id`, `firebase_app_id`, `firebase_api_key`

1. Ouvrez https://console.firebase.google.com/ et **créez un projet** (gratuit,
   plan Spark).
2. Dans le projet : **Build > Firestore Database > Create database**
   → démarrez en mode **production** → région `europe-west` (par ex.).
3. Onglet **Règles** : collez le contenu du fichier [`firestore.rules`](firestore.rules)
   puis **Publier**.
4. Récupérez les 3 valeurs : icône ⚙️ **Paramètres du projet > Général**, puis
   **Vos applications > Ajouter une application > Web** (icône `</>`).
   Donnez un surnom, validez. Firebase affiche un bloc `firebaseConfig` :
   ```js
   const firebaseConfig = {
     apiKey: "AIza........",        // -> firebase_api_key
     projectId: "mon-projet-1234", // -> firebase_project_id
     appId: "1:12345:web:abcdef",  // -> firebase_app_id
     ...
   };
   ```
5. Reportez ces 3 valeurs dans `secrets.xml`.

> Pas besoin de `google-services.json` : l'app initialise Firebase avec ces 3
> valeurs directement.

---

## Après avoir rempli `secrets.xml`

1. Enregistrez le fichier et **commitez / poussez** sur GitHub.
2. Le workflow **Build Hna Hofra APK** se relance et produit l'APK.
3. Onglet **Actions** > dernier build > section **Artifacts** > téléchargez
   `hna-hofra-debug-apk` > installez `app-debug.apk` sur le téléphone.

Toutes les personnes qui installent l'APK partagent la **même carte** en temps réel.
