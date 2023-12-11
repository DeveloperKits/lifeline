package com.developerkits.lifeline.Adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.developerkits.lifeline.Model.Contact
import com.developerkits.lifeline.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ContactsAdapter(
    private val context: Fragment,
    private val contacts: List<Contact>,
    private val onClick: () -> Unit
) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(view: View, val onClick: () -> Unit) : RecyclerView.ViewHolder(view){
        val nameTextView: TextView = view.findViewById(R.id.name)
        val numberTextView: TextView = view.findViewById(R.id.number)
        val photoView: ImageView = view.findViewById(R.id.photoView)
        val button: Button = view.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacts, parent, false)
        return ViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.numberTextView.text = contact.number

        /*if (contact.photo != " "){
            Glide
                .with(context)
                .load(contact.photo)
                .centerInside()
                .into(holder.photoView)
        }*/

        holder.button.setOnClickListener {
            updateData(contact)
        }
    }

    private fun updateData(contact: Contact) {
        val db = Firebase.firestore

        val contacts = hashMapOf(
            "name" to contact.name,
            "number" to contact.number,
        )

        val auth = Firebase.auth

        db.collection("users")
            .document(auth.currentUser!!.uid).collection("contacts")
            .get()
            .addOnSuccessListener {documents ->
                if (documents.isEmpty || documents.size() < 5) {

                    db.collection("users")
                        .document(auth.currentUser!!.uid).collection("contacts")
                        .document(contact.number)
                        .set(contacts)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context.requireContext(),
                                "Contact added successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            onClick()
                        }
                        .addOnFailureListener {

                        }
                }else{
                    Toast.makeText(
                        context.requireContext(),
                        "Already have 5 contacts",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun getItemCount() = contacts.size
}