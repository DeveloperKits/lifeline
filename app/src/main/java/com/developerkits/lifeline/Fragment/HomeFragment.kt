package com.developerkits.lifeline.Fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.developerkits.lifeline.Model.ContactAdd
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URL
import java.util.Locale

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val contactList = ArrayList<String>()
    private val PERMISSIONS_REQUEST_CODE = 100
    private var mapLink: String = " "
    val permissionsNeeded = mutableListOf<String>()
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //check permission and request for
        checkAndRequestPermissions()

        if (!isLocationEnabled()) {
            enableLocationDialog()
        }

        // Read infoMap from SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        binding.locationDetailTextView.text = sharedPreferences.getString("address", null)

        // set up video
        val videoView: VideoView = binding.sendMessage as VideoView
        val videoPath = "android.resource://com.developerkits.lifeline/" + R.raw.animated_button
        videoView.setVideoURI(Uri.parse(videoPath))

        // Enable looping
        videoView.setOnPreparedListener { mp: MediaPlayer -> mp.isLooping = true }

        // Start the video playback
        videoView.start()
        // -----------------

        val db = Firebase.firestore
        val auth = Firebase.auth

        getLastLocation()

        binding.medicalHelpCard.setOnClickListener{
            showConfirmationDialog("medical")
        }

        binding.fireServiceCard.setOnClickListener{
            showConfirmationDialog("fire")
        }

        binding.sendMessage.setOnClickListener{
            lifecycleScope.launch {
                db.collection("users")
                    .document(auth.currentUser!!.uid).collection("contacts")
                    .get()
                    .addOnSuccessListener { documents ->
                        if (!documents.isEmpty) {
                            for (document in documents) {
                                contactList.add(document.getString("number").toString())
                                showConfirmationDialog("send")
                            }
                        }else{
                            showCustomDialog("add contacts")
                        }
                    }
            }
        }
    }

    private fun enableLocationDialog() {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_enable_location, null)

        // Create the AlertDialog builder
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        // Create and show the dialog
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)

        val button = dialogView.findViewById<Button>(R.id.button2)

        button.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        dialog.show()
    }

    private fun getLastLocation() {
        val locationPermission = ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val locationCoarsePermission = ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    val geocoder = Geocoder(requireContext(), Locale.getDefault())
                    try {
                        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                        if (addresses!!.isNotEmpty()) {
                            val address = addresses[0]
                            val locality = address.locality // This is the city name, like "Dhaka"
                            val countryName = address.countryName // This is the country name

                            binding.locationDetailTextView.text =
                                "${address.locality}, ${address.countryName}"

                            val editor = sharedPreferences.edit()
                            editor.putString("address", "${address.locality}, ${address.countryName}")
                            editor.apply()

                            // Use locality and countryName as needed
                            Log.d("LocationInfo", "City: $locality, Country: $countryName")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    mapLink = "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                    Log.d("maps", mapLink)
                }
            }
        }else{
            checkAndRequestPermissions()
        }
    }

    fun sendSms(numbers: List<String>, message: String) {
        val smsManager = SmsManager.getDefault()
        for (number in numbers) {
            smsManager.sendTextMessage(number, null, message, null, null)
            Log.d("SmsCheck", "$number: $message")
        }
    }


    private fun showCustomDialog(string: String) {
        // Inflate the custom layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_multiple, null)

        // Create the AlertDialog builder
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(dialogView)
        // Create and show the dialog
        val dialog = dialogBuilder.create()
        dialog.setCancelable(false)

        // Access the custom dialog components
        val imageView = dialogView.findViewById<ImageView>(R.id.image)
        val textView = dialogView.findViewById<TextView>(R.id.text)
        val button = dialogView.findViewById<Button>(R.id.button)

        when (string) {
            "send" -> {
                Glide.with(requireContext())
                    .load(R.drawable.tick_circle)
                    .into(imageView)

                textView.text = "Your emergency is send Successfully"
                button.text = "Back"

                lifecycleScope.launch {
                    val shortUrl = shortenUrl(mapLink)
                    sendSms(contactList, "I'm in trouble\nHere's my location: $shortUrl")
                }
            }
            "medical" -> {
                Glide.with(requireContext())
                    .load(R.drawable.tick_circle)
                    .into(imageView)

                textView.text = "Your medical emergency is send to your nearest hospital"
                button.text = "Back"
            }
            "fire" -> {
                Glide.with(requireContext())
                    .load(R.drawable.tick_circle)
                    .into(imageView)

                textView.text = "Your fire emergency is send to your nearest fire service"
                button.text = "Back"
            }
        }

        button.setOnClickListener {
            when (button.text.toString()) {
                "Add" -> {
                    Toast.makeText(
                        requireContext(),
                        "Go contacts and add first contact! Thank You.",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                else -> dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showConfirmationDialog(string: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert Message")
            .setMessage("Are you want to send message?")

            .setNegativeButton("Yes") { dialog, which ->
                dialog.dismiss()
                dialog.dismiss()
                showCustomDialog(string)
            }
            .setPositiveButton("No") { dialog, which ->
                dialog.dismiss()
                dialog.dismiss()
            }
            .show()
    }

    suspend fun shortenUrl(url: String): String = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://tinyurl.com/api-create.php?url=$url")
                .build()

            val response = client.newCall(request).execute()
            response.body?.string() ?: url
        }catch (e: Exception){
            e.printStackTrace()
            Log.d("Errors", e.message.toString())
            url
        }
    }


    private fun checkAndRequestPermissions() {
        val locationPermission = ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val locationCoarsePermission = ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        val smsPermission = ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)

        if (locationPermission != PackageManager.PERMISSION_GRANTED &&
            locationCoarsePermission != PackageManager.PERMISSION_GRANTED) {

            permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (smsPermission != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.SEND_SMS)
        }

        if (permissionsNeeded.isNotEmpty()) {
            getLastLocation()
            ActivityCompat.requestPermissions(requireActivity(), permissionsNeeded.toTypedArray(), PERMISSIONS_REQUEST_CODE)
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Toast.makeText(
                        requireContext(),
                        "Permission request granted",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permission request denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    override fun onResume() {
        super.onResume()

        getLastLocation()
    }
}
