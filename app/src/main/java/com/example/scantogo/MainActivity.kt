package com.example.scantogo

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.scantogo.Analyzer.QRScannerAnalyzer
import com.example.scantogo.databinding.ActivityMainBinding
import com.github.kittinunf.fuel.Fuel
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import org.jetbrains.anko.alert
import org.jetbrains.anko.progressDialog
import org.jetbrains.anko.toast
import java.lang.Exception
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if(isGranted) {
            startCamera()
        } else {
            toast("Permissions Denied").show()
        }
    }
    private val webIntent: Intent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse(qrCodeContent)) }

    private var qrCodeContent = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) -> {
                startCamera()
            }
            else -> {
                cameraPermissionRequest.launch(Manifest.permission.CAMERA)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCamera() {
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
                        onQRScannedSuccessful = { qrCodeContent ->
                            runOnUiThread {
                                if(qrCodeContent.contains("http://") || qrCodeContent.contains("https://") ) {
                                    this.qrCodeContent = qrCodeContent
                                    startActivity(webIntent)
                                } else {
                                    toast("$qrCodeContent").show()
                                }
                            }
                        },
                        onQRScannedFailed = {

                        }
                    ))
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    imageCapture,
                    preview,
                    imageAnalyzer
                )
            } catch (exception: Exception) {
                throw(exception)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun callApi() {
        with(binding) {
            if(etApiText.editText?.text!!.isEmpty()) {
                toast("API Field is Empty!").show()
            } else {
                val BASE_URL = etApiText.editText?.text.toString()
                Fuel.post(BASE_URL)
                    .body(
                        createMock(
                            payAmount = 1000.toFloat(),
                            userId = 1.toLong(),
                            rentingTranId = 1.toLong()
                        )
                    ).responseString { request, response, result ->
                        if(response.statusCode == 200) {
                            alert("Successful: ${result.component1().toString()}") {
                                positiveButton("DISMISS") {
                                    it.dismiss()
                                }
                            }
                        } else {
                            alert("Failed: ${result.component1().toString()}")
                                .positiveButton("DISMISS") {
                                    it.dismiss()
                                }
                        }
                    }

            }
        }
    }

    private fun createMock(payAmount: Float, userId: Long, rentingTranId: Long): String {
        return """{
                     pay_amount : $payAmount ",
                     paid_date_time : "",
                     user_id: $userId,
                     renting_transaction_id: "$rentingTranId"
                  }"""
    }
}