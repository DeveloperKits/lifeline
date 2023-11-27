package com.developerkits.lifeline.Fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentNidInfoBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

class NIDInfoFragment : Fragment() {

    private lateinit var binding: FragmentNidInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNidInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read infoMap from SharedPreferences
        var sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Retrieve JSON string from SharedPreferences
        val infoMapJson = sharedPreferences.getString("infoMap", null)

        // Convert JSON string back to infoMap
        val infoMapType = object : TypeToken<Map<String, String>>() {}.type
        val infoMap = Gson().fromJson<Map<String, String>>(infoMapJson, infoMapType)


        if (!infoMap.isNullOrEmpty()) {
            Log.d("Map Info:", infoMap.toString())
        }

        binding.nextPage.setOnClickListener{
            val nid = binding.nidNumberText.text.toString()
            val name = binding.applicantsNameText.text.toString()
            val father = binding.fatherNameText.text.toString()
            val mother = binding.motherNameText.text.toString()
            val bod = binding.birthDateText.text.toString()
            val address = binding.pAddressText.text.toString()

            if (nid.isNullOrBlank()){
                binding.nidNumberText.error = "This field is required"
            }else if (name.isNullOrBlank()){
                binding.applicantsNameText.error = "This field is required"
            }else if (father.isNullOrBlank()){
                binding.fatherNameText.error = "This field is required"
            }else if (mother.isNullOrBlank()){
                binding.motherNameText.error = "This field is required"
            }else if (bod.isNullOrBlank()){
                binding.birthDateText.error = "This field is required"
            }else if (address.isNullOrBlank()){
                binding.pAddressText.error = "This field is required"
            }else{
                saveData(nid, name, father, mother, bod, address)
            }
        }
    }

    private fun saveData(nid: String, name: String, father: String,
                         mother: String, bod: String, address: String) {

        val db = Firebase.firestore
        // Create a new user with a first and last name
        val user = hashMapOf(
            "nid" to nid,
            "name" to name,
            "father" to father,
            "mother" to mother,
            "bod" to bod,
            "address" to address,
        )

        val TAG = "messageForStore"


        lifecycleScope.launch {
            // Add a new document with a generated ID
            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document", e)
                }
        }

    }
}