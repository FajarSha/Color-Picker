package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.ads

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds

object Banner1 {
    fun show(context: Context, layout: FrameLayout, activity: Activity) {
        kotlin.runCatching {
            if (!activity.isFinishing) {
                MobileAds.initialize(context) {}
                val adView = AdView(context)
                adView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                adView.setAdSize(AdSize.FULL_BANNER)
                adView.adUnitId = context.getString(R.string.banner_high_id)

                val adRequest = AdRequest.Builder().build()
                activity.runOnUiThread {
                    layout.addView(adView)
                    adView.loadAd(adRequest)
                }
            }
        }
    }
}