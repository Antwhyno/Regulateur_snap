package com.anti_scroll.blocker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var btnActiver: Button
    private lateinit var txtStatut: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 🛡️ Masque la barre d'application système par défaut
        actionBar?.hide()
        setContentView(R.layout.activity_main)

        // Liaison avec les éléments du fichier XML
        btnActiver = findViewById<Button>(R.id.btn_activer_bloqueur)
        txtStatut = findViewById<TextView>(R.id.txt_statut)

        // Clic sur le bouton : ouvre le menu d'accessibilité du S24
        btnActiver.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        
        // À chaque fois que l'application revient à l'écran, on vérifie le statut
        if (isAccessibilityServiceEnabled(this, SpotlightBlockerService::class.java)) {
            
            // 🛡️ MODE SÉCURISÉ : Le bloqueur tourne en arrière-plan
            // On masque complètement le bouton (GONE = invisible et ne prend pas de place)
            btnActiver.visibility = View.GONE
            
            // On affiche un message d'encouragement fixe
            txtStatut.text = "Régulateur actif. Bon travail ! 🚀\nLaisse ton téléphone de côté et concentre-toi."
            
        } else {
            // MODE REPOS : Le bloqueur est éteint, on affiche le bouton pour pouvoir le configurer
            btnActiver.visibility = View.VISIBLE
            txtStatut.text = "Le régulateur est actuellement désactivé."
        }
    }

    /**
     * Fonction système qui vérifie si ton service SpotlightBlockerService est activé dans Android
     */
    private fun isAccessibilityServiceEnabled(context: Context, serviceClass: Class<*>): Boolean {
        val expectedComponentName = "${context.packageName}/${serviceClass.name}"
        
        // On récupère la liste des services d'accessibilité actuellement autorisés par l'utilisateur
        val settingValue = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        
        // Si notre identifiant unique est dans la liste, c'est que le service est ON
        return settingValue?.contains(expectedComponentName) == true
    }
}