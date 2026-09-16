package com.jeremstyke.purgecoremobile.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jeremstyke.purgecoremobile.R
import com.jeremstyke.purgecoremobile.ui.components.BrandHeader
import com.jeremstyke.purgecoremobile.ui.theme.Amber

/**
 * Rather than reimplementing an app list (which needs the sensitive
 * QUERY_ALL_PACKAGES permission on modern Android and complicates a Play
 * Store review), this opens Android's own app management screen, which
 * already lists every installed app with its size and an uninstall option.
 */
@Composable
fun AppsScreen() {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        BrandHeader(title = stringResource(R.string.apps_title))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, Amber)
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.apps_body),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS)
                        context.startActivity(intent)
                    }) {
                        Text(stringResource(R.string.apps_open_settings))
                    }
                }
            }
        }
    }
}
