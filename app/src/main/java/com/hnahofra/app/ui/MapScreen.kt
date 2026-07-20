package com.hnahofra.app.ui

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.hnahofra.app.R
import com.hnahofra.app.data.FirebaseInit
import com.hnahofra.app.data.Pothole
import com.hnahofra.app.data.PotholeRepository
import com.hnahofra.app.util.MapIcons
import com.hnahofra.app.util.Safi
import java.text.DateFormat
import java.util.Date

private const val TEN_DAYS_MS = 10L * 24 * 60 * 60 * 1000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    onNewReport: () -> Unit,
    onRepair: (Pothole) -> Unit
) {
    val context = LocalContext.current
    val configured = remember { FirebaseInit.isConfigured(context) }

    var potholes by remember { mutableStateOf<List<Pothole>>(emptyList()) }
    var selected by remember { mutableStateOf<Pothole?>(null) }

    // Écoute temps réel de Firestore.
    DisposableEffect(configured) {
        val reg = if (configured) {
            PotholeRepository.listen(context) { potholes = it }
        } else null
        onDispose { reg?.remove() }
    }

    // Filtre : les trous réparés depuis plus de 10 jours disparaissent.
    val now = System.currentTimeMillis()
    val visible = potholes.filter { p ->
        !p.isRepaired || (now - p.date.toDate().time) <= TEN_DAYS_MS
    }

    val openIcon = remember { MapIcons.fromVector(context, R.drawable.ic_pothole) }
    val repairedIcon = remember { MapIcons.fromVector(context, R.drawable.ic_repaired) }

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
                visible.forEach { p ->
                    Marker(
                        state = MarkerState(position = LatLng(p.lat, p.lng)),
                        icon = if (p.isRepaired) repairedIcon else openIcon,
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
                    onRepair = {
                        selected = null
                        onRepair(p)
                    }
                )
            }
        }
    }
}

@Composable
private fun PotholeDetail(pothole: Pothole, onRepair: () -> Unit) {
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
            text = stringResource(R.string.on_date, df.format(pothole.date.toDate())),
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
    }
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
