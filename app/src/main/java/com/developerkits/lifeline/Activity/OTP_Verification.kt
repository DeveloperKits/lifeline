package com.developerkits.lifeline.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.developerkits.lifeline.databinding.ActivityOtpVerificationBinding

class OTP_Verification : AppCompatActivity() {
    private lateinit var binding: ActivityOtpVerificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpVerificationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.verifyButton.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, NIDScan::class.java))
        })

        binding.backButton.setOnClickListener(View.OnClickListener {
            onBackPressedDispatcher.onBackPressed()
        })
    }
}