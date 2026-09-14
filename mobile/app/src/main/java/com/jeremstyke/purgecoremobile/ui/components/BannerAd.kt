package com.jeremstyke.purgecoremobile.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A single banner ad unit shown at the bottom of the app, below the content
 * and above the bottom navigation. This funds development, the app itself
 * has no paywall or feature locked behind it.
 */
private const val BANNER_AD_UNIT_ID = "ca-app-pub-8638687738606649/8466667977"

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                adUnitId = BANNER_AD_UNIT_ID
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
