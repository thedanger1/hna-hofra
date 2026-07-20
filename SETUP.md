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

## 3) Supabase  → `supabase_url`, `supabase_anon_key`

> Supabase remplace Firebase : **gratuit, sans carte bancaire**.

1. Ouvrez https://supabase.com/ → **Sign in** (avec GitHub, c'est le plus simple).
2. **New project** : donnez un nom (ex. `hna-hofra`), un mot de passe de base de
   données (notez-le), une région (ex. `Europe West`). Attendez ~1 min la création.
3. **Créer la table** : menu de gauche **SQL Editor** → **New query** → collez tout
   le contenu du fichier [`supabase_schema.sql`](supabase_schema.sql) → **Run**.
   (Cela crée la table `potholes` et autorise l'accès anonyme.)
4. **Récupérer les 2 valeurs** : menu de gauche ⚙️ **Project Settings > API** (aussi
   appelé *Data API*) :
   - **Project URL** → à coller dans `supabase_url`
     (ressemble à `https://abcdefgh.supabase.co`)
   - **Project API Keys > `anon` `public`** → à coller dans `supabase_anon_key`
     (longue chaîne commençant par `eyJ...`)
5. Reportez ces 2 valeurs dans `secrets.xml` (lignes `supabase_url` et
   `supabase_anon_key`).

> La clé `anon` est publique par nature : la sécurité vient des règles (RLS) du
> fichier SQL, qui autorisent lecture / signalement / réparation mais **pas** la
> suppression.

---

## Après avoir rempli `secrets.xml`

1. Enregistrez le fichier et **commitez / poussez** sur GitHub.
2. Le workflow **Build Hna Hofra APK** se relance et produit l'APK.
3. Onglet **Actions** > dernier build > section **Artifacts** > téléchargez
   `hna-hofra-debug-apk` > installez `app-debug.apk` sur le téléphone.

Toutes les personnes qui installent l'APK partagent la **même carte** en temps réel.
