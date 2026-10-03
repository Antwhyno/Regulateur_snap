ackage com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class SpotlightBlockerService : AccessibilityService() {

    enum class SnapTab { CAMERA, CHAT, STORIES, SPOTLIGHT, UNKNOWN }

    companion object {
        private const val SNAP_PACKAGE = "com.snapchat.android"
        private const val BLOCK_COOLDOWN_MS = 1500L

        // Passe à true pour relever les IDs/descriptions dans Logcat (filtre SNAPDUMP),
        // puis remets à false une fois detectCurrentTab() complété.
        private const val DEBUG_DUMP = true
    }

    private var lastBlock = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() != SNAP_PACKAGE) return

        val root = rootInActiveWindow ?: return

        if (DEBUG_DUMP && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            dumpTree(root)
        }

        // Anti-spam : pas de nouveau blocage pendant 1,5 s
        val now = System.currentTimeMillis()
        if (now - lastBlock < BLOCK_COOLDOWN_MS) return

        // 🛡️ SÉCURITÉ PHOTO : écran d'envoi => on ne bloque rien
        if (isPhotoPreviewActive(root)) return

        val screenHeight = resources.displayMetrics.heightPixels
        val tab = detectCurrentTab(root, screenHeight)

        // 1. BLOCAGE DE SPOTLIGHT
        val onSpotlight = if (tab == SnapTab.UNKNOWN) {
            // Fallback tant que les signatures d'onglets ne sont pas complétées
            isSpotlightTopTitleVisible(root, screenHeight)
        } else {
            tab == SnapTab.SPOTLIGHT
        }
        if (onSpotlight) {
            block("Spotlight bloqué ! Retour au travail ❌", now)
            return
        }

        // 2. BLOCAGE DU SCROLL DANS LES STORIES
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            val onStories = if (tab == SnapTab.UNKNOWN) {
                isStoriesScreenActive(root)
            } else {
                tab == SnapTab.STORIES
            }
            if (onStories) {
                block("Pas de scroll dans les Stories ! 🛑", now)
            }
        }
    }

    private fun block(message: String, now: Long) {
        lastBlock = now
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    // ---------------------------------------------------------------
    // DÉTECTION DE L'ONGLET (barre de navigation du bas)
    // ---------------------------------------------------------------

    private fun detectCurrentTab(root: AccessibilityNodeInfo, screenHeight: Int): SnapTab {
        val selected = findSelectedBottomNode(root, screenHeight) ?: return SnapTab.UNKNOWN
        val id = selected.viewIdResourceName.orEmpty().lowercase()
        val desc = selected.contentDescription?.toString().orEmpty().lowercase()

        // ⚠️ À ADAPTER avec ce que tu as relevé dans le dump SNAPDUMP
        return when {
            id.contains("spotlight") || desc.contains("spotlight") -> SnapTab.SPOTLIGHT
            id.contains("stories") || desc.contains("stories") || desc.contains("découvrir") -> SnapTab.STORIES
            id.contains("chat") || desc.contains("chat") -> SnapTab.CHAT
            id.contains("camera") || desc.contains("caméra") || desc.contains("camera") -> SnapTab.CAMERA
            else -> SnapTab.UNKNOWN
        }
    }

    private fun findSelectedBottomNode(node: AccessibilityNodeInfo?, screenHeight: Int): AccessibilityNodeInfo? {
        if (node == null) return null

        if (node.isSelected) {
            val r = Rect().also { node.getBoundsInScreen(it) }
            if (r.top > screenHeight * 0.85) return node // zone de la barre du bas
        }

        for (i in 0 until node.childCount) {
            findSelectedBottomNode(node.getChild(i), screenHeight)?.let { return it }
        }
        return null
    }

    // ---------------------------------------------------------------
    // OUTIL DE DUMP (debug)
    // ---------------------------------------------------------------

    private fun dumpTree(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return
        val r = Rect().also { node.getBoundsInScreen(it) }
        Log.d(
            "SNAPDUMP",
            "${"  ".repeat(depth)}${node.className} " +
                "id=${node.viewIdResourceName} text=${node.text} " +
                "desc=${node.contentDescription} sel=${node.isSelected} " +
                "vis=${node.isVisibleToUser} bounds=$r"
        )
        for (i in 0 until node.childCount) dumpTree(node.getChild(i), depth + 1)
    }

    // ---------------------------------------------------------------
    // DÉTECTIONS PAR TEXTE (sécurité photo + fallback)
    // ---------------------------------------------------------------

    private fun isPhotoPreviewActive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            if (text.contains("Envoyer à", ignoreCase = true) || text.contains("Send To", ignoreCase = true)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isPhotoPreviewActive(node.getChild(i))) return true
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
                if (rect.top < screenHeight / 2) return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isSpotlightTopTitleVisible(node.getChild(i), screenHeight)) return true
        }
        return false
    }

    private fun isStoriesScreenActive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        if (node.isVisibleToUser) {
            val text = node.text?.toString() ?: ""
            if (text.contains("Découvrir", ignoreCase = true) || text.contains("Comptes suivis", ignoreCase = true)) {
                return true
            }
        }

        for (i in 0 until node.childCount) {
            if (isStoriesScreenActive(node.getChild(i))) return true
        }
        return false
    }

    override fun onInterrupt() {}
}
