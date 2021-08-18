package com.example.scantogo.Analyzer

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QRScannerAnalyzer(val onQRScannedSuccessful: (String) -> Unit,
                        val onQRScannedFailed: (Exception) -> Unit): ImageAnalysis.Analyzer {

    private val scanner: BarcodeScanner by lazy { BarcodeScanning.getClient(scannerOptions) }
    private val scannerOptions: BarcodeScannerOptions by lazy {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_PDF417
            )
            .build()
    }


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        mediaImage?.let {
            val image: InputImage = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            scanner.process(image)
                .addOnSuccessListener {
                    val barcode = it.getOrNull(0)

                    barcode?.rawValue?.let {
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