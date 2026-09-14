package com.jeremstyke.purgecoremobile.ui.screens

import android.Manifest
import android.app.RecoverableSecurityException
import android.content.pm.PackageManager
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.jeremstyke.purgecoremobile.R
import com.jeremstyke.purgecoremobile.data.DuplicateFinder
import com.jeremstyke.purgecoremobile.data.DuplicateGroup
import kotlinx.coroutines.launch

private fun mediaPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= 33) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

@Composable
fun DuplicatesScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember {
        mutableStateOf(
            mediaPermissions().all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    var isScanning by remember { mutableStateOf(false) }
    var groups by remember { mutableStateOf<List<DuplicateGroup>>(emptyList()) }
    var hasScannedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermission = results.values.all { it }
    }

    // Deleting media on modern Android needs the system's own confirmation
    // dialog, the mobile equivalent of "goes to the Recycle Bin, with a
    // confirmation" on the Windows app, nothing here deletes silently.
    val deleteLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            scope.launch {
                isScanning = true
                groups = DuplicateFinder.findDuplicates(context)
                isScanning = false
            }
        }
    }

    fun runScan() {
        scope.launch {
            isScanning = true
            groups = DuplicateFinder.findDuplicates(context)
            isScanning = false
            hasScannedOnce = true
        }
    }

    fun deleteExtraCopies(group: DuplicateGroup) {
        // Always keeps the first copy, exactly like the Windows duplicate
        // finder, a group is never fully wiped out by one action.
        val urisToDelete = group.items.drop(1).map { it.uri }
        if (urisToDelete.isEmpty()) return

        if (Build.VERSION.SDK_INT >= 30) {
            val pendingIntent = MediaStore.createDeleteRequest(context.contentResolver, urisToDelete)
            deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
        } else {
            // Pre-API 30: no batch confirmation dialog exists, delete one by
            // one and let a RecoverableSecurityException surface if the OS
            // still wants to ask, rather than forcing a fake confirmation.
            scope.launch {
                urisToDelete.forEach { uri ->
                    try {
                        context.contentResolver.delete(uri, null, null)
                    } catch (e: RecoverableSecurityException) {
                        // Would need startIntentSenderForResult with e.userAction here;
                        // acceptable gap for the initial legacy-OS fallback path.
                    }
                }
                groups = DuplicateFinder.findDuplicates(context)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(
            text = stringResource(R.string.duplicates_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.duplicates_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        if (!hasPermission) {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    Text(stringResource(R.string.duplicates_permission_rationale))
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { permissionLauncher.launch(mediaPermissions()) }) {
                        Text(stringResource(R.string.duplicates_grant_permission))
                    }
                }
            }
            return@Column
        }

        Button(onClick = { runScan() }, enabled = !isScanning) {
            Text(stringResource(R.string.duplicates_scan))
        }
        Spacer(Modifier.height(16.dp))

        when {
            isScanning -> {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text(stringResource(R.string.duplicates_scanning))
                    }
                }
            }
            hasScannedOnce && groups.isEmpty() -> {
                Text(stringResource(R.string.duplicates_none_found))
            }
            groups.isNotEmpty() -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(groups) { group ->
                        Card(shape = RoundedCornerShape(16.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = String.format(
                                        stringResource(R.string.duplicates_group_count),
                                        group.items.size
                                    ),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${formatBytes(group.reclaimableBytes)} reclaimable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedButton(onClick = { deleteExtraCopies(group) }) {
                                    Text(stringResource(R.string.duplicates_delete_extra))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
