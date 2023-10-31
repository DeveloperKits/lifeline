package com.developerkits.lifeline.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.developerkits.lifeline.Model.Contact
import com.developerkits.lifeline.R

class ContactsAdapter (private val context: Fragment, private val contacts: List<Contact>) :
    RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val nameTextView: TextView = view.findViewById(R.id.name)
        val numberTextView: TextView = view.findViewById(R.id.number)
        val photoView: ImageView = view.findViewById(R.id.photoView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contacts, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactsAdapter.ViewHolder, position: Int) {
        val contact = contacts[position]
        holder.nameTextView.text = contact.name
        holder.numberTextView.text = contact.number

        if (contact.photo != " "){
            Glide
                .with(context)
                .load(contact.photo)
                .centerInside()
                .into(holder.photoView)
        }
    }

    override fun getItemCount() = contacts.size
}