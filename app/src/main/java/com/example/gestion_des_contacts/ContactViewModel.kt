package com.example.gestion_des_contacts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.gestion_des_contacts.data.Contact
import com.example.gestion_des_contacts.data.ContactDao
import com.example.gestion_des_contacts.data.ContactDbHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(application: Application) : AndroidViewModel(application) {
    private val contactDao: ContactDao
    private val _allContacts = MutableLiveData<List<Contact>>()
    val allContacts: LiveData<List<Contact>> = _allContacts

    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?> = _selectedContact

    private var currentSortOrder: String = "NOM_ASC"
    private var currentQuery: String = ""

    init {
        val dbHelper = ContactDbHelper.getInstance(application)
        contactDao = ContactDao(dbHelper)
    }

    fun loadContacts(orderBy: String = currentSortOrder) {
        currentSortOrder = orderBy
        viewModelScope.launch {
            val contacts = withContext(Dispatchers.IO) {
                if (currentQuery.isEmpty()) {
                    contactDao.getAll(currentSortOrder)
                } else {
                    contactDao.search(currentQuery, currentSortOrder)
                }
            }
            _allContacts.value = contacts
        }
    }

    fun searchContacts(query: String, orderBy: String = currentSortOrder) {
        currentQuery = query
        currentSortOrder = orderBy
        loadContacts()
    }

    fun getContactById(id: Long) {
        viewModelScope.launch {
            val contact = withContext(Dispatchers.IO) {
                contactDao.getById(id)
            }
            _selectedContact.value = contact
        }
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (contact.id == 0L) {
                    contactDao.insert(contact)
                } else {
                    contactDao.update(contact)
                }
            }
            loadContacts()
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                contactDao.delete(id)
            }
            loadContacts()
        }
    }

    fun toggleFavori(contact: Contact) {
        val updatedContact = contact.copy(isFavori = !contact.isFavori)
        saveContact(updatedContact)
    }
}
