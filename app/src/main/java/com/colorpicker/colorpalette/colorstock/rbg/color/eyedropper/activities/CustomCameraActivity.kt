package com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.utils.CompanionClass
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.R
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.databinding.ActivityCustomCameraBinding
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.utils.showShortToast
import com.colorpicker.colorpalette.colorstock.rbg.color.eyedropper.utils.Constants
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CustomCameraActivity : AppCompatActivity() {

    private var binding: ActivityCustomCameraBinding? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var outputDirectory: File
    private var cameraExecutor: ExecutorService? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    var tag = "CustomCameraActivity"
    var savedUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomCameraBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        CompanionClass.counter++

        CoroutineScope(Dispatchers.IO).launch {
            outputDirectory = getOutputDirectory()
            withContext(Dispatchers.Main) {
                cameraExecutor = Executors.newSingleThreadExecutor()
                cameraProviderFuture = ProcessCameraProvider.getInstance(this@CustomCameraActivity)
                if (allPermissionGranted()) {
                    startCamera()
                } else {
                    ActivityCompat.requestPermissions(
                        this@CustomCameraActivity,
                        Constants.REQUESTED_PERMISSIONS,
                        Constants.REQUEST_CODE_PERMISSION
                    )
                }
            }
        }

        binding?.takePictureBtn?.setOnClickListener {
            binding?.takePictureBtn?.isEnabled = false
            binding?.takePictureBtn?.isClickable = false
            takePhoto()
        }

        binding?.switchBtn?.setOnClickListener {

            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            // restart the camera
            startCamera()
        }
        binding?.closeCameraBtn?.setOnClickListener {
            finish()
        }
        binding?.flashIcon?.setOnClickListener {
            changeFlashMode()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == Constants.REQUEST_CODE_PERMISSION) {
            if (allPermissionGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "permission not granted", Toast.LENGTH_SHORT).show()
                finish()
            }

        }
    }

    private fun changeFlashMode() {
        when {
            binding?.flashIcon?.tag?.equals("1") == true -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_ON
                binding?.flashIcon?.setImageResource(R.drawable.ic_flash_on)
                binding?.flashIcon?.tag = "0"
            }
            binding?.flashIcon?.tag?.equals("0") == true -> {
                imageCapture?.flashMode = ImageCapture.FLASH_MODE_OFF
                binding?.flashIcon?.setImageResource(R.drawable.ic_flash_off)
                binding?.flashIcon?.tag = "1"
            }
        }
    }

    private fun takePhoto() {

        kotlin.runCatching {
            // Get a stable reference of the
            // modifiable image capture use case
            val imageCapture = imageCapture ?: return

            // Create time-stamped output file to hold the image
            val photoFile = File(
                outputDirectory, SimpleDateFormat(
                    Constants.FILE_NAME_FORMAT, Locale.getDefault()
                ).format(System.currentTimeMillis()) + ".jpg"
            )

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile)
                .build()
            imageCapture.takePicture(outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        Log.e(tag, "Photo capture failed: ${exc.message}", exc)
                    }

                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        savedUri = Uri.fromFile(photoFile)

                        binding?.takePictureBtn?.isEnabled = true
                        binding?.takePictureBtn?.isClickable = true

                        val intent = Intent(this@CustomCameraActivity, ColorPickerActivity::class.java)
                        intent.putExtra("camera", savedUri.toString())
                        intent.putExtra("from","camera")
                        startActivity(intent)
                        finish()
                    }
                })
        }
    }

    private fun startCamera() {
        kotlin.runCatching {
            val cameraProvidedFuture = ProcessCameraProvider.getInstance(this)
            cameraProvidedFuture.addListener(
                {
                    val cameraProvider: ProcessCameraProvider = cameraProvidedFuture.get()
                    if (cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) && cameraProvider.hasCamera(
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        )
                    ) {
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(binding?.viewFinder?.surfaceProvider)
                        }
                        imageCapture = ImageCapture.Builder().build()
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                this, cameraSelector, preview, imageCapture
                            )

                        } catch (exc: Exception) {
                            Log.e(tag, "Use case binding failed", exc)
                        }
                    } else {
                        showShortToast("Device Camera is not supported!")
                        finish()
                    }
                }, ContextCompat.getMainExecutor(this)
            )
        }
    }

    private suspend fun getOutputDirectory(): File {
        val returnVal = CoroutineScope(Dispatchers.IO).async {
            val mediaDir = externalMediaDirs.firstOrNull()?.let {
                File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return@async if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
        }
        return returnVal.await()
    }

    private fun allPermissionGranted() = Constants.REQUESTED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor?.shutdown()
    }

}