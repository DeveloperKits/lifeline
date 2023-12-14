package com.developerkits.lifeline.Fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentAidBinding

class AidFragment : Fragment() {

    private lateinit var binding: FragmentAidBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAidBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.heartAttachCard.setOnClickListener{
            sendData("heartAttack", "Heart Attack")
        }

        binding.BreathlessnessCard.setOnClickListener{
            sendData("breath", "Breathlessness")
        }

        binding.StrokeCard.setOnClickListener{
            sendData("stroke", "Stroke")
        }

        binding.UnconsciousnessCard.setOnClickListener{
            sendData("uncons", "Unconsciousness")
        }
    }

    private fun sendData(string: String, tittle: String){
        val bundle = Bundle()
        bundle.putString("doc", string)
        bundle.putString("tittle", tittle)
        findNavController().navigate(R.id.action_aidFragment_to_detailsFragment, bundle)
    }
}