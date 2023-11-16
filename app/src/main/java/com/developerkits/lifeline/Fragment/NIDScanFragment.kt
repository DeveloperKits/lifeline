package com.developerkits.lifeline.Fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.developerkits.lifeline.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class NIDScanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_nid_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read infoMap from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Retrieve JSON string from SharedPreferences
        val infoMapJson = sharedPreferences.getString("infoMap", null)

        // Convert JSON string back to infoMap
        val infoMapType = object : TypeToken<Map<String, String>>() {}.type
        val infoMap = Gson().fromJson<Map<String, String>>(infoMapJson, infoMapType)

        // Now, infoMap contains the data saved in the first fragment

    }

}