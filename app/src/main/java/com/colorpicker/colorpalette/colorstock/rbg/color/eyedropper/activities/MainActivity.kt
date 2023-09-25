package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.Ads_Util.InAppBilling
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.BuildConfig
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.utils.CompanionClass.Companion.counter
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.ads.Banner
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.ads.InterstitialActivity
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.ads.NativeAd
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.databinding.ActivityMainBinding
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.utils.showSettings
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null
    private var imageUri: Uri? = null
    private lateinit var bottomSheetExitDialog: BottomSheetDialog
    private lateinit var inAppDialog: Dialog
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var subBtn: CardView
    private lateinit var inAppBilling: InAppBilling
    var isInAppPurched = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        inAppBilling = InAppBilling(this, this);
        isInAppPurched = inAppBilling.hasUserBoughtInApp()
        counter++
        val animation1 = AnimationUtils.loadAnimation(applicationContext, R.anim.button_scale)
        animation1.repeatCount = Animation.INFINITE
        animation1.repeatMode = Animation.REVERSE
        actionBarToggle =
            ActionBarDrawerToggle(this, binding?.drawerLayout, R.string.open, R.string.close)
        binding?.drawerLayout?.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()
        binding?.navView?.setNavigationItemSelectedListener(this)
        if (!isInAppPurched) {
            CoroutineScope(Dispatchers.IO).launch {
                binding?.adView?.let { Banner.show(this@MainActivity, it, this@MainActivity) }
            }
        }



        bottomSheetExitDialog = BottomSheetDialog(this)
        val exitDialogLayout = layoutInflater.inflate(R.layout.exit_dialog, null)
        bottomSheetExitDialog.setContentView(exitDialogLayout)
        bottomSheetExitDialog.findViewById<FrameLayout>(R.id.adFrame)?.let {
            CoroutineScope(Dispatchers.IO).launch {
                NativeAd.showNativeAdvancedAd(this@MainActivity, this@MainActivity, it, false)
            }
        }
        binding?.adBtn?.startAnimation(animation1)
        inAppBilling = InAppBilling(this, this)
        isInAppPurched = inAppBilling.hasUserBoughtInApp();
        if (isInAppPurched) {
            binding?.adBtn?.visibility = View.GONE;
        } else {
            setInAppDialog();
            inAppDialog.show();
        }
        binding?.adBtn?.setOnClickListener {
            setInAppDialog();
            if (inAppDialog != null) {
                if (!isInAppPurched) {
                    inAppDialog.show();
                }

            }
        }

        binding?.camera?.setOnClickListener {

            if ((counter % 2) == 1) {
                if (isInAppPurched) {
                    val intent = Intent(this, CustomCameraActivity::class.java)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, InterstitialActivity::class.java)
                    resultLauncher.launch(intent)
                }

            } else {
                val intent = Intent(this, CustomCameraActivity::class.java)
                startActivity(intent)
            }

        }

        binding?.gallery?.setOnClickListener {
            onRequestAllPermissionsClick()
        }

        binding?.menu?.setOnClickListener {
            binding?.drawerLayout?.openDrawer(
                GravityCompat.START
            )
        }
    }

    private fun onRequestAllPermissionsClick() {
        CoroutineScope(Dispatchers.IO).launch {
            kotlin.runCatching {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) readPermissionCallback.launch(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                else readPermissionCallback.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private val readPermissionCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission: Boolean ->
            if (permission) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) writePermissionCallback.launch(
                    Manifest.permission.READ_MEDIA_IMAGES
                )
                else writePermissionCallback.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                MaterialAlertDialogBuilder(this).setTitle("Permission Required")
                    .setMessage("Write External Storage permissions are required for this purpose")
                    .setCancelable(false).setNegativeButton("Deny") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(
                            this,
                            "Write External Storage permissions are required for this purpose",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.setPositiveButton("Grant") { _, _ ->
                        this.showSettings()
                    }.show()
            }
        }

    private val writePermissionCallback =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { permission: Boolean ->
            if (permission) {
                getImageContract.launch("image/*")
            } else {
                MaterialAlertDialogBuilder(this).setTitle("Permission Required")
                    .setMessage("Read External Storage permissions are required for this purpose")
                    .setCancelable(false).setNegativeButton("Deny") { dialog, _ ->
                        dialog.dismiss()
                        Toast.makeText(
                            this,
                            "Read External Storage permissions are required for this purpose",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.setPositiveButton("Grant") { _, _ ->
                        this.showSettings()
                    }.show()
            }
        }

    private val getImageContract = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            imageUri = it


            if ((counter % 2) == 0) {
                if(isInAppPurched){
                    val intent = Intent(this, ColorPickerActivity::class.java)
                    intent.putExtra("camera", imageUri.toString())
                    counter++
                    startActivity(intent)
                }
                else{
                    val intent = Intent(this, InterstitialActivity::class.java)
                    resultLauncher1.launch(intent)
                }
            } else {
                val intent = Intent(this, ColorPickerActivity::class.java)
                intent.putExtra("camera", imageUri.toString())
                counter++
                startActivity(intent)
            }
        }
    }

    override fun onBackPressed() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawers()
        } else {
            showNativeAdExitDialog();
        }
