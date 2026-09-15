package com.jeremstyke.purgecoremobile.ui.components

import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

private const val NATIVE_AD_UNIT_ID = "ca-app-pub-8638687738606649/4492455873"

/**
 * A native ad styled as a card in the results list, rather than a banner or
 * a full-screen interruption. Loaded once per composition and released when
 * this leaves the list (results are re-scanned), so nothing is held onto
 * past its use.
 */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(Unit) {
        val adLoader = AdLoader.Builder(context, NATIVE_AD_UNIT_ID)
            .forNativeAd { ad -> nativeAd = ad }
            .withAdListener(object : AdListener() {})
            .build()
        adLoader.loadAd(AdRequest.Builder().build())

        onDispose { nativeAd?.destroy() }
    }

    val ad = nativeAd
    if (ad != null) {
        val cardColor = MaterialTheme.colorScheme.surface.toArgb()
        val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
        val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()
        val accentColor = MaterialTheme.colorScheme.primary.toArgb()

        AndroidView(
            modifier = modifier.fillMaxWidth(),
            factory = { ctx ->
                val headline = TextView(ctx).apply {
                    setTextColor(textColor)
                    textSize = 15f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }
                val body = TextView(ctx).apply {
                    setTextColor(mutedColor)
                    textSize = 13f
                    setPadding(0, 8, 0, 12)
                }
                val adLabel = TextView(ctx).apply {
                    text = "Ad"
                    setTextColor(mutedColor)
                    textSize = 11f
                    setPadding(0, 0, 0, 8)
                }
                val cta = Button(ctx).apply {
                    setTextColor(cardColor)
                    setBackgroundColor(accentColor)
                }

                val content = LinearLayout(ctx).apply {
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.START
                    addView(adLabel)
                    addView(headline)
                    addView(body)
                    addView(cta)
                }

                NativeAdView(ctx).apply {
                    val padding = (16 * ctx.resources.displayMetrics.density).toInt()
                    setPadding(padding, padding, padding, padding)
                    background = GradientDrawable().apply {
                        setColor(cardColor)
                        cornerRadius = 16 * ctx.resources.displayMetrics.density
                    }
                    addView(
                        content,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    headlineView = headline
                    bodyView = body
                    callToActionView = cta
                }
            },
            update = { view ->
                val adView = view as NativeAdView
                (adView.headlineView as TextView).text = ad.headline
                (adView.bodyView as TextView).text = ad.body ?: ""
                (adView.bodyView as TextView).visibility =
                    if (ad.body.isNullOrEmpty()) android.view.View.GONE else android.view.View.VISIBLE
                (adView.callToActionView as Button).text = ad.callToAction ?: ""
                adView.setNativeAd(ad)
            }
        )
    }
}
