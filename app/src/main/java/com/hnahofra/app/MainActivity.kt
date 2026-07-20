package com.hnahofra.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hnahofra.app.data.Pothole
import com.hnahofra.app.ui.HomeScreen
import com.hnahofra.app.ui.MapScreen
import com.hnahofra.app.ui.ReportScreen
import com.hnahofra.app.ui.theme.HnaHofraTheme
import com.hnahofra.app.util.LocaleManager

/** Trou en attente de réparation, transmis de la carte au formulaire. */
object PendingRepair {
    var pothole: Pothole? = null
}

object Routes {
    const val HOME = "home"
    const val MAP = "map"
    const val REPORT = "report"
}

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HnaHofraTheme {
                AppRoot(onToggleLang = {
                    LocaleManager.toggle(this)
                    recreate()
                })
            }
        }
    }
}

@Composable
private fun AppRoot(onToggleLang: () -> Unit) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onReport = { nav.navigate(Routes.MAP) },
                onToggleLang = onToggleLang
            )
        }
        composable(Routes.MAP) {
            MapScreen(
                onBack = { nav.popBackStack() },
                onNewReport = {
                    PendingRepair.pothole = null
                    nav.navigate(Routes.REPORT)
                },
                onRepair = { pothole ->
                    PendingRepair.pothole = pothole
                    nav.navigate(Routes.REPORT)
                }
            )
        }
        composable(Routes.REPORT) {
            ReportScreen(
                repairTarget = PendingRepair.pothole,
                onDone = {
                    PendingRepair.pothole = null
                    nav.popBackStack()
                },
                onBack = {
                    PendingRepair.pothole = null
                    nav.popBackStack()
                }
            )
        }
    }
}
