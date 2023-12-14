package com.developerkits.lifeline.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.developerkits.lifeline.databinding.FragmentDetailsBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class DetailsFragment : Fragment() {

    private lateinit var binding: FragmentDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pageName.text = arguments?.getString("tittle").toString()

        val db = Firebase.firestore

        db.collection("other")
            .document(arguments?.getString("doc").toString())
            .get()
            .addOnSuccessListener {
                binding.des.text = "${it.getString("des")}"
            }

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }
    }
}