//        exitDialog()
    }

    private fun exitDialog() {
        bottomSheetExitDialog.show()
        bottomSheetExitDialog.findViewById<TextView>(R.id.exitBtn)?.setOnClickListener {
            bottomSheetExitDialog.dismiss()
            finishAffinity()
        }
    }

    private fun closeDrawer() {
        if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
            binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        }
    }

    private fun rateIntentForUrl(url: String): Intent {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url + packageName))
        var flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        flags =
            flags or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
        intent.addFlags(flags)
        return intent
    }

    private fun rateApp() {
        try {
            val rateIntent = rateIntentForUrl("market://details?id=")
            startActivity(rateIntent)
        } catch (e: ActivityNotFoundException) {
            val rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details?id=")
            startActivity(rateIntent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.privacy_policy -> {

                kotlin.runCatching {
                    closeDrawer()
                    val i = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sites.google.com/view/bhagowalhood/home")
                    )
                    if (i.resolveActivity(packageManager) != null) startActivity(i)
                }

            }

            R.id.terms_conditions -> {
                kotlin.runCatching {
                    closeDrawer()
                    val i = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://sites.google.com/view/bhagowalhood/home")
                    )
                    if (i.resolveActivity(packageManager) != null) startActivity(i)
                }
            }

            R.id.rate_us -> {
                rateApp()
                closeDrawer()
            }

            R.id.share -> {
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey check out my app at: https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID
                )
                sendIntent.type = "text/plain"
                if (sendIntent.resolveActivity(packageManager) != null) {
                    startActivity(sendIntent)
                }
                closeDrawer()
            }

            R.id.manage_subscription -> {

                kotlin.runCatching {
                    closeDrawer()
                    val i = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/account/subscriptions")
                    )
                    if (i.resolveActivity(packageManager) != null) startActivity(i)
                }
            }
        }
        return true
    }

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, CustomCameraActivity::class.java)
                startActivity(intent)
            }
        }
    private var resultLauncher1 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, ColorPickerActivity::class.java)
                intent.putExtra("camera", imageUri.toString())
                counter++
                startActivity(intent)
            }
        }

    private fun setInAppDialog() {
        val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.button_scale)
        animation.repeatCount = Animation.INFINITE
        animation.repeatMode = Animation.REVERSE
        inAppDialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        val view1 = layoutInflater.inflate(R.layout.inapp_layout, null)
        inAppDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        inAppDialog?.setContentView(view1)
        val window = inAppDialog.window
        val wlp = window?.attributes
        val closebtn = view1.findViewById<ImageView>(R.id.cross_btn)
        subBtn = view1.findViewById(R.id.subscribeNow)
        subBtn.startAnimation(animation)
        closebtn.setOnClickListener {
            if (inAppDialog.isShowing) {
                inAppDialog.dismiss()
            }
        }
        subBtn.setOnClickListener {
            inAppBilling.purchase(view1)
        }
        wlp?.gravity = Gravity.CENTER
        wlp?.flags = wlp?.flags?.and(WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv())
        window?.attributes = wlp
        inAppDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
    }

    override fun onResume() {
        super.onResume()
        inAppBilling = InAppBilling(this, this);
        var isPurchase=inAppBilling.isCurrentpurchased();
        if(isPurchase){
            val intent = Intent(this@MainActivity, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
        else{
            isInAppPurched = inAppBilling.hasUserBoughtInApp();
            if (isInAppPurched) {
                binding?.adView?.visibility = View.GONE
            } else {
                binding?.adView?.visibility = View.VISIBLE
                CoroutineScope(Dispatchers.IO).launch {
                    binding?.adView?.let { Banner.show(this@MainActivity, it, this@MainActivity) }
                }
            }
        }

    }


    fun showNativeAdExitDialog() {
        val exitDialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        val view1 = LayoutInflater.from(this).inflate(R.layout.my_exit_dialog, null)
        exitDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        exitDialog.setContentView(view1)
        val inAppBilling = InAppBilling(this, this)
        val isInAppPurchased = inAppBilling.hasUserBoughtInApp()

        val window = exitDialog.window
        val wlp = window?.attributes
        wlp?.gravity = Gravity.CENTER
        wlp?.flags = wlp?.flags?.and(WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv())
        window?.attributes = wlp
        exitDialog.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )

        val adnativeLayout = view1.findViewById<ConstraintLayout>(R.id.adlayoutNative)


        val is_In_App_Purchased = inAppBilling.hasUserBoughtInApp()

        if (is_In_App_Purchased) {
            adnativeLayout?.visibility = View.GONE
        } else {
            showBannerAd(adnativeLayout, this)
            adnativeLayout?.visibility = View.VISIBLE
        }

        val yesBtn = view1.findViewById<CardView>(R.id.yesBtn)
        val NoBtn = view1.findViewById<CardView>(R.id.Nobtn)

        yesBtn.setOnClickListener {
            if (exitDialog.isShowing) {
                exitDialog.dismiss()
            }
            finishAffinity()
        }

        NoBtn.setOnClickListener {
            exitDialog.dismiss()
        }
        exitDialog.show()
    }

    private fun showBannerAd(adLayout: ConstraintLayout, context: Context) {
        val adRequest = AdRequest.Builder().build()
        val adView = AdView(context)
        adView.adUnitId = context.getString(R.string.banner_medium_id)
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE)
        adView.loadAd(adRequest)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        adLayout.addView(adView, params)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                if (loadAdError != null && loadAdError.code == AdRequest.ERROR_CODE_NO_FILL) {

                }
            }
        }
    }

}