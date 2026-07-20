package com.hnahofra.app.ui

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hnahofra.app.R
import com.hnahofra.app.data.SupabaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onReport: () -> Unit,
    onToggleLang: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showAdmin by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Sélecteur de langue (en haut)
            OutlinedButton(
                onClick = onToggleLang,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Filled.Language, contentDescription = null)
                Spacer(Modifier.height(0.dp))
                Text("  " + stringResource(R.string.lang_toggle))
            }

            // Bloc central : logo + bouton
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Appui long sur le logo = accès administrateur (caché).
                Image(
                    painter = painterResource(R.drawable.ic_pothole),
                    contentDescription = null,
                    modifier = Modifier
                        .size(150.dp)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showAdmin = true }
                        )
                )

                Spacer(Modifier.height(24.dp))

                // Logo textuel en arabe (identité de l'app)
                Text(
                    text = stringResource(R.string.brand),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = onReport,
                    contentPadding = PaddingValues(horizontal = 28.dp, vertical = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null)
                    Text(
                        "   " + stringResource(R.string.report_pothole),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Pied de page
            Text(
                text = stringResource(R.string.developed_by),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            )
        }
    }

    if (showAdmin) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var busy by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!busy) showAdmin = false },
            title = { Text(stringResource(R.string.admin_login_title)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(stringResource(R.string.admin_email)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.admin_password)) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        busy = true
                        scope.launch {
                            val ok = SupabaseAuth.signIn(context, email.trim(), password)
                            busy = false
                            if (ok) {
                                showAdmin = false
                                Toast.makeText(context, R.string.admin_active, Toast.LENGTH_SHORT).show()
                                onReport()
                            } else {
                                Toast.makeText(context, R.string.admin_login_failed, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                ) { Text(stringResource(R.string.admin_login)) }
            },
            dismissButton = {
                TextButton(onClick = { if (!busy) showAdmin = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
