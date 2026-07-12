# Rebonnté 💊

Application Android de gestion de stock médicaments pour le pôle Supply Chain du groupe pharmaceutique Rebonnté.

## Aperçu

Rebonnté permet aux opérateurs du pôle Supply Chain de gérer en temps réel les stocks de médicaments par rayon,
avec un historique complet de chaque modification.

## Fonctionnalités

- **Authentification** : Création de compte et connexion via Firebase Auth (email + mot de passe)
- **Gestion des rayons** : Consultation et ajout de rayons de stockage
- **Gestion des médicaments** : Consultation par rayon, ajout via formulaire, suppression
- **Gestion des stocks** : Incrémentation et décrémentation du stock avec validation
- **Historique** : Chaque modification de stock est enregistrée avec l'email de l'opérateur, la date et le détail de l'action
- **Temps réel** : Synchronisation automatique entre tous les appareils via Firestore
- **Déconnexion** : Gestion multi-utilisateurs

## Stack technique


- Kotlin(2.1.0) = Langage principal 
- Jetpack Compose | BOM 2024.12.01 | UI déclarative |
- Firebase Auth | BoM 33.5.0 | Authentification |
- Firebase Firestore | BoM 33.5.0 | Base de données temps réel |
- Hilt | 2.51.1 | Injection de dépendances |
- Android Gradle Plugin | 8.7.0 | Build system |
- compileSdk / targetSdk | 35 | SDK Android |
- minSdk | 24 | Android 7.0+ |


## Architecture

Le projet suit le pattern MVVM (Model - View - ViewModel) avec le pattern Repository :

Composable (View)
↕ collectAsState()
ViewModel
↕ Flow<List<T>>
Repository (Interface)
↕ callbackFlow + addSnapshotListener
Firebase Firestore

### Structure des packages
com.openclassrooms.rebonnte/
├── di/
│   └── RepositoryModule.kt          # Module Hilt — binding interface/implémentation
├── ui/
│   ├── auth/
│   │   ├── LoginActivity.kt         # Écran de connexion
│   │   └── RegisterActivity.kt      # Écran de création de compte
│   ├── aisle/
│   │   ├── Aisle.kt                 # Modèle de données
│   │   ├── AisleRepository.kt       # Accès Firestore (collection "aisles")
│   │   ├── AisleViewModel.kt        # Logique métier rayons
│   │   ├── AisleScreen.kt           # Liste des rayons
│   │   └── AisleDetailActivity.kt   # Détail d'un rayon
│   ├── medicine/
│   │   ├── Medicine.kt              # Modèle de données
│   │   ├── MedicineRepository.kt    # Accès Firestore (collection "medicines")
│   │   ├── MedicineRepositoryInterface.kt  # Contrat du Repository
│   │   ├── MedicineViewModel.kt     # Logique métier médicaments
│   │   ├── MedicineScreen.kt        # Liste des médicaments
│   │   ├── MedicineDetailActivity.kt # Détail + stock + historique
│   │   ├── MedicineItemComponent.kt # Composant réutilisable
│   │   └── AddMedicineActivity.kt   # Formulaire d'ajout
│   └── history/
│       └── History.kt               # Modèle d'historique
├── MainActivity.kt                  # Navigation principale (bottom bar)
└── RebonnteApplication.kt           # Point d'entrée Hilt


## Structure Firestore
firestore/
├── aisles/          {name: String}
└── medicines/       {
id: String (auto),
name: String,
stock: Int,
nameAisle: String,
histories: List<History>
}
History: {
medicineName: String,
userId: String (email),
date: String,
details: String
}

## Prérequis

- Android Studio Hedgehog ou supérieur
- JDK 17
- Compte Firebase avec projet configuré
- Fichier `google-services.json` dans le dossier `app/`

## Tests

### Tests unitaires
```bash
./gradlew test
```
5 tests couvrant les flux principaux du `MedicineViewModel` :
- Ajout d'un médicament
- Modification du stock + création d'entrée historique
- Suppression d'un médicament
- Filtrage par nom
- Restauration de la liste après effacement du filtre

### Tests instrumentés
```bash
./gradlew connectedAndroidTest
```

## CI/CD

Le pipeline GitHub Actions (`.github/workflows/ci.yml`) s'exécute à chaque push sur `main` :

1. **Job 1 — Unit Tests** : exécute `./gradlew test`
2. **Job 2 — Build & Distribution** (si Job 1 réussi) : compile l'APK et le déploie sur Firebase App Distribution

## Compte de démonstration

Email    : johndoe@hotmail.com
Password : Test123

## Licence

Projet réalisé dans le cadre de la formation OpenClassrooms — Développeur Android.


