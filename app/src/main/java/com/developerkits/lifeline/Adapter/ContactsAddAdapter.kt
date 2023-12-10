package com.developerkits.lifeline.Adapter

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
import com.developerkits.lifeline.Model.ContactAdd
import com.developerkits.lifeline.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore

class ContactsAddAdapter(
    private val context: Fragment,
    private val contacts: List<ContactAdd>
):
    RecyclerView.Adapter<ContactsAddAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val nameTextView: TextView = view.findViewById(R.id.name)
        val numberTextView: TextView = view.findViewById(R.id.number)
        val photoView: ImageView = view.findViewById(R.id.photoView)
        val button: Button = view.findViewById(R.id.button)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactsAddAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacts, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsAddAdapter.ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.numberTextView.text = contact.number

        holder.button.text = "Remove"
        holder.button.setBackgroundColor(ContextCompat.getColor(context.requireContext(), R.color.app_main_color))

        holder.button.setOnClickListener {
            val db = Firebase.firestore
            val auth = Firebase.auth

            db.collection("users")
                .document(auth.currentUser!!.uid)
                .collection("contacts")
                .document(contact.number)
                .delete().addOnSuccessListener {
                    Toast.makeText(
                        context.requireContext(),
                        "Delete successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return contacts.size
    }


}