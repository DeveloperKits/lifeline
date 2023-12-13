package com.developerkits.lifeline.Fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
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
                binding.userNumber.text = "${auth.currentUser!!.phoneNumber}"
                binding.userName.text = "${it.getString("name")}"
            }

        binding.button.setOnClickListener{
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.app_name))
            .setMessage("Are you ant to log out?")

            .setNegativeButton("Yes") { dialog, which ->
                val sharedPreferences = requireContext()
                    .getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

                // Clear all data from SharedPreferences
                sharedPreferences.edit().clear().apply()
                findNavController().navigate(R.id.action_profileFragment_to_registrationFragment)
            }
            .setPositiveButton("No") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}