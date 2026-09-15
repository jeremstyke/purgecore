package com.jeremstyke.purgecoremobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.ads.MobileAds
import com.jeremstyke.purgecoremobile.ui.components.BannerAd
import com.jeremstyke.purgecoremobile.ui.screens.DuplicatesScreen
import com.jeremstyke.purgecoremobile.ui.screens.StorageScreen
import com.jeremstyke.purgecoremobile.ui.theme.PurgeCoreMobileTheme

private sealed class Destination(val route: String, val label: String) {
    data object Storage : Destination("storage", "Storage")
    data object Duplicates : Destination("duplicates", "Duplicates")
}

private val destinations = listOf(Destination.Storage, Destination.Duplicates)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MobileAds.initialize(this)
        setContent {
            PurgeCoreMobileTheme {
                Surface {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            Column {
                // Shown once here rather than inside each screen, so it
                // stays put across navigation instead of reloading an ad
                // every time someone switches tabs.
                BannerAd()
                AppBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Storage.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Destination.Storage.route) { StorageScreen() }
            composable(Destination.Duplicates.route) { DuplicatesScreen() }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        destinations.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (destination == Destination.Storage) Icons.Filled.Storage else Icons.Filled.ContentCopy,
                        contentDescription = destination.label
                    )
                },
                label = { Text(destination.label) }
            )
        }
    }
}

