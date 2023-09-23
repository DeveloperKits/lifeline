package com.developerkits.lifeline.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.ActivityNidscanBinding

class NIDScan : AppCompatActivity() {
    private lateinit var binding: ActivityNidscanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNidscanBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.backButton.setOnClickListener(View.OnClickListener {
            onBackPressedDispatcher.onBackPressed()
        })
    }
}