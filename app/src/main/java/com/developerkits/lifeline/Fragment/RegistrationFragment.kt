package com.developerkits.lifeline.Fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentRegistrationBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var storedVerificationId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpCall()

        binding.registerButton.setOnClickListener{
            val email = binding.emailText.text!!
            val number = binding.numberText.text!!

            if (email.isBlank()){
                binding.emailText.error = "Email field is empty"
                binding.emailText.requestFocus()

            }else if (number.isEmpty()){
                binding.numberText.error = "Number field is empty"
                binding.numberText.requestFocus()

            }else if (number.length != 11){
                binding.numberText.error = "Please enter correct number length"
                binding.numberText.requestFocus()

            }else if (!binding.checkBox.isChecked){
                Snackbar.make(binding.mainView, "You must agree to the terms", Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(resources.getColor(R.color.app_main_color))
                    .show()

            }else {
                startPhoneNumberVerification(number.toString())
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
                Toast
                    .makeText(context,
                        "Verification failed: ${e.message}",
                        Toast.LENGTH_LONG)
                    .show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                // Save verification ID
                storedVerificationId = verificationId

                // Navigate to OtpFragment
                val bundle = Bundle()
                bundle.putString("verificationID", verificationId)
                findNavController().navigate(R.id.registration_to_OTP_verification,)
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // Check if user with this phone number already exists
        auth.fetchSignInMethodsForEmail(phoneNumber)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (!signInMethods.isNullOrEmpty()) {
                        // User already exists, prompt to log in
                        Toast.makeText(context, "This number is already registered. Please log in.", Toast.LENGTH_LONG).show()
                    } else {
                        // User does not exist, proceed to send OTP
                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phoneNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(requireActivity())
                            .setCallbacks(callbacks)
                            .build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }
                } else {
                    // Handle error
                    Toast.makeText(context, "Failed to check user existence: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    Log.d("Fail --- ", task.exception?.message.toString())
                }
            }
    }
}