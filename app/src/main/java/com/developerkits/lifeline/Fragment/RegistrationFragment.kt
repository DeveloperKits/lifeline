package com.developerkits.lifeline.Fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentRegistrationBinding
import com.google.android.material.snackbar.Snackbar

class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding

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

        binding.registerButton.setOnClickListener{
            val email = binding.emailText.text!!
            val number = binding.numberText.text!!

            if (email.isBlank()){
                binding.emailText.error = "Email field is empty"
                binding.emailText.requestFocus()

            }else if (number.isBlank()){
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
                // Navigate to OtpFragment
                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("number", number.toString())
                editor.putString("email", email.toString())
                editor.apply()

                val bundle = Bundle()
                bundle.putString("number", number.toString())
                bundle.putString("authType", "registration")
                findNavController().navigate(R.id.registration_to_OTP_verification, bundle)
            }
        }

        binding.textButton.setOnClickListener {
            findNavController().navigate(R.id.registrationFragment_to_loginFragment)
        }
    }


}