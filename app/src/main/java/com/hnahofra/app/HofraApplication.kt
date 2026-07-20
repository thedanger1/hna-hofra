package com.hnahofra.app

import android.app.Application
import com.hnahofra.app.data.FirebaseInit

class HofraApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialise Firebase si les clés sont renseignées (sinon ignoré).
        FirebaseInit.ensure(this)
    }
}
