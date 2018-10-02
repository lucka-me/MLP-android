package labs.lucka.mlp

import android.app.Activity
import android.content.Intent
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

/**
 * MainActivity for MLP
 *
 * ## Changelog
 * ### 0.2
 * - Replace saveData and loadData with [DataKit]
 * - Migrate isServiceOnline] and isMockLocationEnabled to [MockLocationProviderService]
 * - Migrate showDeveloperOptionsDialog and showAddMockTargetDialog to [DialogKit]
 * ### 0.2.4
 * - Import / export JSON (Totally ready)
 * - Import / export GPX (UI only)
 *
 * ## Private Attributes
 * - [mockTargetList]
 * - [mainRecyclerViewAdapter]
 * - [mainRecyclerViewListener]
 *
 * ## Nested Classes
 * - [AppRequest]
 *
 * ## Methods
 * ### Overridden
 * - [onCreate]
 * - [onCreateOptionsMenu]
 * - [onOptionsItemSelected]
 * - [onActivityResult]
 * ### Private
 * - [updateFabService]
 *
 * @see <a href="https://www.techotopia.com/index.php/An_Android_Storage_Access_Framework_Example#Saving_to_a_Storage_File">An Android Storage Access Framework Example | Techotopia</a>
 *
 * @author lucka-me
 * @since 0.1
 *
 * @property [mockTargetList] ArrayList for mock targets
 * @property [mainRecyclerViewAdapter] Adapter for [mainRecyclerView]
 * @property [mainRecyclerViewListener] Listener for message from [mainRecyclerViewAdapter]
 */
class MainActivity : AppCompatActivity() {

    private var mockTargetList: ArrayList<MockTarget> = ArrayList(0)
    private lateinit var mainRecyclerViewAdapter: MainRecyclerViewAdapter
    private val mainRecyclerViewListener: MainRecyclerViewAdapter.MainRecyclerViewListener =
        object : MainRecyclerViewAdapter.MainRecyclerViewListener {

            override fun onRemovedAt(position: Int, removedTarget: MockTarget) {
                try {
                    DataKit.saveData(this@MainActivity, mockTargetList)
                } catch (error: Exception) {
                    DialogKit.showSimpleAlert(this@MainActivity, error.message)
                }
                Snackbar
                    .make(
                        nestedScrollView,
                        R.string.target_removed,
                        Snackbar.LENGTH_LONG
                    )
                    .setAction(R.string.undo) {
                        mockTargetList.add(position, removedTarget)
                        mainRecyclerViewAdapter.notifyItemInserted(position)
                        try {
                            DataKit.saveData(this@MainActivity, mockTargetList)
                        } catch (error: Exception) {
                            DialogKit.showSimpleAlert(this@MainActivity, error.message)
                        }
                    }
                    .show()

            }

            override fun onEditAt(position: Int) {
                DialogKit.showEditMockTargetDialog(
                    this@MainActivity, mockTargetList[position]
                ) {
                    try {
                        DataKit.saveData(this@MainActivity, mockTargetList)
                    } catch (error: Exception) {
                        DialogKit.showSimpleAlert(this@MainActivity, error.message)
                    }
                    mainRecyclerViewAdapter.notifyItemChanged(position)
                }
            }

        }

    /**
     * Request codes
     *
     * @author lucka-me
     * @since 0.2.4
     */
    enum class AppRequest(val code: Int) {
        IMPORT_GPX(3101),
        IMPORT_JSON(3102),
        EXPORT_GPX(3201),
        EXPORT_JSON(3202)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Load data
        try {
            mockTargetList = DataKit.loadData(this)
        } catch (error: Exception) {
            DialogKit.showSimpleAlert(this@MainActivity, error.message)
        }

        // Setup recycler view
        mainRecyclerViewAdapter =
            MainRecyclerViewAdapter(this, mockTargetList, mainRecyclerViewListener)
        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = mainRecyclerViewAdapter
        mainRecyclerView.isNestedScrollingEnabled = false
        mainRecyclerViewAdapter.attachItemTouchHelperTo(mainRecyclerView)

        // Setup fabs
        fabAddMockTarget.setOnClickListener { _ ->
            DialogKit.showAddMockTargetDialog(this) { newTarget ->
                mockTargetList.add(newTarget)
                try {
                    DataKit.saveData(this, mockTargetList)
                } catch (error: Exception) {
                    DialogKit.showSimpleAlert(this, error.message)
                }
                mainRecyclerViewAdapter.notifyAddMockTarget()
                Snackbar.make(
                    nestedScrollView,
                    R.string.add_mock_target_success,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

        nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) {
                    fabAddMockTarget.hide()
                } else {
                    fabAddMockTarget.show()
                }
            }
        )

        updateFabService()

