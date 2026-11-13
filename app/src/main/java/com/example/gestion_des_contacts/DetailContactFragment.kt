package com.example.gestion_des_contacts

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.example.gestion_des_contacts.data.Contact
import com.example.gestion_des_contacts.databinding.FragmentDetailContactBinding
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class DetailContactFragment : Fragment() {

    private var _binding: FragmentDetailContactBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()
    private var contactId: Long = 0
    private var currentPhotoUri: String? = null
    private lateinit var prefsManager: PreferencesManager

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val internalUri = copyImageToInternalStorage(it)
            currentPhotoUri = internalUri.toString()
            binding.detailImage.setImageURI(internalUri)
            updatePhotoVisibility()
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            bitmap?.let {
                val internalUri = saveBitmapToInternalStorage(it)
                currentPhotoUri = internalUri.toString()
                binding.detailImage.setImageBitmap(it)
                updatePhotoVisibility()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            contactId = it.getLong(ARG_CONTACT_ID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefsManager = PreferencesManager(requireContext())
        applyPreferences()

        setupMenu()

        if (contactId != 0L) {
            (activity as? MainActivity)?.setToolbarTitle(getString(R.string.title_details_contact), true)
            viewModel.getContactById(contactId)
            viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
                contact?.let { populateFields(it) }
            }
        } else {
            (activity as? MainActivity)?.setToolbarTitle(getString(R.string.title_nouveau_contact), true)
            binding.btnDial.visibility = View.GONE
            binding.btnEmail.visibility = View.GONE
            binding.btnMap.visibility = View.GONE
            updatePhotoVisibility()
        }

        setupListeners()
        setupTextWatchers()
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (contactId != 0L) {
                    menuInflater.inflate(R.menu.menu_detail, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmation()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun applyPreferences() {
        val fontSize = prefsManager.getFontSize()
        val color = prefsManager.getPreferredColor()

        binding.etNom.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        binding.etPrenom.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        binding.etPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        binding.etNom.setTextColor(color)
        binding.etPrenom.setTextColor(color)
    }

    private fun populateFields(contact: Contact) {
        binding.etNom.setText(contact.nom)
        binding.etPrenom.setText(contact.prenom)
        binding.etPhone.setText(contact.telephone)
        binding.etEmail.setText(contact.email)
        binding.etLat.setText(contact.latitude?.toString() ?: "")
        binding.etLon.setText(contact.longitude?.toString() ?: "")
        currentPhotoUri = contact.photoUri
        if (!contact.photoUri.isNullOrEmpty()) {
            binding.detailImage.setImageURI(contact.photoUri.toUri())
        }
        updatePhotoVisibility()
    }

    private fun updatePhotoVisibility() {
        if (currentPhotoUri == null) {
            binding.detailImage.visibility = View.GONE
            binding.detailInitials.visibility = View.VISIBLE
            updateInitialsText()
        } else {
            binding.detailImage.visibility = View.VISIBLE
            binding.detailInitials.visibility = View.GONE
        }
    }

    private fun updateInitialsText() {
        val nom = binding.etNom.text.toString()
        val prenom = binding.etPrenom.text.toString()
        val initialNom = if (nom.isNotEmpty()) nom[0].toString() else ""
        val initialPrenom = if (prenom.isNotEmpty()) prenom[0].toString() else ""
        binding.detailInitials.text = String.format("%s%s", initialNom, initialPrenom).uppercase(Locale.getDefault())
    }

    private fun setupListeners() {
        binding.fabEditPhoto.setOnClickListener {
            val options = arrayOf("Galerie", "Caméra")
            AlertDialog.Builder(requireContext())
                .setTitle("Changer la photo")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> galleryLauncher.launch("image/*")
                        1 -> cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                    }
                }
                .show()
        }

        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
        
        binding.btnCamera.setOnClickListener {
            cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        }

        binding.btnSave.setOnClickListener { saveContact() }

        binding.btnDial.setOnClickListener {
            val phone = binding.etPhone.text.toString()
            if (phone.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_DIAL, "tel:$phone".toUri()))
            }
        }

        binding.btnEmail.setOnClickListener {
            val email = binding.etEmail.text.toString()
            if (email.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = "mailto:$email".toUri()
                }
                startActivity(intent)
            }
        }

        binding.btnMap.setOnClickListener {
            val lat = binding.etLat.text.toString().toDoubleOrNull()
            val lon = binding.etLon.text.toString().toDoubleOrNull()
            if (lat != null && lon != null) {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CarteContactFragment.newInstance(lat, lon, binding.etNom.text.toString()))
                    .addToBackStack(null)
                    .commit()
            } else {
                Toast.makeText(requireContext(), getString(R.string.gps_invalid_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (currentPhotoUri == null) {
                    updateInitialsText()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        }
        binding.etNom.addTextChangedListener(watcher)
        binding.etPrenom.addTextChangedListener(watcher)
    }

    private fun saveContact() {
        val nom = binding.etNom.text.toString()
        val prenom = binding.etPrenom.text.toString()
        val phone = binding.etPhone.text.toString()
        val email = binding.etEmail.text.toString()
        val lat = binding.etLat.text.toString().toDoubleOrNull()
        val lon = binding.etLon.text.toString().toDoubleOrNull()

        if (nom.isBlank() || prenom.isBlank() || phone.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.mandatory_fields_error), Toast.LENGTH_SHORT).show()
            return
        }

        val contact = Contact(
            id = contactId,
            nom = nom,
            prenom = prenom,
            telephone = phone,
            email = email.ifBlank { null },
            photoUri = currentPhotoUri,
            latitude = lat,
            longitude = lon,
            isFavori = viewModel.selectedContact.value?.isFavori ?: false
        )

        viewModel.saveContact(contact)
        parentFragmentManager.popBackStack()
    }

    private fun showDeleteConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                viewModel.deleteContact(contactId)
                parentFragmentManager.popBackStack()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val file = File(requireContext().filesDir, "contact_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return Uri.fromFile(file)
    }

    private fun saveBitmapToInternalStorage(bitmap: Bitmap): Uri {
        val file = File(requireContext().filesDir, "contact_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return Uri.fromFile(file)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONTACT_ID = "contact_id"

        fun newInstance(id: Long) = DetailContactFragment().apply {
            arguments = Bundle().apply {
                putLong(ARG_CONTACT_ID, id)
            }
        }
    }
}
