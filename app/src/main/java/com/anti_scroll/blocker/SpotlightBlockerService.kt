package com.anti_scroll.blocker

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import kotlin.math.abs

class SpotlightBlockerService : AccessibilityService() {

    enum class SnapTab { CAMERA, CHAT, STORIES, OTHER }

    companion object {
        private const val SNAP_PACKAGE = "com.snapchat.android"

        private const val BLOCK_COOLDOWN_MS = 1500L      // pas deux blocages à moins de 1,5 s
        private const val REPEAT_WINDOW_MS = 4000L       // 2e blocage dans les 4 s => on utilise "Retour"
        private const val STORIES_ENTRY_GRACE_MS = 900L  // on ignore le scroll juste après l'arrivée sur Stories

        // true  : le scroll est aussi bloqué quand une story est ouverte en plein écran
        // false : seul le scroll de la grille Stories est bloqué
        private const val BLOCK_IN_STORY_VIEWER = true

        // Passe à true pour relever les IDs dans Logcat (filtre SNAPDUMP), puis remets à false.
        private const val DEBUG_DUMP = false

        // IDs relevés dans les dumps SNAPDUMP
        private const val ID_NAV_CAMERA = "$SNAP_PACKAGE:id/ngs_camera_icon_container" // barre du bas
        private const val ID_CAMERA_PAGE = "$SNAP_PACKAGE:id/camera_page"             // page Caméra
        private const val ID_CHAT_ITEM = "$SNAP_PACKAGE:id/ff_item"                    // lignes du Chat
        private const val ID_STORY_CARD = "$SNAP_PACKAGE:id/df_large_story"            // cartes "Découvrir"
        private const val ID_FRIEND_CARD = "$SNAP_PACKAGE:id/friend_card_frame"        // cercles d'amis
        private const val ID_VIEWER = "$SNAP_PACKAGE:id/opera_viewer"                  // story en plein écran

        private val WATCHED_IDS = setOf(
            ID_NAV_CAMERA, ID_CAMERA_PAGE, ID_CHAT_ITEM, ID_STORY_CARD, ID_FRIEND_CARD, ID_VIEWER
        )
    }

    private var lastBlock = 0L
    private var lastDump = 0L
    private var storiesEnteredAt = 0L

    /** Résultat d'un unique parcours de l'arbre d'accessibilité. */
    private class Scan {
        val ids = mutableSetOf<String>()
        var navCamera: AccessibilityNodeInfo? = null // bouton Caméra de la barre du bas
        var spotlightHeader = false                  // titre centré "Spotlight" en haut
        var photoPreview = false                     // écran d'envoi d'un snap
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() != SNAP_PACKAGE) return

        val root = rootInActiveWindow ?: return

        if (DEBUG_DUMP) {
            val t = System.currentTimeMillis()
            val isTabChange = event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
                event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            if (isTabChange && t - lastDump > 700) {
                lastDump = t
                Log.d("SNAPDUMP", "===== NOUVEAU DUMP =====")
                dumpTree(root)
            }
        }

        // Anti-spam
        val now = System.currentTimeMillis()
        if (now - lastBlock < BLOCK_COOLDOWN_MS) return

        // Un seul parcours de l'arbre pour tout relever
        val metrics = resources.displayMetrics
        val scan = Scan()
        scanTree(root, scan, metrics.widthPixels, metrics.heightPixels)

        // 🛡️ SÉCURITÉ PHOTO : écran d'envoi => on ne bloque rien
        if (scan.photoPreview) return

        // Pas de barre du bas = conversation, snap ouvert, etc. => on ne bloque rien
        val navCamera = scan.navCamera ?: return

        val tab = detectCurrentTab(scan.ids)

        // Mémorise le moment d'arrivée sur l'onglet Stories
        if (tab == SnapTab.STORIES) {
            if (storiesEnteredAt == 0L) storiesEnteredAt = now
        } else {
            storiesEnteredAt = 0L
        }

