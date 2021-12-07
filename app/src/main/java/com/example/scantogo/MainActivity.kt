package com.example.scantogo

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import com.example.scantogo.analyzer.QRScannerAnalyzer
import com.example.scantogo.databinding.ActivityMainBinding
import com.example.scantogo.enumeration.QRType
import com.example.scantogo.presentation.result_sheet.ResultBottomSheetDialog
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import org.jetbrains.anko.alert
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.io.IOException
import java.lang.Exception

class MainActivity : AppCompatActivity(), ResultBottomSheetDialog.OnDismissedListener {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var isFlashOn = false

    private var emailResultBottomSheetDialog: ResultBottomSheetDialog? = null
    private var smsResultBottomSheetDialog: ResultBottomSheetDialog? = null
    private var textResultBottomSheetDialog: ResultBottomSheetDialog? = null
    private var scanner = BarcodeScanning.getClient()

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if(isGranted) {
            startCamera()
        } else {
            toast("Permissions Denied").show()
        }
    }

    private val pickImageRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.let {
            analyzeImage(it.data!!)
        }
    }

    private val playStoreRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        //TODO To be Implemented
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        startCamera()
    }

    private val playStoreIntent: Intent by lazy { Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=<seach_query>&c=apps")) }

    private val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> by lazy {
        ProcessCameraProvider.getInstance(this)
    }

    private val cameraProvider: ProcessCameraProvider by lazy {
        cameraProviderFuture.get()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSplashScreen()
        setContentView(binding.root)

        initPermissions()
        initViews()
        initListener()
    }

    private fun initSplashScreen() {
        val splashScreen = installSplashScreen()
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
            ivFile.setImageDrawable(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_image_search))
            ivFlashSwitch.isVisible = false
        }

        intent?.let { intent ->
            if (intent.action == Intent.ACTION_SEND) {
                val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
                imageUri?.let {
                    analyzeImage(imageUri)
                } ?: run {
                    longToast(getString(R.string.general_image_not_found_message_text)).show()
                }
            }
        }
    }

    private fun initListener() {
        binding.ivFile.setOnClickListener {
            val chooseImage = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            pickImageRequest.launch(chooseImage)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCamera() {
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
                        it.setAnalyzer(
                            ContextCompat.getMainExecutor(this), QRScannerAnalyzer(
                                context = this,
                                onQRScannedSuccessful = { barcode ->
                                    runOnUiThread {
                                        validateBarcode(barcode)
                                    }
                                },
                                onQRScannedFailed = {

                                }
                            )
                        )
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

    @SuppressLint("MissingPermission")
    private fun analyzeImage(imageUri: Uri) {
        var image: InputImage? = null
        try {
            image = InputImage.fromFilePath(this, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        image?.let {
            val resultTask = scanner.process(it)
            resultTask
                .addOnSuccessListener {
                    val barcode = it.getOrNull(0)
                    barcode?.let {
                        validateBarcode(barcode)
                    }
                }
                .addOnFailureListener {

                }
        }
    }

    private fun validateBarcode(barcode: Barcode) {
        when(barcode.valueType) {
            Barcode.TYPE_EMAIL -> {
                val emailContent = barcode.email
                emailContent?.let {
                    val contents = arrayOf(
                        getString(R.string.email_template_to_text, it.address ?: "") ,
                        getString(R.string.email_template_subject_text, it.subject ?: ""),
                        getString(R.string.email_template_description_text, it.body ?: ""),
                    )

                    if (emailResultBottomSheetDialog != null) {
                        if (!emailResultBottomSheetDialog!!.isAdded) {
                            emailResultBottomSheetDialog!!.show(supportFragmentManager, ResultBottomSheetDialog.EMAIL_TAG)
                        }
                    } else {
                        emailResultBottomSheetDialog = ResultBottomSheetDialog.newInstance(contents, QRType.EMAIL.typeName)
                        emailResultBottomSheetDialog!!.show(supportFragmentManager, ResultBottomSheetDialog.EMAIL_TAG)
                    }
                }
            }

            Barcode.TYPE_SMS -> {
                barcode.sms?.let {
                    val contents = arrayOf(
                        getString(R.string.sms_template_phone_text, it.phoneNumber),
                        getString(R.string.sms_template_message_text, it.message)
                    )

                    if (smsResultBottomSheetDialog != null) {
                        if (!smsResultBottomSheetDialog!!.isAdded) {
                            smsResultBottomSheetDialog!!.show(supportFragmentManager, ResultBottomSheetDialog.SMS_TAG)
                        }
                    } else {
                        smsResultBottomSheetDialog = ResultBottomSheetDialog.newInstance(contents, QRType.SMS.typeName)
                        smsResultBottomSheetDialog!!.show(supportFragmentManager, ResultBottomSheetDialog.SMS_TAG)
                    }
                }
            }

            Barcode.TYPE_URL -> {
                val url = barcode.rawValue
                resolveUrl(url)
                cameraProvider.unbindAll()
            }

            else -> {
                val content = arrayOf(barcode.rawValue ?: "")
                if (textResultBottomSheetDialog != null) {
                    if (!textResultBottomSheetDialog!!.isAdded) {
                        textResultBottomSheetDialog!!.show(supportFragmentManager, ResultBottomSheetDialog.TEXT_TAG)
                    }
                } else {
                    textResultBottomSheetDialog = ResultBottomSheetDialog.newInstance(content, QRType.TEXT.typeName)
                    textResultBottomSheetDialog!!.show(supportFragmentManager, ResultBottomSheetDialog.TEXT_TAG)
                }
            }
        }
    }

    private fun resolveUrl(url: String) {
        try {
            startForResult.launch(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            )
        } catch (activityNotFound: ActivityNotFoundException) {
            alert("The link need browser to open.") {
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

    override fun onDismiss() {
        emailResultBottomSheetDialog?.run {
            dismiss()
            emailResultBottomSheetDialog = null
        }

        smsResultBottomSheetDialog?.run {
            dismiss()
            smsResultBottomSheetDialog = null
        }

        textResultBottomSheetDialog?.run {
            dismiss()
            textResultBottomSheetDialog = null
        }
    }

    override fun onContentCopy(content: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)?.let { clipboardManager ->
            val clipData = ClipData.newPlainText(content, content)
            clipboardManager.setPrimaryClip(clipData)
            longToast(getString(R.string.general_content_copied_text)).show()
        }
    }
}