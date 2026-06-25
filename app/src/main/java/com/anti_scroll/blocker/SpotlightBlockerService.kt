package com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class SpotlightBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // 1. On récupère le contenu textuel et visuel actuellement affiché à l'écran
        val rootNode: AccessibilityNodeInfo = rootInActiveWindow ?: return

        // 2. On cherche si le mot "Spotlight" est visible à l'écran
        // Note : Sur le Snapchat français, l'onglet s'appelle aussi "Spotlight"
        val nodes = rootNode.findAccessibilityNodeInfosByText("Spotlight")
        
        if (!nodes.isNullOrEmpty()) {
            // 3. Alerte ! Tu as ouvert le Spotlight. 
            // On simule un appui sur le bouton "Retour" du téléphone pour fermer la page
            performGlobalAction(GLOBAL_ACTION_BACK)
            
            // Variante radicale : performGlobalAction(GLOBAL_ACTION_HOME) pour fermer Snapchat
        }
        
        // 4. Nettoyage de la mémoire (Obligatoire sur Android pour éviter les ralentissements)
        rootNode.recycle()
    }

    override fun onInterrupt() {
        // Méthode obligatoire demandée par Android si le service est coupé
    }
}