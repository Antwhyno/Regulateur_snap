systeme exploitation: Androide One UI

Apres choix du langage kotlin:

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