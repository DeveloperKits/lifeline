package com.developerkits.lifeline.Fragment

import android.content.Intent
import android.net.Uri
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
            //sendData("heartAttack", "Heart Attack")
            openYouTubeVideo("https://youtu.be/6ZGg0zJUFEI?si=Ca9BdZ8-hmBERw5-")
        }

        binding.BreathlessnessCard.setOnClickListener{
            //sendData("breath", "Breathlessness")
            openYouTubeVideo("https://youtu.be/DUaxt8OlT3o?si=WAy_xekmCFWzJ0mD")
        }

        binding.StrokeCard.setOnClickListener{
            //sendData("stroke", "Stroke")
            openYouTubeVideo("https://youtu.be/EYUDS3wVWEk?si=Dza1kpPufgl3ed1I")
        }

        binding.UnconsciousnessCard.setOnClickListener{
            //sendData("uncons", "Unconsciousness")
            openYouTubeVideo("https://youtu.be/I-p_YnvOs-0?si=CtNsA7CssrGsh9Kw")
        }
    }

    private fun sendData(string: String, tittle: String){
        val bundle = Bundle()
        bundle.putString("doc", string)
        bundle.putString("tittle", tittle)
        findNavController().navigate(R.id.action_aidFragment_to_detailsFragment, bundle)
    }

    private fun openYouTubeVideo(videoId: String) {
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse(videoId))
        startActivity(appIntent)
    }

}