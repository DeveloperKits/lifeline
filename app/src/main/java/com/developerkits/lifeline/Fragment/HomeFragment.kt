package com.developerkits.lifeline.Fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import java.net.URL

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val contactList = ArrayList<String>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val PERMISSIONS_REQUEST_CODE = 100
    private var mapLink: String = " "

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // set up video
        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.animation_gif)
            .into(binding.sendMessage)
        // -----------------

        val db = Firebase.firestore
        val auth = Firebase.auth

        db.collection("users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                binding.locationDetailTextView.text = "${it.getString("address")}"
            }

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

    private fun getLastLocation() {
        val locationPermission = ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
        val locationCoarsePermission = ContextCompat
            .checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)

        if (locationPermission == PackageManager.PERMISSION_GRANTED &&
            locationCoarsePermission == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

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
                    // todo findNavController().navigate(R.id.) error
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
        val permissionsNeeded = mutableListOf<String>()

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
}
