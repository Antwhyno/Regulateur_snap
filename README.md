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

apres choix du langage kotlin:

SpotlightBlocker/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml             <-- La carte d'identité (Permissions)
│   │       ├── java/com/anti_scroll/blocker/
│   │       │   ├── MainActivity.kt             <-- L'écran d'accueil (Bouton d'activation)
│   │       │   └── SpotlightBlockerService.kt  <-- Le cerveau (Détecteur & Bloqueur)
│   │       └── res/
│   │           ├── layout/
│   │           │   └── activity_main.xml       <-- Le design de ton écran d'accueil
│   │           └── xml/
│   │               └── accessibility_config.xml <-- Le filtre réseau/système (Cible Snapchat)