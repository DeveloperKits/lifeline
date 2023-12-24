package com.developerkits.lifeline.Fragment

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener{
            val number = binding.numberText.text!!

            if (number.isBlank()){
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
                editor.apply()

                val bundle = Bundle()
                bundle.putString("number", number.toString())
                bundle.putString("authType", "login")
                findNavController().navigate(R.id.loginFragment_to_OTP_verificationFragment, bundle)
            }
        }

        binding.textButton.setOnClickListener {
            findNavController().navigate(R.id.loginFragment_to_registrationFragment)
        }
    }

}