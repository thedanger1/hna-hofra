package com.hnahofra.app.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polygon
import com.google.maps.android.compose.rememberCameraPositionState
import com.hnahofra.app.R
import com.hnahofra.app.data.AdminSession
import com.hnahofra.app.data.Pothole
import com.hnahofra.app.data.PotholeRepository
import com.hnahofra.app.data.STATE_OPEN
import com.hnahofra.app.data.STATE_REPAIRED
import com.hnahofra.app.data.SupabaseConfig
import com.hnahofra.app.util.MapIcons
import com.hnahofra.app.util.Notifications
import com.hnahofra.app.util.Safi
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TEN_DAYS_MS = 10L * 24 * 60 * 60 * 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onNewReport: () -> Unit,
    onRepair: (Pothole) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val configured = remember { SupabaseConfig.isConfigured(context) }
    val isAdmin = AdminSession.isActive

    var potholes by remember { mutableStateOf<List<Pothole>>(emptyList()) }
    var selected by remember { mutableStateOf<Pothole?>(null) }
    var confirmDeleteId by remember { mutableStateOf<String?>(null) }
    var flagTarget by remember { mutableStateOf<Pothole?>(null) }

    // Nombre de trous ayant au moins un signalement (pour l'admin).
    val pendingReports = potholes.count { it.reportCount > 0 }

    // Notification locale à l'admin quand des signalements sont en attente.
    val notifPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}
    LaunchedEffect(isAdmin) {
        if (isAdmin && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(isAdmin, pendingReports) {
        if (isAdmin && pendingReports > 0) {
            Notifications.showPendingReports(context, pendingReports)
        }
    }

    // Rafraîchissement périodique via l'API REST Supabase : la carte est à jour
    // pour tout le monde toutes les ~12 s (et à chaque ouverture de l'écran).
    LaunchedEffect(configured) {
        while (configured) {
            potholes = PotholeRepository.fetchAll(context)
            delay(12_000)
        }
    }

    // Filtre : les trous réparés depuis plus de 10 jours disparaissent.
    val now = System.currentTimeMillis()
    val visible = potholes.filter { p ->
        !p.isRepaired || (now - p.dateMillis) <= TEN_DAYS_MS
    }

    // On s'assure que le SDK Maps est initialisé avant de fabriquer les icônes
    // (BitmapDescriptorFactory), sinon crash à l'entrée sur la carte.
    val openIcon = remember {
        MapsInitializer.initialize(context)
        MapIcons.fromVector(context, R.drawable.ic_pothole)
    }
    val repairedIcon = remember {
        MapsInitializer.initialize(context)
        MapIcons.fromVector(context, R.drawable.ic_repaired)
    }
    val flagIcon = remember {
        MapsInitializer.initialize(context)
        MapIcons.fromVector(context, R.drawable.ic_flag)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(Safi.CENTER, Safi.DEFAULT_ZOOM)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (isAdmin && pendingReports > 0) {
                        BadgedBox(
                            badge = { Badge { Text("$pendingReports") } },
                            modifier = Modifier.padding(end = 12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Flag,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    if (isAdmin) {
                        IconButton(onClick = {
                            AdminSession.clear()
                            Toast.makeText(context, R.string.admin_logout, Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = stringResource(R.string.admin_logout)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewReport,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.add)) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    minZoomPreference = Safi.MIN_ZOOM,
                    latLngBoundsForCameraTarget = Safi.BOUNDS,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false
                )
            ) {
                // Contour officiel de la ville de Safi (rouge pointillé)
                Polygon(
                    points = Safi.BOUNDARY,
                    strokeColor = Color(0xFFD32F2F),
                    strokeWidth = 6f,
                    fillColor = Color(0x14D32F2F),
                    strokePattern = listOf(Dash(30f), Gap(20f))
                )
                visible.forEach { p ->
                    Marker(
                        state = MarkerState(position = LatLng(p.lat, p.lng)),
                        icon = when {
                            isAdmin && p.reportCount > 0 -> flagIcon
                            p.isRepaired -> repairedIcon
                            else -> openIcon
                        },
                        onClick = {
                            selected = p
                            true
                        }
                    )
                }
            }

            if (!configured) {
                ConfigBanner(modifier = Modifier.align(Alignment.TopCenter))
            }
        }

        // Fiche détail du trou sélectionné.
        selected?.let { p ->
            ModalBottomSheet(
                onDismissRequest = { selected = null },
                sheetState = rememberModalBottomSheetState()
            ) {
                PotholeDetail(
                    pothole = p,
                    isAdmin = isAdmin,
                    onFlag = { flagTarget = p },
                    onRepair = {
                        selected = null
                        onRepair(p)
                    },
                    onToggleState = {
                        val newState = if (p.isRepaired) STATE_OPEN else STATE_REPAIRED
                        selected = null
                        scope.launch {
                            val ok = PotholeRepository.setState(context, p.id, newState)
                            if (ok) potholes = PotholeRepository.fetchAll(context)
                            else Toast.makeText(context, R.string.action_failed, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDelete = { confirmDeleteId = p.id }
                )
            }
        }

        // Confirmation de suppression (admin).
        confirmDeleteId?.let { id ->
            AlertDialog(
                onDismissRequest = { confirmDeleteId = null },
                title = { Text(stringResource(R.string.delete)) },
                text = { Text(stringResource(R.string.delete_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        confirmDeleteId = null
                        selected = null
                        scope.launch {
                            val ok = PotholeRepository.delete(context, id)
                            if (ok) potholes = PotholeRepository.fetchAll(context)
                            Toast.makeText(
                                context,
                                if (ok) R.string.deleted_ok else R.string.action_failed,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }) { Text(stringResource(R.string.delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { confirmDeleteId = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Choix du motif de signalement (visible par tous).
        flagTarget?.let { p ->
            val reasons = listOf(
                "FAKE" to R.string.reason_fake,
                "INAPPROPRIATE" to R.string.reason_inappropriate,
                "PERSON" to R.string.reason_person,
                "OTHER" to R.string.reason_other
            )
            AlertDialog(
                onDismissRequest = { flagTarget = null },
                title = { Text(stringResource(R.string.report_dialog_title)) },
                text = {
                    Column {
                        reasons.forEach { (key, label) ->
                            TextButton(
                                onClick = {
                                    flagTarget = null
                                    selected = null
                                    scope.launch {
                                        val ok = PotholeRepository.reportPothole(context, p.id, key)
                                        if (ok) potholes = PotholeRepository.fetchAll(context)
                                        Toast.makeText(
                                            context,
                                            if (ok) R.string.report_thanks else R.string.action_failed,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text(stringResource(label)) }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { flagTarget = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun PotholeDetail(
    pothole: Pothole,
    isAdmin: Boolean,
    onFlag: () -> Unit,
    onRepair: () -> Unit,
    onToggleState: () -> Unit,
    onDelete: () -> Unit
) {
    val df = remember { DateFormat.getDateInstance(DateFormat.LONG) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pothole.imageUrl.isNotBlank()) {
            AsyncImage(
                model = pothole.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(16.dp))
        }

        val stateColor = if (pothole.isRepaired)
            MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        Text(
            text = stringResource(
                if (pothole.isRepaired) R.string.state_repaired else R.string.state_pothole
            ),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = stateColor
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(
                if (pothole.isRepaired) R.string.repaired_by else R.string.declared_by,
                pothole.reporterName
            ),
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = stringResource(R.string.on_date, df.format(Date(pothole.dateMillis))),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!pothole.isRepaired) {
            Spacer(Modifier.height(20.dp))
            Button(onClick = onRepair, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Build, contentDescription = null)
                Text("   " + stringResource(R.string.mark_repaired))
            }
        }

        // Signaler un problème (photo non conforme, personne, faux trou…) — tout le monde.
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onFlag, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Flag, contentDescription = null)
            Text("   " + stringResource(R.string.report_problem))
        }

        // Actions de modération réservées à l'administrateur.
        if (isAdmin) {
            if (pothole.reportCount > 0) {
                Spacer(Modifier.height(16.dp))
                val context = LocalContext.current
                val reasons by produceState(initialValue = emptyList<String>(), pothole.id) {
                    value = PotholeRepository.fetchReportReasons(context, pothole.id)
                }
                Text(
                    text = stringResource(R.string.reports_count, pothole.reportCount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                if (reasons.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.reports_reasons_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    reasons.forEach { r ->
                        Text(
                            text = "• " + reasonLabel(r),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onToggleState, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.SwapHoriz, contentDescription = null)
                Text(
                    "   " + stringResource(
                        if (pothole.isRepaired) R.string.admin_set_open else R.string.admin_set_repaired
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Text("   " + stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun reasonLabel(key: String): String = when (key) {
    "FAKE" -> stringResource(R.string.reason_fake)
    "INAPPROPRIATE" -> stringResource(R.string.reason_inappropriate)
    "PERSON" -> stringResource(R.string.reason_person)
    else -> stringResource(R.string.reason_other)
}

@Composable
private fun ConfigBanner(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Text(
            text = stringResource(R.string.config_missing),
            color = Color(0xFFB00020),
            modifier = Modifier.padding(12.dp)
        )
    }
}
