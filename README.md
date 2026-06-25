systeme exploitation: Androide One UI

MonApplication/
│
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml       <-- La carte d'identité de l'application
│   │   │   │
│   │   │   ├── java/com/monprojet/       <-- Le cerveau (ton code source)
│   │   │   │   ├── MainActivity.kt        <-- L'écran d'accueil
│   │   │   │   └── BlockService.kt       <-- Ton service d'accessibilité
│   │   │   │
│   │   │   └── res/                      <-- Le visuel (Ressources)
│   │   │       ├── layout/activity_main.xml <-- Le design de ton interface
│   │   │       └── xml/accessibility_config.xml <-- La configuration du bloqueur