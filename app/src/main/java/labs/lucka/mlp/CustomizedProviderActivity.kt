package labs.lucka.mlp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_customized_provider.*
import org.jetbrains.anko.defaultSharedPreferences

/**
 * Activity for Customized Provider screen
 *
 * ## Private Attributes
 * - [providerList]
 * - [customizedProviderRecyclerViewAdapter]
 * - [customizedProviderRecyclerViewListener]
 *
 * ## Overridden Methods
 * - [onCreate]
 * - [onCreateOptionsMenu]
 * - [onOptionsItemSelected]
 *
 * @author lucka-me
 * @since 0.2.7
 *
 * @property [providerList] ArrayList for customized providers
 * @property [customizedProviderRecyclerViewAdapter] Adapter for [customizedProviderRecyclerView]
 * @property [customizedProviderRecyclerViewListener] Listener for message from [customizedProviderRecyclerViewAdapter]
 */
class CustomizedProviderActivity : AppCompatActivity() {

    private var providerList: ArrayList<String> = ArrayList(0)
    private lateinit var customizedProviderRecyclerViewAdapter:
        CustomizedProviderRecyclerViewAdapter
    private val customizedProviderRecyclerViewListener:
        CustomizedProviderRecyclerViewAdapter.CustomizedProviderRecyclerViewListener =
        object : CustomizedProviderRecyclerViewAdapter.CustomizedProviderRecyclerViewListener {

            override fun onRemovedAt(position: Int, removedProvider: String) {
                Snackbar
                    .make(
                        customizedProviderRecyclerView,
                        R.string.customized_provider_removed,
                        Snackbar.LENGTH_LONG
                    )
                    .setAction(R.string.undo) {
                        providerList.add(position, removedProvider)
                        customizedProviderRecyclerViewAdapter.notifyItemInserted(position)
                    }
                    .show()
            }

            override fun onEditAt(position: Int) {
                DialogKit.showEditProviderDialog(
                    this@CustomizedProviderActivity, providerList[position]
                ) { provider ->
                    providerList[position] = provider
                    customizedProviderRecyclerViewAdapter.notifyItemChanged(position)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customized_provider)

        val customizedProviderList = defaultSharedPreferences.getStringSet(
            getString(R.string.pref_provider_customized_list_key), setOf()
        )
        providerList =
            if (customizedProviderList != null) ArrayList(customizedProviderList) else ArrayList()
        customizedProviderRecyclerViewAdapter = CustomizedProviderRecyclerViewAdapter(
            this, providerList, customizedProviderRecyclerViewListener
        )
        customizedProviderRecyclerView
            .addItemDecoration(androidx.recyclerview.widget.DividerItemDecoration(this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL))
        customizedProviderRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        customizedProviderRecyclerView.adapter = customizedProviderRecyclerViewAdapter
        customizedProviderRecyclerViewAdapter
            .attachItemTouchHelperTo(customizedProviderRecyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_customized_provider, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            when (item.itemId) {

                android.R.id.home -> {

                    onBackPressed()
                    return true
                }

                R.id.menu_customized_provider_add -> {
                    DialogKit.showAddProviderDialog(this) { provider ->
                        providerList.add(provider)
                        customizedProviderRecyclerViewAdapter
                            .notifyItemInserted(providerList.size - 1)
                    }
                }

                R.id.menu_customized_provider_save -> {
                    defaultSharedPreferences
                        .edit()
                        .putStringSet(
                            getString(R.string.pref_provider_customized_list_key),
                            HashSet(providerList)
                        )
                        .apply()
                    onBackPressed()
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }
}