        fabService.setOnClickListener { _ ->
            if (MockLocationProviderService.isServiceOnline(this)) {
                val mlpService = Intent(this, MockLocationProviderService::class.java)
                stopService(mlpService)
                updateFabService()
            } else {
                try {
                    DataKit.saveData(this@MainActivity, mockTargetList)
                } catch (error: Exception) {
                    DialogKit.showSimpleAlert(this@MainActivity, error.message)
                }
                if (mockTargetList.isEmpty()) {
                    DialogKit.showSimpleAlert(this, R.string.err_no_target)
                } else if (MockLocationProviderService.isMockLocationEnabled(this)) {
                    val mlpService =
                        Intent(this, MockLocationProviderService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(mlpService)
                    } else {
                        startService(mlpService)
                    }
                    updateFabService()
                } else {
                    DialogKit.showDeveloperOptionsDialog(this)
                }
            }
        }

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        when(item.itemId) {
            R.id.menu_main_import -> {
                DialogKit.showImportExportMenuDialog(this, R.string.import_title) { fileType ->
                    startActivityForResult(
                        Intent(Intent.ACTION_GET_CONTENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType(getString(fileType.mime)),
                        fileType.importRequestCode
                    )
                }
            }
            R.id.menu_main_export -> {
                DialogKit.showImportExportMenuDialog(this, R.string.export_title) { fileType ->
                    startActivityForResult(
                        Intent(Intent.ACTION_CREATE_DOCUMENT)
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .setType(getString(fileType.mime)),
                        fileType.exportRequestCode
                    )
                }
            }
            R.id.menu_main_clear -> {
                fun removeAll() {
                    val oldList: ArrayList<MockTarget> = ArrayList(0)
                    for (mockTarget in mockTargetList)
                        oldList.add(mockTarget)
                    mockTargetList.clear()
                    mainRecyclerViewAdapter.notifyItemRangeRemoved(0, oldList.size)
                    try {
                        DataKit.saveData(this@MainActivity, mockTargetList)
                    } catch (error: Exception) {
                        DialogKit.showSimpleAlert(this@MainActivity, error.message)
                    }
                    Snackbar
                        .make(
                            nestedScrollView,
                            R.string.targets_cleared,
                            Snackbar.LENGTH_LONG
                        )
                        .setAction(R.string.undo) {
                            for (mockTarget in oldList)
                                mockTargetList.add(mockTarget)
                            mainRecyclerViewAdapter
                                .notifyItemRangeInserted(0, mockTargetList.size)
                            try {
                                DataKit.saveData(this@MainActivity, mockTargetList)
                            } catch (error: Exception) {
                                DialogKit.showSimpleAlert(this@MainActivity, error.message)
                            }
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
                startActivity(Intent(this, PreferenceMainActivity::class.java))
            }
            R.id.menu_main_about -> {
                startActivity(Intent(this, PreferenceAboutActivity::class.java))
            }
        }

        return when (item.itemId) {
            R.id.menu_main_preference -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {

            AppRequest.IMPORT_GPX.code,
            AppRequest.IMPORT_JSON.code -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    try {
                        val newList = when (requestCode) {
                            AppRequest.IMPORT_GPX.code -> {
                                DataKit.importFromGPX(
                                    this, DataKit.readFile(this, data.data)
                                )
                            }
                            AppRequest.IMPORT_JSON.code -> {
                                DataKit.importFromJSON(DataKit.readFile(this, data.data))
                            }
                            else -> return
                        }
                        if (newList.isNotEmpty()) {
                            mockTargetList.addAll(newList)
                            mainRecyclerViewAdapter.notifyItemRangeInserted(
                                mockTargetList.size - newList.size, newList.size
                            )
                            DataKit.saveData(this, mockTargetList)
                            Snackbar.make(
                                nestedScrollView,
                                String.format(getString(R.string.import_success), newList.size),
                                Snackbar.LENGTH_LONG
                            ).show()
                        } else {
                            Snackbar.make(
                                nestedScrollView, R.string.import_empty, Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } catch (error: Exception) {
                        DialogKit.showSimpleAlert(
                            this,
                            String.format(getString(R.string.import_failed), error.message)
                        )
                    }
                }
            }

            AppRequest.EXPORT_GPX.code,
            AppRequest.EXPORT_JSON.code -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    var exportTargetList: ArrayList<MockTarget> = ArrayList(0)
                    if (defaultSharedPreferences.getBoolean(
                            getString(R.string.pref_ie_export_enabled_only_key), false
                        )) {
                        for (target in mockTargetList)
                            if (target.enabled) exportTargetList.add(target)
                    } else {
                        exportTargetList = mockTargetList
                    }
                    try {
                        DataKit.writeFile(
                            this,
                            when (requestCode) {
                                AppRequest.EXPORT_GPX.code ->
                                    DataKit.exportToGPX(exportTargetList)
                                AppRequest.EXPORT_JSON.code ->
                                    DataKit.exportToJSON(exportTargetList)
                                else -> return
                            },
                            data.data
                        )
                    } catch (error: Exception) {
                        DialogKit.showSimpleAlert(this, error.message)
                    }
                    Snackbar.make(
                        nestedScrollView,
                        String.format(getString(R.string.export_success), exportTargetList.size),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Update the icon of [fabService]
     *
     * @author lucka-me
     * @since 0.1.1
     */
    private fun updateFabService() {
        fabService.imageResource =
            if (MockLocationProviderService.isServiceOnline(this)) {
                R.drawable.ic_stop_service
            } else {
                R.drawable.ic_start_service
            }
        fabService.hide()
        fabService.show()
    }

}
