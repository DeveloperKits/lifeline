package com.developerkits.lifeline.Fragment

import android.Manifest
import android.R.attr
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentCameraNidScanBinding
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraNidScanFragment : Fragment() {

    private lateinit var binding: FragmentCameraNidScanBinding
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    private lateinit var savedUri: Uri

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
                // Todo: pass bitmap in another fragment
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
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    binding.previewImage.visibility = View.VISIBLE
                    binding.previewImage.setImageBitmap(bitmap)
                    binding.previewImage.rotation = 90F
                    binding.submit.text = "Submit"
                    
                    // convert image to text and also if image are not clear then show a notification
                    convertToText(bitmap)
                    
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("TAG", "Photo capture failed: ${exception.message}", exception)
                }
            }
        )
    }

    private fun convertToText(bitmap: Bitmap?) {
        // Assuming 'bitmap' is your Bitmap object
        val inputImage = InputImage.fromBitmap(bitmap!!, 0) // 0 for rotation degrees

        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(inputImage)
            .addOnSuccessListener { text ->
                val recognizedText = text.text

                // Check for the presence of "National ID Card" in the text
                if (!recognizedText.contains("National ID Card", ignoreCase = true)) {
                    Snackbar.make(binding.mainLayout, "Invalid document. Please ensure you capture the correct NID card.", Snackbar.LENGTH_LONG)
                        .setAction("Ok") {}
                        .setTextColor(resources.getColor(R.color.white))
                        .setBackgroundTint(resources.getColor(R.color.app_main_color))
                        .setActionTextColor(resources.getColor(R.color.white))
                        .show()
                } else {
                    getInfoFromNID(recognizedText)
                }

                Log.i("Text Result", "Text : ${text.text}")
            }
            .addOnFailureListener { e ->
                // Handle the error
                // ...
            }

    }

    private fun getInfoFromNID(text: String) {
        val infoMap = mutableMapOf<String, String>()

        // Regex patterns to match the required fields
        val namePattern = Regex("Name:\\s*(.*)")
        val dobPattern = Regex("Date of Birth:\\s*(\\d{2} \\w{3} \\d{4})")
        val idNoPattern = Regex("ID NO:\\s*(\\d+)")

        // Try to find matches for the first type of card
        namePattern.find(text)?.let { infoMap["Name"] = it.groupValues[1].trim() }
        dobPattern.find(text)?.let { infoMap["Date of Birth"] = it.groupValues[1].trim() }
        idNoPattern.find(text)?.let { infoMap["ID No"] = it.groupValues[1].trim() }

        // If not found, try to find matches for the second type of card
        if (infoMap.isEmpty()) {
            //val altNamePattern = Regex("(?m)^([A-Z ]+)$") // Matches a line with all caps (name in 2nd type of card)
            val altNamePattern = Regex("Name\\s*\\n(.+)")
            val altDobPattern = Regex("Date of Birth\\s*(\\d{2} \\w{3} \\d{4})")

            altNamePattern.find(text)?.let { infoMap["Name"] = it.groupValues[1].trim() }
            altDobPattern.find(text)?.let { infoMap["Date of Birth"] = it.groupValues[1].trim() }

            // for number ... Regex to find a sequence of digits that may include spaces, with 10 or more digits in total
            val nidNumberPattern = Regex("(?:\\d\\s*){10,}")
            val matchResult = nidNumberPattern.find(text)

            if (matchResult != null) {
                // Remove all non-digit characters from the matched result to get the NID number
                val nidNumber = matchResult.value.filter { it.isDigit() }
                Log.i("NID Number", nidNumber)
                infoMap["ID No"] = nidNumber
            }
        }

        Log.i("NID Info", "Name: ${infoMap["Name"]}, " +
                "Date of Birth: ${infoMap["Date of Birth"]}, ID No: ${infoMap["ID No"]}")
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