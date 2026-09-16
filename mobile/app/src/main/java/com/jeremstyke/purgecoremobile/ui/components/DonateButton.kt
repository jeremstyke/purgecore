package com.jeremstyke.purgecoremobile.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.jeremstyke.purgecoremobile.R

private const val REWARDED_AD_UNIT_ID = "ca-app-pub-8638687738606649/7119579671"

/**
 * A voluntary "support the developer" button, separate from any feature:
 * watching the ad doesn't unlock anything in the app, it's optional, the
 * same way offering a coffee on the website is optional. Nothing in the
 * app changes whether someone taps this or not.
 */
@Composable
fun DonateButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val loadingLabel = stringResource(R.string.donate_loading)
    val thanksLabel = stringResource(R.string.donate_thanks)
    val unavailableLabel = stringResource(R.string.donate_unavailable)

    Button(
        modifier = modifier.fillMaxWidth(),
        enabled = !isLoading,
        onClick = {
            isLoading = true
            statusMessage = null
            RewardedAd.load(
                context,
                REWARDED_AD_UNIT_ID,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        isLoading = false
                        val activity = context as? Activity
                        if (activity == null) {
                            statusMessage = unavailableLabel
                            return
                        }
                        ad.show(activity) {
                            statusMessage = thanksLabel
                        }
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isLoading = false
                        statusMessage = unavailableLabel
                    }
                }
            )
        }
    ) {
        Text(if (isLoading) loadingLabel else stringResource(R.string.donate_button))
    }

    statusMessage?.let {
        Text(text = it)
    }
}
