package com.hnahofra.app

import android.app.Application
import com.google.android.gms.maps.MapsInitializer

class HofraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialise le SDK Google Maps dès le démarrage : sinon
        // BitmapDescriptorFactory (icônes de marqueurs) n'est pas prêt et
        // provoque un crash à l'ouverture de la carte.
        try {
            MapsInitializer.initialize(this)
        } catch (_: Exception) {
        }
    }
}
