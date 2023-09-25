package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.ads

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

object NativeAd {
    private var currentNativeAd: NativeAd? = null
    private fun adViewInitializer(
        nativeAd: NativeAd?,
        adView: NativeAdView?,
        isWithMedia: Boolean
    ) {
        kotlin.runCatching {
            if (isWithMedia) {
                val mediaView = adView?.findViewById<MediaView>(R.id.ad_media)
                adView?.mediaView = mediaView
            }
            adView?.headlineView = adView?.findViewById(R.id.ad_headline)
            adView?.callToActionView = adView?.findViewById(R.id.ad_call_to_action)
            adView?.iconView = adView?.findViewById(R.id.ad_app_icon)
            adView?.bodyView = adView?.findViewById(R.id.ad_body)

            (adView?.headlineView as TextView).text = nativeAd?.headline

            if (nativeAd?.body == null) {
                adView.bodyView?.visibility = View.INVISIBLE
            } else {
                adView.bodyView?.visibility = View.VISIBLE
                (adView.bodyView as TextView).text = nativeAd.body
            }
            if (nativeAd?.callToAction == null) {
                adView.callToActionView?.visibility = View.INVISIBLE
            } else {
                adView.callToActionView?.visibility = View.VISIBLE
                (adView.callToActionView as Button).text = nativeAd.callToAction
            }

            if (nativeAd?.icon == null) {
                adView.iconView?.visibility = View.GONE
            } else {
                (adView.iconView as ImageView).setImageDrawable(
                    nativeAd.icon?.drawable
                )
                adView.iconView?.visibility = View.VISIBLE
            }
            adView.setNativeAd(nativeAd!!)

        }
    }


    @SuppressLint("InflateParams")
    fun showNativeAdvancedAd(
        activity: Activity,
        context: Context,
        adFrame: FrameLayout,
        isWithMedia: Boolean
    ) {
        kotlin.runCatching {
            if (!activity.isFinishing) {
                val unitId = context.getString(R.string.native_low_id)
                val builder = AdLoader.Builder(activity, unitId)
                builder.forNativeAd {
                    val activityDestroyed: Boolean = activity.isDestroyed
                    if (activityDestroyed || activity.isFinishing || activity.isChangingConfigurations) {
                        it.destroy()
                        return@forNativeAd
                    }
                    currentNativeAd?.destroy()
                    currentNativeAd = it

                    activity.runOnUiThread {
                        val adView = when {
                            isWithMedia -> activity.layoutInflater
                                .inflate(R.layout.native_ad_with_media, null) as NativeAdView
                            else -> activity.layoutInflater
                                .inflate(R.layout.native_ad_without_media, null) as NativeAdView
                        }

                        adViewInitializer(it, adView, isWithMedia)
                        adFrame.removeAllViews()
                        adFrame.addView(adView)
                    }


                }

                val adOptions = NativeAdOptions.Builder()
                    .build()

                builder.withNativeAdOptions(adOptions)
                val adLoader = builder.withAdListener(object : AdListener() {

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    }
                }).build()

                adLoader.loadAd(AdRequest.Builder().build())
            }
        }
    }
}