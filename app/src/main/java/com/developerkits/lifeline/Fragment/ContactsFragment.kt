package com.developerkits.lifeline.Fragment

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.developerkits.lifeline.Adapter.ContactsAdapter
import com.developerkits.lifeline.Model.Contact
import com.developerkits.lifeline.R
import com.developerkits.lifeline.databinding.FragmentContactsBinding

class ContactsFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentContactsBinding.inflate(inflater, container, false)

        // check user are give permission to read their phone contact
        requestContactPermission()

        // back button click and shift home fragment
        binding.backButton.setOnClickListener{ findNavController().navigate(R.id.contacts_to_home) }

        return binding.root
    }

    private fun requestContactPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // load all contact
                loadContacts()

            }else -> {
                // You can directly ask for the permission.
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(
                    Manifest.permission.READ_CONTACTS)
            }
        }
    }

    private fun loadContacts() {
        val contacts = getContacts()
        if (contacts.isEmpty()){
            // Todo add a Textview or image
        }else {
            val sortedList = contacts.sortedBy { it.name }
            val adapter = ContactsAdapter(this, sortedList)
            binding.phoneContacts.layoutManager = LinearLayoutManager(requireContext())
            binding.phoneContacts.adapter = adapter
        }
    }


    // get all contact list from mobile
    @SuppressLint("Range")
    private fun getContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val resolver = requireContext().contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))

                val number =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

                var photo =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI))

                if (photo == null){
                    photo = " "
                }

                contacts.add(Contact(name, number, photo))
            }
        }
        return contacts
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                loadContacts()
            } else {
                Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }


}