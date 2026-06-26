package com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class SpotlightBlockerService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return

        // 🛡️ SÉCURITÉ FINALE : Si la caméra est ACTUELLEMENT VISIBLE par l'utilisateur, on ne fait rien.
        // Grâce à "isVisibleToUser", on ignore les boutons de la caméra cachés en arrière-plan !
        if (isCameraActivelyVisible(rootNode)) {
            return
        }

        // 🎯 CIBLAGE CHIRURGICAL : On vérifie si l'onglet Spotlight est détecté ou cliqué
        if (isSpotlightTargeted(rootNode, event)) {
            Toast.makeText(applicationContext, "Spotlight bloqué ! Retour au travail 🚀", Toast.LENGTH_SHORT).show()
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    // Parcourt l'écran pour voir si les éléments exclusifs de l'appareil photo sont affichés à l'utilisateur
    private fun isCameraActivelyVisible(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            if (text.contains("Flash") || text.contains("Sons") || text.contains("Mode HD") || text.contains("selfies")) {
                return true // La caméra est réellement sous les yeux de l'utilisateur
            }
        }

        for (i in 0 until node.childCount) {
            if (isCameraActivelyVisible(node.getChild(i))) {
                return true
            }
        }
        return false
    }

    // Détecte si l'onglet Spotlight est sélectionné ou s'il vient de subir un clic
    private fun isSpotlightTargeted(node: AccessibilityNodeInfo?, event: AccessibilityEvent): Boolean {
        if (node == null) return false

        val text = node.text?.toString()?.lowercase() ?: ""
        val description = node.contentDescription?.toString()?.lowercase() ?: ""

        if (text.contains("spotlight") || description.contains("spotlight")) {
            // Détection par clic direct sur l'onglet
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
                return true
            }
            // Détection si l'élément (ou sa boîte parente) est marqué comme sélectionné
            if (node.isSelected || (node.parent?.isSelected == true)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isSpotlightTargeted(node.getChild(i), event)) {
                return true
            }
        }
        return false
    }

    override fun onInterrupt() {}
}