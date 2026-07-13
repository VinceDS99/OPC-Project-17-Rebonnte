# Rebonnté 💊

Application Android de gestion de stock médicaments pour le pôle Supply Chain du groupe pharmaceutique Rebonnté.

## Aperçu

Rebonnté permet aux opérateurs du pôle Supply Chain de gérer en temps réel les stocks de médicaments par rayon, avec un historique complet et fiable de chaque modification.

## Fonctionnalités

- **Authentification** : création de compte et connexion via Firebase Auth (email + mot de passe)
- **Gestion des rayons** : consultation et ajout de rayons de stockage
- **Gestion des médicaments** : consultation par rayon, ajout via formulaire dédié, suppression avec confirmation
- **Gestion des stocks** : incrémentation/décrémentation avec validation (pas de stock négatif)
- **Historique** : chaque modification de stock est enregistrée avec l'email de l'opérateur, la date et le détail de l'action, affichée du plus récent au plus ancien
- **Tri et filtrage** : effectués via les requêtes natives Firestore (`orderBy`, `startAt`/`endAt`), pas de traitement local
- **Pagination** : chargement progressif des médicaments (`.limit()` + scroll infini), pas de chargement de toute la collection d'un coup
- **Temps réel** : synchronisation automatique entre tous les appareils via `addSnapshotListener`
- **Déconnexion** : gestion multi-utilisateurs
- **Gestion d'erreurs** : try-catch systématique sur les opérations Firebase, remontée via `StateFlow<String?>`

## Stack technique

| Technologie | Version |
|---|---|
| Kotlin | 2.1.0 |
| Jetpack Compose | BOM 2024.12.01 |
| Firebase Auth / Firestore | BoM 33.5.0 |
| Hilt | 2.56.2 |
| Android Gradle Plugin | 8.7.0 |
| Gradle | 8.9 |
| compileSdk / targetSdk | 35 |
| minSdk | 24 |

## Architecture — MVVM + Repository
Composable (View)
↕ collectAsState()
ViewModel (logique métier, StateFlow)
↕ Flow<List<T>>
Repository — Interface (contrat testable)
↕ callbackFlow + addSnapshotListener
Repository — Implémentation (Firebase)
↕ Dispatchers.IO
Firebase Firestore / Firebase Auth


### Structure des packages
com.openclassrooms.rebonnte/
├── core/
│   └── AppException.kt              # Hiérarchie d'exceptions métier (Medicine/Aisle/Auth/Network)
├── di/
│   ├── AuthModule.kt                # Binding Hilt — AuthRepositoryInterface
│   └── RepositoryModule.kt          # Binding Hilt — MedicineRepositoryInterface
├── ui/
│   ├── auth/
│   │   ├── AuthUiState.kt           # Idle / Loading / Success / Error
│   │   ├── AuthRepository(Interface).kt
│   │   ├── SessionViewModel.kt      # Session partagée (isLoggedIn, signOut, email)
│   │   ├── LoginActivity.kt + LoginViewModel.kt
│   │   └── RegisterActivity.kt + RegisterViewModel.kt
│   ├── aisle/
│   │   ├── Aisle.kt, AisleRepository.kt, AisleViewModel.kt
│   │   ├── AisleScreen.kt, AisleDetailActivity.kt
│   ├── medicine/
│   │   ├── Medicine.kt, MedicineQueryParams.kt
│   │   ├── MedicineRepository(Interface).kt, MedicineViewModel.kt
│   │   ├── MedicineScreen.kt, MedicineItemComponent.kt
│   │   ├── MedicineDetailActivity.kt, AddMedicineActivity.kt
│   ├── history/
│   │   └── History.kt
│   ├── main/
│   │   ├── MainScreen.kt            # Scaffold + NavHost, extrait de MainActivity
│   │   └── components/
│   │       ├── RebonnteTopBar.kt
│   │       ├── RebonnteBottomBar.kt
│   │       └── EmbeddedSearchBar.kt
│   └── theme/
│       └── Color.kt, Theme.kt, Type.kt
├── MainActivity.kt                  # Point d'entrée post-connexion (~40 lignes)
└── RebonnteApplication.kt           # Point d'entrée Hilt

## Structure Firestore
firestore/
├── aisles/          { name: String }
└── medicines/       {
id: String (auto, non stocké dans le document),
name: String,
nameLower: String,   ← recherche insensible à la casse
stock: Int,
nameAisle: String,
histories: List<History>
}
History: {
medicineName: String,
userId: String (email de l'opérateur),
date: String,
details: String
}

## Prérequis

- Android Studio Hedgehog ou supérieur
- JDK 17
- Compte Firebase avec projet configuré
- Fichier `google-services.json` dans le dossier `app/`

## Installation

### 1. Cloner le repository
```bash
git clone https://github.com/VinceDS99/OPC-Project-17-Rebonnte.git
```

### 2. Configurer Firebase
- Créer un projet sur [Firebase Console](https://console.firebase.google.com)
- Activer **Firebase Authentication** (email/mot de passe)
- Activer **Cloud Firestore**
- Télécharger `google-services.json` et le placer dans `app/`
- Configurer les règles Firestore :
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

### 3. Secrets CI/CD (GitHub Actions)
- `FIREBASE_APP_ID` : dans `google-services.json` → `mobilesdk_app_id`
- `CREDENTIAL_FILE_CONTENT` : clé du service account (Firebase → Project Settings → Service accounts)

### 4. Lancer l'application
Ouvrir dans Android Studio → Sync Gradle → Run.

## Tests

### Tests unitaires
```bash
./gradlew test
```
5 tests couvrant `MedicineViewModel` via un `FakeMedicineRepository` (sans Firebase, sans réseau) :
- Ajout d'un médicament
- Modification du stock + création d'entrée historique
- Suppression d'un médicament
- Filtrage par nom
- Restauration de la liste après effacement du filtre

### Tests instrumentés
```bash
./gradlew connectedAndroidTest
```
4 fichiers de tests UI avec interaction utilisateur réelle :
- `LoginScreenTest`
- `RegisterScreenTest` 
- `MedicineDetailScreenTest`
- `AisleScreenTest` 

## CI/CD

Le pipeline GitHub Actions (`.github/workflows/ci.yml`) s'exécute à chaque push sur `main`/`develop` :

1. **Job 1 — Unit Tests** : exécute `./gradlew test`, upload des rapports
2. **Job 2 — Build & Distribution** (si Job 1 réussi) : compile l'APK et le déploie sur Firebase App Distribution

## Compte de démonstration
Email    : johndoe@hotmail.com
Password : Test123


