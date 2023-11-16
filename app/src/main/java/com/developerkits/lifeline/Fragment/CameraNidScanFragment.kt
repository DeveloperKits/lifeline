package com.developerkits.lifeline.Fragment

import android.Manifest
import android.R.attr
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentCameraNidScanBinding
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.ByteArrayOutputStream

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraNidScanFragment : Fragment() {

    private lateinit var binding: FragmentCameraNidScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var isAllAreConverted = false
    private val infoMap = mutableMapOf<String, String>()
    private lateinit var bitmap: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCameraNidScanBinding.inflate(inflater, container, false)

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (allPermissionsGranted()) {
            startCamera(binding.centeredViewFinder)
        } else {
            activityResultLauncher.launch(REQUIRED_PERMISSIONS)
        }

        binding.submit.setOnClickListener {
            if (binding.submit.text == "Capture") {
                captureImage()
            } else if (binding.submit.text == "Submit") {

                if (isAllAreConverted) {
                    // add all value in share preference and go NidScan Fragment
                    val sharedPreferences =
                        requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()

                    // Convert infoMap to JSON string
                    val infoMapJson = Gson().toJson(infoMap)
                    // Save JSON string to SharedPreferences
                    editor.putString("infoMap", infoMapJson)

                    // Convert Bitmap to Base64 and save it to SharedPreferences
                    val base64String = bitmapToBase64(bitmap)
                    editor.putString("bitmap_key", base64String)

                    editor.apply()
                }

            }
        }

        binding.retake.setOnClickListener{
            binding.previewImage.visibility = View.GONE
            binding.submit.text = "Capture"
        }

    }

    private fun startCamera(viewFinder: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        imageCapture = ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3).build()

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                //Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun captureImage() {
        val imageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val buffer = image.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    //Todo : There image crop doesn't work and also some problem have in view
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.previewImage.visibility = View.VISIBLE
                    //binding.previewImage.setImageBitmap(bitmap)
                    binding.previewImage.rotation = 90F
                    binding.submit.text = "Submit"

                    Glide
                        .with(requireContext())
                        .load(bitmap)
                        .centerCrop()
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.previewImage)

                    // convert image to text and also if image are not clear then show a notification
                    convertToText(bitmap)
                    
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    addSnakeBar(exception.message.toString(), R.color.app_main_color)
                }
            }
        )
    }

    private fun convertToText(bitmap: Bitmap?) {

        val inputImage = InputImage.fromBitmap(bitmap!!, 0) // 0 for rotation degrees

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(inputImage)
            .addOnSuccessListener { text ->
                val recognizedText = text.text

                // Check for the presence of "National ID Card" in the text
                if (!recognizedText.contains("National ID Card", ignoreCase = true)) {
                    addSnakeBar("Invalid document. Please ensure you capture the correct NID card.", R.color.app_main_color)

                } else {
                    getInfoFromNID(recognizedText)
                }

            }
            .addOnFailureListener { e ->
                addSnakeBar(e.message.toString(), R.color.app_main_color)
            }
    }

    private fun getInfoFromNID(text: String) {

        var boolean = false

        val namePattern = Regex("Name:\\s*(.*)")
        val dobPattern = Regex("Date of Birth:\\s*(\\d{2} \\w{3} \\d{4})")
        val idNoPattern = Regex("ID NO:\\s*(\\d+)")

        namePattern.find(text)?.let { infoMap["Name"] = it.groupValues[1].trim() }
        dobPattern.find(text)?.let { infoMap["Date of Birth"] = it.groupValues[1].trim() }
        idNoPattern.find(text)?.let { infoMap["ID No"] = it.groupValues[1].trim() }


        // If not found, try to find matches for the second type of card
        if (infoMap.isEmpty()) {

            // Matches a line with all caps (name in 2nd type of card)
            val altNamePattern = Regex("(?m)^([A-Z ]+)$")

            // Define a regex pattern to match numbers in the specified format (3-3-4) = digit-word-digit
            val altDobPattern = Regex("\\s*(\\d{2} \\w{3} \\d{4})")

            // Define a regex pattern to match numbers in the specified format (3-3-4) all digit
            val altIdPattern = Regex("\\b(\\d{3} \\d{3} \\d{4})\\b")

            altNamePattern.find(text)?.let { infoMap["Name"] = it.groupValues[1].trim() }
            altDobPattern.find(text)?.let { infoMap["Date of Birth"] = it.groupValues[1].trim() }
            altIdPattern.find(text)?.let { infoMap["ID No"] = it.groupValues[1].replace(" ", "") }

            boolean = true
        }

        // check 2 or more infoMap value are null
        if (infoMap["Name"]!!.isBlank() && infoMap["Date of Birth"]!!.isBlank() && infoMap["ID No"]!!.isBlank() ||
            infoMap["Name"]!!.isBlank() && infoMap["Date of Birth"]!!.isBlank() ||
            infoMap["Date of Birth"]!!.isBlank() && infoMap["ID No"]!!.isBlank() ||
            infoMap["Name"]!!.isBlank() && infoMap["ID No"]!!.isBlank()){

            addSnakeBar("Image is not clear please capture again and don't shake hands", R.color.app_main_color)
        }else {
            // all are done now
            isAllAreConverted = true
        }

        Log.i("NID Info", "Name: ${infoMap["Name"]}, " +
                "Date of Birth: ${infoMap["Date of Birth"]}, ID No: ${infoMap["ID No"]}, Enter: $boolean")
    }

    private fun addSnakeBar(message: String, background: Int){
        Snackbar
            .make(binding.mainLayout, message, Snackbar.LENGTH_LONG)
            .setAction("Ok") {}
            .setTextColor(resources.getColor(R.color.white))
            .setBackgroundTint(resources.getColor(background))
            .setActionTextColor(resources.getColor(R.color.white))
            .show()
    }

    // Convert Bitmap to Base64
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }


    // Camera permission
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private val REQUIRED_PERMISSIONS =
        mutableListOf (
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()


    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        var permissionGranted = true
        permissions.entries.forEach {
            if (it.key in REQUIRED_PERMISSIONS && !it.value)
                permissionGranted = false
        }
        if (!permissionGranted) {
            Toast.makeText(
                requireContext(),
                "Permission request denied",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            startCamera(binding.centeredViewFinder)
        }
    }


}