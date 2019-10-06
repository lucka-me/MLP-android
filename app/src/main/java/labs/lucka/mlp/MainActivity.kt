package labs.lucka.mlp

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.imageResource

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdapter: MockTargetRecyclerViewAdapter
    private val recyclerViewListener: MockTargetRecyclerViewAdapter.MainRecyclerViewListener =
        object : MockTargetRecyclerViewAdapter.MainRecyclerViewListener {

            override fun onRemove(
                mockTarget: MockTarget, onConfirmed: () -> Unit, onCanceled: () -> Unit
            ) {

                val remove = {
                    onConfirmed()
                    Snackbar
                        .make(
                            nested_scroll_view_main, R.string.target_removed, Snackbar.LENGTH_LONG
                        )
                        .setAction(R.string.undo) { recyclerViewAdapter.add(mockTarget) }
                        .show()
                }

                if (defaultSharedPreferences
                        .getBoolean(getString(R.string.pref_edit_confirm_remove_key), true)
                ) {
                    DialogKit.showDialog(
                        this@MainActivity,
                        R.string.remove_mock_target_confirm_title,
                        getString(
                            R.string.remove_mock_target_confirm_message,
                            mockTarget.title,
                            Location.convert(mockTarget.longitude, Location.FORMAT_SECONDS),
                            Location.convert(mockTarget.latitude, Location.FORMAT_SECONDS)
                        ),
                        positiveButtonListener = { _, _ -> remove() },
                        negativeButtonTextId = R.string.cancel,
                        negativeButtonListener = { _, _ -> onCanceled() },
                        cancelable = false
                    )
                } else {
                    remove()
                }

            }

            override fun onEdit(mockTarget: MockTarget, onSave: () -> Unit) {
                DialogKit.showEditMockTargetDialog(this@MainActivity, mockTarget, onSave)
            }

        }

    /**
     * Request codes
     *
     * @author lucka-me
     * @since 0.2.4
     */
    enum class Request(val code: Int) {
        IMPORT_GPX(3101),
        IMPORT_JSON(3102),
        EXPORT_GPX(3201),
        EXPORT_JSON(3202)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        recyclerViewAdapter = MockTargetRecyclerViewAdapter(this, recyclerViewListener)
        recycler_view_mock_target.layoutManager = LinearLayoutManager(this)
        recycler_view_mock_target.adapter = recyclerViewAdapter
        recycler_view_mock_target.isNestedScrollingEnabled = false
        recyclerViewAdapter.attachItemTouchHelperTo(recycler_view_mock_target)

        fab_add.setOnClickListener {
            DialogKit.showAddMockTargetDialog(this) { newTarget ->
                recyclerViewAdapter.add(newTarget)
                Snackbar
                    .make(
                        nested_scroll_view_main,
                        R.string.add_mock_target_success, Snackbar.LENGTH_LONG
                    )
                    .show()
            }
        }

        nested_scroll_view_main.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) {
                    fab_add.hide()
                } else {
                    fab_add.show()
                }
            }
        )

        updateFabService()

        fab_service.setOnClickListener {

            if (MLPService.isServiceOnline(this)) {
                val mlpService = Intent(this, MLPService::class.java)
                stopService(mlpService)
                updateFabService()
            } else {
                recyclerViewAdapter.saveData()
                when {

                    recyclerViewAdapter.isEmpty() ->
                        DialogKit.showSimpleAlert(this, R.string.err_no_target)

                    MLPService.isMockLocationEnabled(this) -> {
                        val mlpService = Intent(this, MLPService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(mlpService)
                        } else {
                            startService(mlpService)
                        }
                        updateFabService()
                    }

                    else -> DialogKit.showDeveloperOptionsDialog(this)

                }
            }

        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {

            R.id.menu_main_import -> {
                DialogKit
                    .showFileTypeSelectMenuDialog(this, R.string.import_title) { fileType ->
                    startActivityForResult(
                        Intent(Intent.ACTION_GET_CONTENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType(getString(fileType.mime)),
                        fileType.importRequestCode
                    )
                }
            }

            R.id.menu_main_export -> {
                DialogKit
                    .showFileTypeSelectMenuDialog(this, R.string.export_title) { fileType ->
                    startActivityForResult(
                        Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType(getString(fileType.mime)),
                        fileType.exportRequestCode
                    )
                }
            }

            R.id.menu_main_clear -> {
                val removeAll = {
                    val oldList = recyclerViewAdapter.clear()
                    Snackbar
                        .make(
                            nested_scroll_view_main, R.string.targets_cleared, Snackbar.LENGTH_LONG
                        )
                        .setAction(R.string.undo) {
                            oldList.forEach { recyclerViewAdapter.add(it) }
                        }
                        .show()
                }
                if (defaultSharedPreferences.getBoolean(
                        getString(R.string.pref_edit_confirm_remove_key), true
                    )
                ) {
                    DialogKit.showDialog(
                        this,
                        R.string.clear_mock_target_confirm_title,
                        R.string.clear_mock_target_confirm_message,
                        positiveButtonListener = { _, _ ->
                            removeAll()
                        },
                        negativeButtonTextId = R.string.cancel,
                        cancelable = false
                    )
                } else {
                    removeAll()
                }

            }
            R.id.menu_main_preference -> {
                startActivity(Intent(this, PreferenceActivity::class.java))
            }
        }

        return when (item.itemId) {
            R.id.menu_main_preference -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if ((requestCode != Activity.RESULT_OK) || (data == null)) {
            return super.onActivityResult(requestCode, resultCode, data)
        }

        when (requestCode) {

            Request.IMPORT_GPX.code,
            Request.IMPORT_JSON.code -> {

                val onFinished = { size: Int ->
                    Snackbar
                        .make(
                            nested_scroll_view_main,
                            if (size > 0)
                                getString(R.string.import_success, size)
                            else
                                getString(R.string.import_empty),
                            Snackbar.LENGTH_LONG
                        )
                        .show()
                }

                val onFailed = { error: Exception ->
                    DialogKit.showSimpleAlert(this, error.message)
                }

                recyclerViewAdapter.import(requestCode, data.data, onFinished, onFailed)

            }

            Request.EXPORT_GPX.code,
            Request.EXPORT_JSON.code -> {
                val onFinished = { size: Int ->
                    Snackbar
                        .make(
                            nested_scroll_view_main,
                            getString(R.string.export_success, size), Snackbar.LENGTH_LONG
                        )
                        .show()
                }

                val onFailed = { error: Exception ->
                    DialogKit.showSimpleAlert(this, error.message)
                }

                recyclerViewAdapter.export(requestCode, data.data, onFinished, onFailed)

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun updateFabService() {
        fab_service.imageResource =
            if (MLPService.isServiceOnline(this)) {
                R.drawable.ic_stop_service
            } else {
                R.drawable.ic_start_service
            }
        fab_service.hide()
        fab_service.show()
    }

}
