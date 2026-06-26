package com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class SpotlightBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        
        // On récupère la hauteur totale de l'écran de ton S24 en pixels
        val screenHeight = resources.displayMetrics.heightPixels

        // 1. BLOCAGE DE SPOTLIGHT (Par positionnement géométrique)
        if (isSpotlightTopTitleVisible(rootNode, screenHeight)) {
            Toast.makeText(applicationContext, "Spotlight bloqué ! Retour au travail ❌", Toast.LENGTH_SHORT).show()
            performGlobalAction(GLOBAL_ACTION_BACK)
            return
        }

        // 2. BLOCAGE DU SCROLL DANS LES STORIES (Qui fonctionne déjà !)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            if (isStoriesScreenActive(rootNode)) {
                Toast.makeText(applicationContext, "Pas de scroll dans les Stories ! 🛑", Toast.LENGTH_SHORT).show()
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    // Fonction géométrique pour détecter le vrai écran Spotlight
    private fun isSpotlightTopTitleVisible(node: AccessibilityNodeInfo?, screenHeight: Int): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""

            // Si l'élément contient le mot "spotlight"
            if (text.contains("spotlight") || desc.contains("spotlight")) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
                
                // Si le haut de l'élément (rect.top) est dans la moitié supérieure de l'écran,
                // c'est le titre ou le contenu de l'onglet Spotlight, donc on bloque !
                if (rect.top < screenHeight / 2) {
                    return true
                }
            }
        }

        for (i in 0 until node.childCount) {
            if (isSpotlightTopTitleVisible(node.getChild(i), screenHeight)) {
                return true
            }
        }
        return false
    }

    // Détection de l'onglet Stories (Inchangé car il fonctionne super bien)
    private fun isStoriesScreenActive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            if (text.contains("Stories", ignoreCase = true) || 
                text.contains("Découvrir", ignoreCase = true) || 
                text.contains("Comptes suivis", ignoreCase = true)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isStoriesScreenActive(node.getChild(i))) {
                return true
            }
        }
        return false
    }

    override fun onInterrupt() {}
}