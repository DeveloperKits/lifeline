package com.developerkits.lifeline.Fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentHomeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

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

        val db = Firebase.firestore
        val auth = Firebase.auth

        db.collection("users")
            .document(auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {
                binding.locationDetailTextView.text = "${it.getString("address")}"
            }

        binding.medicalHelpCard.setOnClickListener{
            showConfirmationDialog("medical")
        }

        binding.fireServiceCard.setOnClickListener{
            showConfirmationDialog("fire")
        }

        binding.imageView2.setOnClickListener{
            showConfirmationDialog("send")
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
                    //findNavController().navigate(R.id.)
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
                showCustomDialog(string)
            }
            .setPositiveButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}