package com.anti_scroll.blocker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Relie le code Kotlin au fichier de design XML
        setContentView(R.layout.activity_main)

        // 2. Récupère le bouton jaune du XML grâce à son identifiant
        val btnActiver = findViewById<Button>(R.id.btn_activer_bloqueur)

        // 3. Écoute le clic sur le bouton
        btnActiver.setOnClickListener {
            // Crée une intention pour ouvrir le menu Accessibilité du Samsung S24
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }
}