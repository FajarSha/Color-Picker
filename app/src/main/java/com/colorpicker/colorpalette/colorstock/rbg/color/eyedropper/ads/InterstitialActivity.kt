package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.ads

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class InterstitialActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interstitial)
        load(this)
    }

    private fun load(context: Context) {
        kotlin.runCatching {
            val adRequest = AdRequest.Builder().build()
            val unitId = context.getString(R.string.interstitial_high_id)

            InterstitialAd.load(context, unitId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                   Mediumload(context)
                    Log.d("TAG", "onAdFailedToLoad:high  ")
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    kotlin.runCatching {
                        interstitialAd.show(this@InterstitialActivity)
                        interstitialAd.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                    }.onFailure {}
                }
            })
        }
    }

    private fun Mediumload(context: Context) {
        kotlin.runCatching {
            val adRequest = AdRequest.Builder().build()
            val unitId = context.getString(R.string.interstitial_medium_id)

            InterstitialAd.load(context, unitId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    lowload(context);
                    Log.d("TAG", "onAdFailedToLoad: Medium  ")
//                    setResult(Activity.RESULT_OK)
//                    finish()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    kotlin.runCatching {
                        interstitialAd.show(this@InterstitialActivity)
                        interstitialAd.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                    }.onFailure {}
                }
            })
        }
    }

    private fun lowload(context: Context) {
        kotlin.runCatching {
            val adRequest = AdRequest.Builder().build()
            val unitId = context.getString(R.string.interstitial_low_id)

            InterstitialAd.load(context, unitId, adRequest, object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("TAG", "onAdFailedToLoad: low  ")

                    setResult(Activity.RESULT_OK)
                    finish()
                }

                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    kotlin.runCatching {
                        interstitialAd.show(this@InterstitialActivity)
                        interstitialAd.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    super.onAdDismissedFullScreenContent()
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }

                                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                    super.onAdFailedToShowFullScreenContent(p0)
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                    }.onFailure {}
                }
            })
        }
    }
}