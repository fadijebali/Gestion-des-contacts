package com.example.gestion_des_contacts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gestion_des_contacts.databinding.FragmentListeContactsBinding

class ListeContactsFragment : Fragment() {

    private var _binding: FragmentListeContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var prefsManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? MainActivity)?.setToolbarTitle(getString(R.string.title_mes_contacts), false)

        setupMenu()

        prefsManager = PreferencesManager(requireContext())
        setupRecyclerView()

        viewModel.allContacts.observe(viewLifecycleOwner) { contacts ->
            adapter.submitList(contacts)
            binding.emptyView.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchContacts(s.toString(), prefsManager.getSortOrder())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabAdd.setOnClickListener { openCreateContact() }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_liste, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_add -> {
                        openCreateContact()
                        true
                    }
                    R.id.action_settings -> {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ParametresFragment())
                            .addToBackStack(null)
                            .commit()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun openCreateContact() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, DetailContactFragment.newInstance(0L))
            .addToBackStack(null)
            .commit()
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter(
            onContactClick = { contact ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, DetailContactFragment.newInstance(contact.id))
                    .addToBackStack(null)
                    .commit()
            },
            onCallClick = { phone ->
                if (phone.isNotEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(R.string.no_phone_app), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onFavoriClick = { contact ->
                viewModel.toggleFavori(contact)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val sortOrder = prefsManager.getSortOrder()
        adapter.updatePreferences(
            prefsManager.getFontSize(),
            prefsManager.getPreferredColor(),
            prefsManager.isCallFromListEnabled()
        )
        viewModel.loadContacts(sortOrder)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
