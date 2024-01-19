package com.developerkits.lifeline.Fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentOtpVerificationBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class OTP_verificationFragment : Fragment() {

    private lateinit var binding: FragmentOtpVerificationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = null
    private lateinit var progressDialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOtpVerificationBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCall()
        setupOtpFields()
        initProgressDialog()

        auth = FirebaseAuth.getInstance()

        val number = arguments?.getString("number")
        startPhoneNumberVerification(number!!)
        getStyledTimeText(number)

        binding.backButton.setOnClickListener{
            findNavController().navigate(R.id.OTP_verification_to_registration)
        }

        binding.verifyButton.setOnClickListener{
            val code1 = binding.code1Text.text.toString()
            val code2 = binding.code2Text.text.toString()
            val code3 = binding.code3Text.text.toString()
            val code4 = binding.code4Text.text.toString()
            val code5 = binding.code5Text.text.toString()
            val code6 = binding.code6Text.text.toString()
            val otp = code1+code2+code3+code4+code5+code6

            if (storedVerificationId != null && otp.isNotEmpty()) {
                val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, otp)

                progressDialog.show()
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun setupOtpFields() {
        val editTexts = listOf(
            binding.code1Text,
            binding.code2Text,
            binding.code3Text,
            binding.code4Text,
            binding.code5Text,
            binding.code6Text
        )
        var lastKeyCode  = 0
        var lastKeyTime : Long = 0

        editTexts.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s != null && s.length == 1) {
                        if (index < editTexts.size - 1) {
                            editTexts[index + 1].requestFocus()
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s != null && s.length > 1) {
                        val latestChar = s.toString().last()
                        s.clear()
                        s.append(latestChar)
                    }
                }
            })

            editText.setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    val currentTime = System.currentTimeMillis()
                    if (keyCode == lastKeyCode && (currentTime - lastKeyTime) < 500) {
                        // Double tap detected
                        if (index > 0) {
                            editTexts[index - 1].apply {
                                text?.clear()
                                requestFocus()
                            }
                        }
                    } else {
                        if (editText.text!!.isEmpty() && index > 0) {
                            editTexts[index - 1].requestFocus()
                        }
                    }
                    lastKeyCode = keyCode
                    lastKeyTime = currentTime
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun setUpCall() {
        auth = FirebaseAuth.getInstance()

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This can be ignored if you are using the manual code entry flow.
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // Handle the error
                progressDialog.dismiss()
                Toast
                    .makeText(requireContext(),
                        "Verification failed: ${e.message}",
                        Toast.LENGTH_LONG)
                    .show()

                Log.d("Fail verification: ", e.message.toString() + e.toString())
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // Save verification ID
                storedVerificationId = verificationId
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // User does not exist, proceed to send OTP
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+88$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.clear()
                    editor.putString("UID", task.result.user!!.uid)
                    editor.apply()
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Verification Successful", Toast.LENGTH_LONG).show()

                    Log.d("UserUID", task.result.user!!.uid)
                    if (arguments?.getString("authType") == "login"){
                        editor.putBoolean("isFullRegistrationComplete", true)
                        editor.apply()
                        findNavController().navigate(R.id.OTP_verificationFragment_to_homeFragment)
                    }else {
                        findNavController().navigate(R.id.OTP_verification_to_NIDScan)
                    }

                } else {
                    // Sign in failed
                    progressDialog.dismiss()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(requireContext(), "Your verification are invalid", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(requireContext(), "Verification Failed", Toast.LENGTH_LONG).show()
                    }

                    //Log.d("Fail to create account: ", task.exception.toString())
                }
            }
    }

    private fun initProgressDialog() {
        // Create a ProgressBar programmatically
        val progressBar = ProgressBar(context).apply {
            isIndeterminate = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER
            }
        }

        // Create an AlertDialog for the progress dialog
        progressDialog = AlertDialog.Builder(requireContext()).apply {
            setTitle("Logging in...")
            setView(progressBar)
            setCancelable(false) // equivalent to setCanceledOnTouchOutside(false)
        }.create()
    }

    private fun getStyledTimeText(num: String) {
        val fullText = "An authentication code has been sent to $num"
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf(num)
        if (startIndex != -1) {
            val endIndex = startIndex + num.length
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.app_main_color)),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.verificationNumber.text = spannableString
    }
}