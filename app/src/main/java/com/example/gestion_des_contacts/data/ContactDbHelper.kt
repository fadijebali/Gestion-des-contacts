package com.example.gestion_des_contacts.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.gestion_des_contacts.data.ContactContract.ContactEntry

class ContactDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_CONTACTS_TABLE = """
            CREATE TABLE ${ContactEntry.TABLE_NAME} (
                ${ContactEntry._ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${ContactEntry.COLUMN_NOM} TEXT NOT NULL,
                ${ContactEntry.COLUMN_PRENOM} TEXT NOT NULL,
                ${ContactEntry.COLUMN_TELEPHONE} TEXT NOT NULL,
                ${ContactEntry.COLUMN_EMAIL} TEXT,
                ${ContactEntry.COLUMN_PHOTO_URI} TEXT,
                ${ContactEntry.COLUMN_LATITUDE} REAL,
                ${ContactEntry.COLUMN_LONGITUDE} REAL,
                ${ContactEntry.COLUMN_FAVORI} INTEGER DEFAULT 0
            );
        """.trimIndent()

        db.execSQL(SQL_CREATE_CONTACTS_TABLE)
        insertDemoData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE ${ContactEntry.TABLE_NAME} ADD COLUMN ${ContactEntry.COLUMN_FAVORI} INTEGER DEFAULT 0")
        }
    }

    private fun insertDemoData(db: SQLiteDatabase) {
        val contacts = listOf(
            ContentValues().apply {
                put(ContactEntry.COLUMN_NOM, "Ben Foulen")
                put(ContactEntry.COLUMN_PRENOM, "Foulen")
                put(ContactEntry.COLUMN_TELEPHONE, "55123456")
                put(ContactEntry.COLUMN_EMAIL, "foulen@example.com")
                put(ContactEntry.COLUMN_LATITUDE, 36.8065)
                put(ContactEntry.COLUMN_LONGITUDE, 10.1815)
                put(ContactEntry.COLUMN_FAVORI, 1)
            },
            ContentValues().apply {
                put(ContactEntry.COLUMN_NOM, "Aloui")
                put(ContactEntry.COLUMN_PRENOM, "Ameni")
                put(ContactEntry.COLUMN_TELEPHONE, "98765432")
                put(ContactEntry.COLUMN_EMAIL, "ameni@example.com")
                put(ContactEntry.COLUMN_LATITUDE, 35.8256)
                put(ContactEntry.COLUMN_LONGITUDE, 10.6367)
                put(ContactEntry.COLUMN_FAVORI, 0)
            },
            ContentValues().apply {
                put(ContactEntry.COLUMN_NOM, "Trabelsi")
                put(ContactEntry.COLUMN_PRENOM, "Sami")
                put(ContactEntry.COLUMN_TELEPHONE, "22334455")
                put(ContactEntry.COLUMN_EMAIL, "sami@example.com")
                put(ContactEntry.COLUMN_FAVORI, 1)
            },
            ContentValues().apply {
                put(ContactEntry.COLUMN_NOM, "Gharbi")
                put(ContactEntry.COLUMN_PRENOM, "Mouna")
                put(ContactEntry.COLUMN_TELEPHONE, "21000111")
                put(ContactEntry.COLUMN_EMAIL, "mouna@example.com")
                put(ContactEntry.COLUMN_LATITUDE, 33.8881)
                put(ContactEntry.COLUMN_LONGITUDE, 10.0982)
                put(ContactEntry.COLUMN_FAVORI, 0)
            },
            ContentValues().apply {
                put(ContactEntry.COLUMN_NOM, "Kallel")
                put(ContactEntry.COLUMN_PRENOM, "Omar")
                put(ContactEntry.COLUMN_TELEPHONE, "50607080")
                put(ContactEntry.COLUMN_FAVORI, 0)
            }
        )

        for (contact in contacts) {
            db.insert(ContactEntry.TABLE_NAME, null, contact)
        }
    }

    companion object {
        const val DATABASE_NAME = "contacts.db"
        const val DATABASE_VERSION = 2

        @Volatile
        private var INSTANCE: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ContactDbHelper(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
