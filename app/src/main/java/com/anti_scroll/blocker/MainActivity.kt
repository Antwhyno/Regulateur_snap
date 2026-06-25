package com.anti_scroll.blocker

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Associe le fichier XML de design

        val btnActiver = findViewById<Button>(R.id.btn_activer_bloqueur)
        
        btnActiver.setOnClickListener {
            // Ouvre directement le menu Paramètres > Accessibilité de ton Samsung
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }
}