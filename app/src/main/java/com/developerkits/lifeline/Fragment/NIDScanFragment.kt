package com.developerkits.lifeline.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentNidScanBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NIDScanFragment : Fragment() {

    private lateinit var binding: FragmentNidScanBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNidScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read infoMap from SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Retrieve JSON string from SharedPreferences
        val infoMapJson = sharedPreferences.getString("infoMap", null)
        val front = sharedPreferences.getString("Front", null)
        val back = sharedPreferences.getString("Back", null)

        // Convert JSON string back to infoMap
        val infoMapType = object : TypeToken<Map<String, String>>() {}.type
        val infoMap = Gson().fromJson<Map<String, String>>(infoMapJson, infoMapType)

        // Now, infoMap contains the data saved in the first fragment
        binding.nextPage.setOnClickListener {
            if (!front.isNullOrEmpty() && !back.isNullOrEmpty()){
                Log.d("Map Info:", infoMap.toString())
                findNavController().navigate(R.id.action_NIDScanFragment_to_NIDInfoFragment)
            }
        }

        binding.backPage.setOnClickListener {
            if (back == null){
                bundle.putString("type", "Back")
                findNavController().navigate(R.id.NIDScan_to_cameraNidScan, bundle)
            }
        }

        binding.frontPage.setOnClickListener {
            if (front == null) {
                bundle.putString("type", "Front")
                findNavController().navigate(R.id.NIDScan_to_cameraNidScan, bundle)
            }
        }

        if (back != null) {
            Glide
                .with(requireContext())
                .load(R.drawable.camera_done)
                .into(binding.backPage)
        }

        if (front != null){
            Glide
                .with(requireContext())
                .load(R.drawable.camera_done)
                .into(binding.frontPage)
        }

    }

}