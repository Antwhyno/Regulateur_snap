package com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class SpotlightBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return

        // 1. BLOCAGE DIRECT DE SPOTLIGHT
        // Si le grand titre "Spotlight" non-cliquable est visible en haut, on éjecte direct.
        if (isSpotlightTitleVisible(rootNode)) {
            Toast.makeText(applicationContext, "Spotlight bloqué ! Retour au travail ❌", Toast.LENGTH_SHORT).show()
            performGlobalAction(GLOBAL_ACTION_BACK)
            return
        }

        // 2. BLOCAGE DU SCROLL DANS LES STORIES (DOOM-SCROLLING)
        // On écoute uniquement l'événement de défilement (TYPE_VIEW_SCROLLED)
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            // Si l'utilisateur est sur l'onglet Stories/Découvrir et qu'il essaie de descendre
            if (isStoriesScreenActive(rootNode)) {
                Toast.makeText(applicationContext, "Pas de scroll dans les Stories ! 🛑", Toast.LENGTH_SHORT).show()
                // On le renvoie instantanément à l'écran précédent (l'appareil photo)
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        }
    }

    // Détecte si le vrai écran Spotlight est actif (en cherchant le titre textuel en haut)
    private fun isSpotlightTitleVisible(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            val desc = node.contentDescription?.toString() ?: ""
            
            // Le titre "Spotlight" en haut de l'écran n'est pas cliquable (contrairement au bouton du bas)
            if ((text.equals("Spotlight", ignoreCase = true) || desc.equals("Spotlight", ignoreCase = true)) && !node.isClickable) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isSpotlightTitleVisible(node.getChild(i))) {
                return true
            }
        }
        return false
    }

    // Détecte si l'utilisateur regarde actuellement l'onglet Stories / Découvrir
    private fun isStoriesScreenActive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            // On cible les mots-clés uniques de cet écran vus sur ta vidéo
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