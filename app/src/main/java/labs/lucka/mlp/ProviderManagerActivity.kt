package labs.lucka.mlp

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_customized_provider.*

class ProviderManagerActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: ProviderManagerRecyclerViewAdapter

    private val recyclerViewListener: ProviderManagerRecyclerViewAdapter.Listener =
        object : ProviderManagerRecyclerViewAdapter.Listener {

            override fun onRemoved(provider: String, position: Int) {
                Snackbar
                    .make(
                        customizedProviderRecyclerView,
                        R.string.msg_provider_removed,
                        Snackbar.LENGTH_LONG
                    )
                    .setAction(R.string.undo) {
                        recyclerViewAdapter.add(provider, position)
                    }
                    .show()
            }

            override fun onEdit(provider: String, onSave: (newProvider: String) -> Unit) {
                DialogKit.showEditProviderDialog(
                    this@ProviderManagerActivity, provider
                ) { onSave(it) }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customized_provider)

        recyclerViewAdapter = ProviderManagerRecyclerViewAdapter(this, recyclerViewListener)

        customizedProviderRecyclerView
            .addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        customizedProviderRecyclerView.layoutManager = LinearLayoutManager(this)
        customizedProviderRecyclerView.adapter = recyclerViewAdapter
        recyclerViewAdapter
            .attachItemTouchHelperTo(customizedProviderRecyclerView)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_customized_provider, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            android.R.id.home -> {

                onBackPressed()
                return true
            }

            R.id.menu_customized_provider_add -> {
                DialogKit.showAddProviderDialog(this) { recyclerViewAdapter.add(it) }
            }

            R.id.menu_customized_provider_save -> {
                recyclerViewAdapter.saveData()
                onBackPressed()
            }

        }
        return super.onOptionsItemSelected(item)
    }
}