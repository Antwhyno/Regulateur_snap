package com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class SpotlightBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return

        // 🛡️ SÉCURITÉ 1 : Le bouclier Caméra
        // Si on détecte les boutons de l'appareil photo visibles sur ta vidéo, on bloque le script de secours !
        if (isCameraScreenActive(rootNode)) {
            return
        }

        // 🎯 SÉCURITÉ 2 : Le ciblage chirurgical
        // On ne déclenche l'action que si l'onglet Spotlight lui-même est activé
        if (isSpotlightTabSelected(rootNode)) {
            Toast.makeText(applicationContext, "Spotlight bloqué ! Retour au travail 🚀", Toast.LENGTH_SHORT).show()
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    // Détecte si on est sur l'appareil photo grâce aux textes exclusifs à cet écran (vus sur ton enregistrement)
    private fun isCameraScreenActive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        val text = node.text?.toString() ?: ""
        if (text.contains("Flash") || text.contains("Sons") || text.contains("Mode HD") || text.contains("selfies")) {
            return true // On est sur la caméra, on arrête tout traitement de blocage
        }

        for (i in 0 until node.childCount) {
            if (isCameraScreenActive(node.getChild(i))) {
                return true
            }
        }
        return false
    }

    // Analyse si l'utilisateur a précisément cliqué et activé l'onglet Spotlight
    private fun isSpotlightTabSelected(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        val text = node.text?.toString()?.lowercase() ?: ""
        val description = node.contentDescription?.toString()?.lowercase() ?: ""

        // On vérifie que le mot "spotlight" est présent et que l'élément est marqué comme sélectionné.
        // L'astuce NSI : "node.childCount == 0" garantit qu'on cible le bouton final, et pas la barre entière.
        if (description.contains("spotlight") || text.contains("spotlight")) {
            if (node.isSelected && node.childCount == 0) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isSpotlightTabSelected(node.getChild(i))) {
                return true
            }
        }
        return false
    }

    override fun onInterrupt() {}
}