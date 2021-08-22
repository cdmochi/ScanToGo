package com.example.scantogo

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.example.scantogo.analyzer.QRScannerAnalyzer
import com.example.scantogo.databinding.ActivityMainBinding
import com.example.scantogo.presentation.result_sheet.ResultBottomSheetDialog
import com.google.mlkit.vision.barcode.Barcode
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivityForResult
import org.jetbrains.anko.toast
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var isFlashOn = false

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted) {
            startCamera()
        } else {
            toast("Permissions Denied").show()
        }
    }

    private val playStoreRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        startCamera()
    }

    private val playStoreIntent: Intent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=<seach_query>&c=apps")) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initPermissions()
        initViews()
    }

    private fun initPermissions() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                startCamera()
            }
            else -> {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun initViews() {
        with(binding) {
            ivFlashSwitch.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_flash_off))
            ivFlashSwitch.isVisible = false
        }
    }

    @SuppressLint("MissingPermission")
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

        val imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

        cameraProviderFuture.addListener(Runnable {
            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this), QRScannerAnalyzer(
                        onQRScannedSuccessful = { qrCodeContent, type ->
                            runOnUiThread {
                                if(type == Barcode.TYPE_URL) {
                                    resolveUrl(qrCodeContent)
                                    cameraProvider.unbindAll()
                                } else {
                                    ResultBottomSheetDialog.newInstance(qrCodeContent).show(supportFragmentManager, "")
                                    cameraProvider.unbindAll()
                                }
                            }
                        },
                        onQRScannedFailed = {

                        }
                    ))
                }

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    imageCapture,
                    preview,
                    imageAnalyzer
                )

                with(binding) {
                    when {
                        camera.cameraInfo.hasFlashUnit() -> {
                            binding.ivFlashSwitch.isVisible = true
                            ivFlashSwitch.setOnClickListener {
                                isFlashOn = !isFlashOn
                                setFlashlightViewState(isFlashOn)
                                camera.cameraControl.enableTorch(isFlashOn)
                            }
                            setFlashlightViewState(isFlashOn)
                        }
                        else -> {
                            ivFlashSwitch.isVisible = false
                        }
                    }
                }
            } catch (exception: Exception) {
                throw(exception)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun resolveUrl(url: String) {
        try {
            startForResult.launch(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        } catch (activityNotFound: ActivityNotFoundException) {
            alert("The link need browser to open. \n Please Download browser and try again") {
                neutralPressed("PLAYSTORE") {
                    playStoreRequest.launch(playStoreIntent)
                    it.dismiss()
                }
            }
        }
    }

    private fun setFlashlightViewState(isEnable: Boolean) {
        if(isEnable)
            binding.ivFlashSwitch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_off))
        else
            binding.ivFlashSwitch.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_flash_on))
    }
}