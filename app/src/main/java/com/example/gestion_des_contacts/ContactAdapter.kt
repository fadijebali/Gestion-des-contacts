package com.example.gestion_des_contacts

import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gestion_des_contacts.data.Contact
import com.example.gestion_des_contacts.databinding.ItemContactBinding
import java.util.Locale

class ContactAdapter(
    private val onContactClick: (Contact) -> Unit,
    private val onCallClick: (String) -> Unit,
    private val onFavoriClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    private var fontSize: Float = 16f
    private var preferredColor: Int = android.graphics.Color.BLACK
    private var isCallEnabled: Boolean = false

    fun updatePreferences(fontSize: Float, preferredColor: Int, isCallEnabled: Boolean) {
        this.fontSize = fontSize
        this.preferredColor = preferredColor
        this.isCallEnabled = isCallEnabled
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(private val binding: ItemContactBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contact: Contact) {
            binding.contactName.text = String.format("%s %s", contact.nom, contact.prenom)
            binding.contactPhone.text = contact.telephone

            // Apply preferences
            binding.contactName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
            binding.contactPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
            binding.contactName.setTextColor(preferredColor)
            
            binding.btnCall.visibility = if (isCallEnabled) View.VISIBLE else View.GONE

            // Favori icon color
            val starColor = if (contact.isFavori) {
                // Gold/Yellow color for favorite
                android.graphics.Color.parseColor("#FFD700")
            } else {
                // Light grey for non-favorite
                android.graphics.Color.parseColor("#BDBDBD")
            }
            binding.btnFavori.setColorFilter(starColor)

            if (!contact.photoUri.isNullOrEmpty()) {
                binding.contactImage.visibility = View.VISIBLE
                binding.contactInitials.visibility = View.GONE
                binding.contactImage.setImageURI(Uri.parse(contact.photoUri))
            } else {
                binding.contactImage.visibility = View.GONE
                binding.contactInitials.visibility = View.VISIBLE
                
                val initialNom = if (contact.nom.isNotEmpty()) contact.nom[0].toString() else ""
                val initialPrenom = if (contact.prenom.isNotEmpty()) contact.prenom[0].toString() else ""
                binding.contactInitials.text = (initialNom + initialPrenom).uppercase(Locale.getDefault())
            }

            binding.root.setOnClickListener { onContactClick(contact) }
            binding.btnCall.setOnClickListener { onCallClick(contact.telephone) }
            binding.btnFavori.setOnClickListener { onFavoriClick(contact) }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean = oldItem == newItem
    }
}
