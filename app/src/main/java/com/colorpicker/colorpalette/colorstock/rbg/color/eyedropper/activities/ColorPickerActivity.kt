package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.activities

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.databinding.ActivityColorPickerBinding
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.utils.showSettings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ColorPickerActivity : AppCompatActivity() {

    private var binding: ActivityColorPickerBinding? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColorPickerBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        val uri2: String? = intent.getStringExtra("camera")

        val value = intent.getStringExtra("from")
        if (value == "camera")
        {
            binding?.icon?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_camera))
        }else{
            binding?.icon?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_gallery))
        }

        binding?.pickColorImage?.isDrawingCacheEnabled = true
        binding?.pickColorImage?.buildDrawingCache(true)
        binding?.pickColorImage?.setImageURI(uri2?.toUri())

        binding?.pickColorImage?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                bitmap = binding?.pickColorImage?.drawingCache
                // get touched pixel

                kotlin.runCatching {
                    val pixel = bitmap?.getPixel(event.x.toInt(), event.y.toInt())
                    val r = pixel?.let { Color.red(it) }
                    val g = pixel?.let { Color.green(it) }
                    val b = pixel?.let { Color.blue(it) }

                    // color name in Hexadecimal(#RRGGBB)
                    val code = "#${pixel?.let { Integer.toHexString(it) }}"
                    binding?.colorCode?.text = code

                    // fill the color in the view
//                binding.fillColor.setBackgroundColor(code.toInt())
                    if (pixel != null) {
                        binding?.color?.setCardBackgroundColor(pixel)
                    }
                }
                // get RGB values from the touched pixel
            }
            true
        }

        binding?.copy?.setOnClickListener {
            val text: String = binding?.colorCode?.text.toString()
            copyTxt(text)
        }

        binding?.icon?.setOnClickListener {
            if (value == "camera")
            {
                val intent = Intent(this, CustomCameraActivity::class.java)
                startActivity(intent)
                finish()

            }else{
                onRequestAllPermissionsClick()
            }
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
            val intent = Intent(this, ColorPickerActivity::class.java)
            intent.putExtra("camera", it.toString())
            startActivity(intent)
            finish()

        }
    }

    private fun copyTxt(text: String) {
        val clip = ClipData.newPlainText("Copied Text", text)
        val clipboardManager: android.content.ClipboardManager =
            getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        clipboardManager.setPrimaryClip(clip)
        Toast.makeText(this, "Code Copied", Toast.LENGTH_SHORT).show()
    }
}
