package com.example.gestion_des_contacts.data

data class Contact(
    val id: Long = 0,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String? = null,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val isFavori: Boolean = false
)