        // 1. BLOCAGE DE SPOTLIGHT
        if (scan.spotlightHeader) {
            block("Spotlight bloqué ! Retour au travail ❌", now, navCamera)
            return
        }

        // 2. BLOCAGE DU SCROLL DANS LES STORIES
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED &&
            tab == SnapTab.STORIES &&
            now - storiesEnteredAt > STORIES_ENTRY_GRACE_MS &&
            (BLOCK_IN_STORY_VIEWER || ID_VIEWER !in scan.ids)
        ) {
            block("Pas de scroll dans les Stories ! 🛑", now, navCamera)
        }
    }

    // ---------------------------------------------------------------
    // ACTION DE BLOCAGE : retour à la Caméra (sinon "Retour")
    // ---------------------------------------------------------------

    private fun block(message: String, now: Long, navCamera: AccessibilityNodeInfo) {
        val isRepeat = now - lastBlock < REPEAT_WINDOW_MS
        lastBlock = now
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

        // Premier essai : clic sur l'onglet Caméra. Si ça échoue ou se répète : Retour.
        if (isRepeat || !clickNode(navCamera)) {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun clickNode(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        var depth = 0
        while (current != null && depth < 4) {
            if (current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            current = current.parent
            depth++
        }
        return false
    }

    // ---------------------------------------------------------------
    // PARCOURS UNIQUE DE L'ARBRE
    // (Snapchat n'expose jamais "selected" sur la barre du bas :
    //  on reconnaît la page affichée par ses éléments)
    // ---------------------------------------------------------------

    private fun scanTree(node: AccessibilityNodeInfo?, out: Scan, width: Int, height: Int) {
        if (node == null) return

        if (node.isVisibleToUser) {
            val id = node.viewIdResourceName
            if (id != null && id in WATCHED_IDS) {
                out.ids.add(id)
                if (id == ID_NAV_CAMERA) out.navCamera = node
            }

            val text = node.text?.toString()?.trim().orEmpty()
            if (text.isNotEmpty()) {
                if (text.contains("Envoyer à", ignoreCase = true) ||
                    text.contains("Send To", ignoreCase = true)
                ) {
                    out.photoPreview = true
                }

                // Titre "Spotlight" : en haut de l'écran ET centré horizontalement.
                // (le sous-titre d'une story est à gauche, le bouton de la barre du bas est en bas)
                if (text.equals("Spotlight", ignoreCase = true)) {
                    val r = Rect().also { node.getBoundsInScreen(it) }
                    val isTop = r.top < height * 0.12
                    val isCentered = abs(r.centerX() - width / 2) < width * 0.10
                    if (isTop && isCentered) out.spotlightHeader = true
                }
            }
        }

        for (i in 0 until node.childCount) scanTree(node.getChild(i), out, width, height)
    }

    private fun detectCurrentTab(ids: Set<String>): SnapTab = when {
        ID_CAMERA_PAGE in ids -> SnapTab.CAMERA
        ID_CHAT_ITEM in ids -> SnapTab.CHAT
        ID_STORY_CARD in ids || ID_FRIEND_CARD in ids -> SnapTab.STORIES
        else -> SnapTab.OTHER
    }

    // ---------------------------------------------------------------
    // OUTIL DE DUMP (debug) : IDs lisibles uniquement, sans texte
    // ---------------------------------------------------------------

    private fun dumpTree(node: AccessibilityNodeInfo?, depth: Int = 0) {
        if (node == null) return
        val id = node.viewIdResourceName
        if (id != null && !id.contains("0_resource_name_obfuscated")) {
            val r = Rect().also { node.getBoundsInScreen(it) }
            Log.d(
                "SNAPDUMP",
                "${"  ".repeat(depth)}id=$id desc=${node.contentDescription} " +
                    "vis=${node.isVisibleToUser} bounds=$r"
            )
        }
        for (i in 0 until node.childCount) dumpTree(node.getChild(i), depth + 1)
    }

    override fun onInterrupt() {}
}