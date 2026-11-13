package com.example.gestion_des_contacts.data

import android.content.ContentValues
import android.database.Cursor
import com.example.gestion_des_contacts.data.ContactContract.ContactEntry

class ContactDao(private val dbHelper: ContactDbHelper) {

    fun insert(contact: Contact): Long {
        val db = dbHelper.writableDatabase
        val values = contactToContentValues(contact)
        return db.insert(ContactEntry.TABLE_NAME, null, values)
    }

    fun update(contact: Contact): Int {
        val db = dbHelper.writableDatabase
        val values = contactToContentValues(contact)
        return db.update(
            ContactEntry.TABLE_NAME,
            values,
            "${ContactEntry._ID} = ?",
            arrayOf(contact.id.toString())
        )
    }

    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        return db.delete(
            ContactEntry.TABLE_NAME,
            "${ContactEntry._ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getAll(orderBy: String = "NOM_ASC"): List<Contact> {
        val db = dbHelper.readableDatabase
        val orderClause = getOrderClause(orderBy)
        val cursor = db.query(
            ContactEntry.TABLE_NAME,
            null, null, null, null, null,
            orderClause
        )
        return cursor.use { cursorToContactList(it) }
    }

    fun getById(id: Long): Contact? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            ContactEntry.TABLE_NAME,
            null,
            "${ContactEntry._ID} = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use { 
            if (it.moveToFirst()) cursorToContact(it) else null 
        }
    }

    fun search(text: String, orderBy: String = "NOM_ASC"): List<Contact> {
        val db = dbHelper.readableDatabase
        val selection = "${ContactEntry.COLUMN_NOM} LIKE ? OR ${ContactEntry.COLUMN_PRENOM} LIKE ? OR ${ContactEntry.COLUMN_TELEPHONE} LIKE ?"
        val selectionArgs = arrayOf("%$text%", "%$text%", "%$text%")
        val orderClause = getOrderClause(orderBy)
        val cursor = db.query(
            ContactEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null, null,
            orderClause
        )
        return cursor.use { cursorToContactList(it) }
    }

    private fun getOrderClause(orderBy: String): String {
        return when (orderBy) {
            "FAVORI_FIRST" -> "${ContactEntry.COLUMN_FAVORI} DESC, ${ContactEntry.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactEntry.COLUMN_PRENOM} COLLATE NOCASE ASC"
            "PRENOM_ASC" -> "${ContactEntry.COLUMN_PRENOM} COLLATE NOCASE ASC, ${ContactEntry.COLUMN_NOM} COLLATE NOCASE ASC"
            else -> "${ContactEntry.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactEntry.COLUMN_PRENOM} COLLATE NOCASE ASC"
        }
    }

    private fun contactToContentValues(contact: Contact): ContentValues {
        return ContentValues().apply {
            put(ContactEntry.COLUMN_NOM, contact.nom)
            put(ContactEntry.COLUMN_PRENOM, contact.prenom)
            put(ContactEntry.COLUMN_TELEPHONE, contact.telephone)
            put(ContactEntry.COLUMN_EMAIL, contact.email)
            put(ContactEntry.COLUMN_PHOTO_URI, contact.photoUri)
            put(ContactEntry.COLUMN_LATITUDE, contact.latitude)
            put(ContactEntry.COLUMN_LONGITUDE, contact.longitude)
            put(ContactEntry.COLUMN_FAVORI, if (contact.isFavori) 1 else 0)
        }
    }

    private fun cursorToContact(cursor: Cursor): Contact {
        return Contact(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactEntry._ID)),
            nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_NOM)),
            prenom = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_PRENOM)),
            telephone = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_TELEPHONE)),
            email = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_EMAIL)),
            photoUri = cursor.getString(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_PHOTO_URI)),
            latitude = if (cursor.isNull(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_LATITUDE))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_LATITUDE)),
            longitude = if (cursor.isNull(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_LONGITUDE))) null else cursor.getDouble(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_LONGITUDE)),
            isFavori = cursor.getInt(cursor.getColumnIndexOrThrow(ContactEntry.COLUMN_FAVORI)) == 1
        )
    }

    private fun cursorToContactList(cursor: Cursor): List<Contact> {
        val contacts = mutableListOf<Contact>()
        while (cursor.moveToNext()) {
            contacts.add(cursorToContact(cursor))
        }
        return contacts
    }
}
