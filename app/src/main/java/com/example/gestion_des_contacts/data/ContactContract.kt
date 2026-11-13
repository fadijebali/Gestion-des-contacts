package com.example.gestion_des_contacts.data

import android.provider.BaseColumns

object ContactContract {
    object ContactEntry : BaseColumns {
        const val TABLE_NAME = "contacts"
        const val _ID = "_id"
        const val COLUMN_NOM = "nom"
        const val COLUMN_PRENOM = "prenom"
        const val COLUMN_TELEPHONE = "telephone"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHOTO_URI = "photo_uri"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
        const val COLUMN_FAVORI = "favori"
    }
}
