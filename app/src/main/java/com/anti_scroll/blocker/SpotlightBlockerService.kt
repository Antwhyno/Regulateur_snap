package com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class SpotlightBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return

        // 🛡️ SÉCURITÉ PHOTO : Si le bouton "Envoyer à" est à l'écran, on ne bloque RIEN
        if (isPhotoPreviewActive(rootNode)) {
            return
        }

        val screenHeight = resources.displayMetrics.heightPixels

        // 1. BLOCAGE DE SPOTLIGHT
        if (isSpotlightTopTitleVisible(rootNode, screenHeight)) {
            Toast.makeText(applicationContext, "Spotlight bloqué ! Retour au travail ❌", Toast.LENGTH_SHORT).show()
            performGlobalAction(GLOBAL_ACTION_BACK)
            return
        }

        // 2. BLOCAGE DU SCROLL DANS LES STORIES
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            if (isStoriesScreenActive(rootNode)) {
                Toast.makeText(applicationContext, "Pas de scroll dans les Stories ! 🛑", Toast.LENGTH_SHORT).show()
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    // Détecte si l'utilisateur vient de prendre une photo et se trouve sur l'écran d'envoi
    private fun isPhotoPreviewActive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            // Le bouton "Envoyer à" n'apparaît que sur l'écran d'édition de la photo
            if (text.contains("Envoyer à", ignoreCase = true) || text.contains("Send To", ignoreCase = true)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isPhotoPreviewActive(node.getChild(i))) {
                return true
            }
        }
        return false
    }

    private fun isSpotlightTopTitleVisible(node: AccessibilityNodeInfo?, screenHeight: Int): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString()?.lowercase() ?: ""
            val desc = node.contentDescription?.toString()?.lowercase() ?: ""

            if (text.contains("spotlight") || desc.contains("spotlight")) {
                val rect = Rect()
                node.getBoundsInScreen(rect)
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

    private fun isStoriesScreenActive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            // On cible l'onglet Stories uniquement via ses titres de sections réels
            if (text.contains("Découvrir", ignoreCase = true) || text.contains("Comptes suivis", ignoreCase = true)) {
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