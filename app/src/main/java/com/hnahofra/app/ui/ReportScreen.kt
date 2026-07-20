package com.hnahofra.app.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.hnahofra.app.R
import com.hnahofra.app.data.ImgbbUploader
import com.hnahofra.app.data.Pothole
import com.hnahofra.app.data.PotholeRepository
import com.hnahofra.app.data.STATE_OPEN
import com.hnahofra.app.data.STATE_REPAIRED
import com.hnahofra.app.data.SupabaseConfig
import com.hnahofra.app.util.LocationHelper
import com.hnahofra.app.util.Safi
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    repairTarget: Pothole?,
    onDone: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isRepair = repairTarget != null

    var name by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(if (isRepair) STATE_REPAIRED else STATE_OPEN) }
    var picked by remember {
        mutableStateOf(repairTarget?.let { LatLng(it.lat, it.lng) })
    }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var focusRequest by remember { mutableStateOf<LatLng?>(null) }
    var dateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var sending by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) photoUri = pendingPhotoUri }

    fun launchCamera() {
        val uri = createImageUri(context)
        pendingPhotoUri = uri
        takePicture.launch(uri)
    }

    val cameraPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera() else toast(context, R.string.perm_camera_needed)
    }

    fun onTakePhoto() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) launchCamera() else cameraPerm.launch(Manifest.permission.CAMERA)
    }

    fun fetchLocation() {
        scope.launch {
            val loc = LocationHelper.current(context)
            when {
                loc == null -> toast(context, R.string.err_location_unavailable)
                !Safi.contains(loc.first, loc.second) -> toast(context, R.string.err_out_of_safi)
                else -> {
                    val p = LatLng(loc.first, loc.second)
                    picked = p
                    focusRequest = p
                }
            }
        }
    }

    // Dialogue système « Activer la localisation » si le GPS est éteint.
    val enableLocationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) fetchLocation()
        else toast(context, R.string.err_location_unavailable)
    }

    // Vérifie que la localisation est activée ; sinon, propose de l'activer,
    // puis récupère la position.
    fun requestLocationThenFetch() {
        val request = LocationSettingsRequest.Builder()
            .addLocationRequest(
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
            )
            .setAlwaysShow(true)
            .build()
        LocationServices.getSettingsClient(context)
            .checkLocationSettings(request)
            .addOnSuccessListener { fetchLocation() }
            .addOnFailureListener { e ->
                if (e is ResolvableApiException) {
                    try {
                        enableLocationLauncher.launch(
                            IntentSenderRequest.Builder(e.resolution).build()
                        )
                    } catch (_: Exception) {
                        toast(context, R.string.err_location_unavailable)
                    }
                } else {
                    fetchLocation()
                }
            }
    }

    val locationPerm = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) requestLocationThenFetch() else toast(context, R.string.perm_location_needed)
    }

    fun onMyLocation() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) requestLocationThenFetch()
        else locationPerm.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun submit() {
        if (name.isBlank()) { toast(context, R.string.err_name); return }
        val loc = picked ?: run { toast(context, R.string.err_location); return }
        if (photoUri == null) { toast(context, R.string.err_photo); return }
        if (!ImgbbUploader.isConfigured(context) || !SupabaseConfig.isConfigured(context)) {
            toast(context, R.string.config_missing); return
        }
        sending = true
        scope.launch {
            val url = ImgbbUploader.upload(context, photoUri!!)
            if (url == null) {
                sending = false
                toast(context, R.string.err_upload)
                return@launch
            }
            val ok = if (isRepair) {
                PotholeRepository.markRepaired(context, repairTarget!!.id, name.trim(), url)
            } else {
                PotholeRepository.add(
                    context,
                    Pothole(
                        reporterName = name.trim(),
                        state = state,
                        lat = loc.latitude,
                        lng = loc.longitude,
                        imageUrl = url,
                        dateMillis = dateMillis
                    )
                )
            }
            sending = false
            if (ok) {
                toast(context, R.string.ok_sent)
                onDone()
            } else {
                toast(context, R.string.err_save)
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(picked ?: Safi.CENTER, Safi.DEFAULT_ZOOM)
    }

    // Recentre la carte sur la position trouvée via « Ma position ».
    LaunchedEffect(focusRequest) {
        focusRequest?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, 16f))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (isRepair) R.string.repair_report_title else R.string.new_report_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.your_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (!isRepair) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.state_label), fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state == STATE_OPEN,
                        onClick = { state = STATE_OPEN },
                        label = { Text(stringResource(R.string.state_pothole)) }
                    )
                    FilterChip(
                        selected = state == STATE_REPAIRED,
                        onClick = { state = STATE_REPAIRED },
                        label = { Text(stringResource(R.string.state_repaired)) }
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.discovery_date), fontWeight = FontWeight.SemiBold)
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                    Text("   " + DateFormat.getDateInstance(DateFormat.LONG).format(Date(dateMillis)))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.pick_location), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp)),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    minZoomPreference = Safi.MIN_ZOOM,
                    latLngBoundsForCameraTarget = Safi.BOUNDS
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    scrollGesturesEnabled = !isRepair
                ),
                onMapClick = { latLng ->
                    if (!isRepair) {
                        if (Safi.contains(latLng.latitude, latLng.longitude)) {
                            picked = latLng
                        } else {
                            toast(context, R.string.err_out_of_safi)
                        }
                    }
                }
            ) {
                picked?.let { Marker(state = MarkerState(position = it)) }
            }

            if (!isRepair) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { onMyLocation() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null)
                    Text("   " + stringResource(R.string.my_location))
                }
            }

            Spacer(Modifier.height(16.dp))
            if (photoUri != null) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { onTakePhoto() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Text(
                    "   " + stringResource(
                        if (photoUri == null) R.string.take_photo else R.string.retake_photo
                    )
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (!sending) submit() },
                enabled = !sending,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("   " + stringResource(R.string.sending))
                } else {
                    Icon(Icons.Filled.Send, contentDescription = null)
                    Text("   " + stringResource(R.string.send))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = dateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { dateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        ) { DatePicker(state = dpState) }
    }
}

private fun createImageUri(context: Context): Uri {
    val dir = File(context.cacheDir, "photos").apply { mkdirs() }
    val file = File(dir, "pothole_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun toast(context: Context, resId: Int) {
    Toast.makeText(context, resId, Toast.LENGTH_LONG).show()
}
