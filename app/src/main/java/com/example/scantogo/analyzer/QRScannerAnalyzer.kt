package com.example.scantogo.analyzer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

class QRScannerAnalyzer(val context: Context,
                        val uri: Uri? = null,
                        val onQRScannedSuccessful: (content: Barcode) -> Unit,
                        val onQRScannedFailed: (Exception) -> Unit): ImageAnalysis.Analyzer {

    private val scanner: BarcodeScanner by lazy { BarcodeScanning.getClient(scannerOptions) }
    private val scannerOptions: BarcodeScannerOptions by lazy {
        BarcodeScannerOptions.Builder()
                             .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                             .build()
    }

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {
               uri?.let { uri ->
                var imageFromFile: InputImage? = null
                try {
                    imageFromFile = InputImage.fromFilePath(context, uri)
                } catch(e: IOException) {
                    e.printStackTrace()
                }

                imageFromFile?.let { image ->
                    scanner.process(image)
                        .addOnSuccessListener {
                            val barcode = it.getOrNull(0)
                            barcode?.let {
                                onQRScannedSuccessful(it)
                            }
                        }
                        .addOnFailureListener {
                            onQRScannedFailed(it)
                        }
                        .addOnCompleteListener {
                            imageProxy.image?.close()
                            imageProxy.close()
                        }
                }
            }?: run {
                val image: InputImage = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
                scanner.process(image)
                    .addOnSuccessListener {
                        val barcode = it.getOrNull(0)
                        barcode?.let {
                            onQRScannedSuccessful(it)
                        }
                    }
                    .addOnFailureListener {
                        onQRScannedFailed(it)
                    }
                    .addOnCompleteListener {
                        imageProxy.image?.close()
                        imageProxy.close()
                    }
            }
        }
    }
}