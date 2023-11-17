package br.com.lucolimac.contactlist.view

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import br.com.lucolimac.contactlist.R
import br.com.lucolimac.contactlist.adapter.ContactAdapter
import br.com.lucolimac.contactlist.databinding.ActivityMainBinding
import br.com.lucolimac.contactlist.model.Constants.EXTRA_CONTACT
import br.com.lucolimac.contactlist.model.Constants.EXTRA_VIEW_CONTACT
import br.com.lucolimac.contactlist.model.Contact

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val contactList: MutableList<Contact> = mutableListOf()
    private val contactAdapter: ContactAdapter by lazy {
        ContactAdapter(this, contactList)
    }

    private lateinit var resultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbarIn.toolbar)
        supportActionBar?.subtitle = getString(R.string.contact_list)

        resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == RESULT_OK) {
                    val contact = result.data?.getParcelableExtra<Contact>(EXTRA_CONTACT)
                    contact?.also { newOrEditedContact ->
                        if (contactList.any { it.id == newOrEditedContact.id }) {
                            val position =
                                contactList.indexOfFirst { it.id == newOrEditedContact.id }
                            contactList[position] = newOrEditedContact
                        } else {
                            contactList.add(newOrEditedContact)
                        }
                        contactAdapter.notifyDataSetChanged()
                    }
                }
            }

        fillContacts()

        binding.contactsLv.adapter = contactAdapter
        registerForContextMenu(binding.contactsLv)

        binding.contactsLv.setOnItemClickListener { _, _, position, _ ->
            startActivity(Intent(this, ContactActivity::class.java).apply {
                putExtra(EXTRA_CONTACT, contactList[position])
                putExtra(EXTRA_VIEW_CONTACT, true)
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addContactMi -> {
                resultLauncher.launch(Intent(this, ContactActivity::class.java))
                true
            }

            else -> {
                false
            }
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menuInflater.inflate(R.menu.context_menu_main, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = (item.menuInfo as AdapterView.AdapterContextMenuInfo).position
        return when (item.itemId) {
            R.id.removeContactMi -> {
                contactList.removeAt(position)
                contactAdapter.notifyDataSetChanged()
                Toast.makeText(this, getString(R.string.contact_removed), Toast.LENGTH_SHORT).show()
                true
            }

            R.id.editContactMi -> {
                resultLauncher.launch(Intent(this, ContactActivity::class.java).apply {
                    putExtra(EXTRA_CONTACT, contactList[position])
                })
                true
            }

            else -> {
                false
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterForContextMenu(binding.contactsLv)
    }

    private fun fillContacts() {
        for (i in 1..10) {
            contactList.add(
                Contact(
                    i,
                    "Nome $i",
                    "Endereço $i",
                    "Telefone $i",
                    "Email $i",
                )
            )
        }

    }
}