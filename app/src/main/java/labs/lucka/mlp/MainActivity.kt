package labs.lucka.mlp

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_add_mock_target.view.*
import java.io.*

/**
 * MainActivity for MLP
 *
 * ## Private Attributes
 * - [mockTargetList]
 * - [mainRecyclerViewAdapter]
 * - [mainRecyclerViewListener]
 *
 * ## Methods
 * ### Overridden
 * - [onCreate]
 * ### Private
 * - [saveData]
 * - [loadData]
 * - [updateFabService]
 * - [isMLPServiceOnline]
 * - [isMockLocationEnabled]
 * - [showDeveloperOptionsDialog]
 * - [showAddMockDialog]
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

            override fun onRemovedAt(position: Int) {
                saveData()
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        loadData()

        mainRecyclerViewAdapter =
            MainRecyclerViewAdapter(this, mockTargetList, mainRecyclerViewListener)
        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = mainRecyclerViewAdapter

        fabAddMockTarget.setOnClickListener { _ ->
            showAddMockDialog()
        }

        nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY) {
                    fabService.hide()
                } else {
                    fabService.show()
                }
            }
        )

        updateFabService()

        fabService.setOnClickListener { _ ->
            if (isMLPServiceOnline()) {
                val mlpService = Intent(this, MockLocationProviderService::class.java)
                stopService(mlpService)
                updateFabService()
            } else {
                saveData()
                if (isMockLocationEnabled()) {
                    val mlpService =
                        Intent(this, MockLocationProviderService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(mlpService)
                    } else {
                        startService(mlpService)
                    }
                    updateFabService()
                } else {
                    showDeveloperOptionsDialog()
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
            R.id.menu_main_preference -> {
                startActivity(Intent(this, PreferenceMainActivity::class.java))
            }
        }

        return when (item.itemId) {
            R.id.menu_main_preference -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Save the [mockTargetList] to file.
     *
     * @author lucka-me
     * @since 0.1
     */
    private fun saveData() {
        val filename = getString(R.string.data_filename)
        val file = File(filesDir, filename)
        val fileOutputStream: FileOutputStream
        val objectOutputStream: ObjectOutputStream

        try {
            fileOutputStream = FileOutputStream(file)
            objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(mockTargetList)
            objectOutputStream.close()
            fileOutputStream.close()
        } catch (error: Exception) {
            DialogKit.showSimpleAlert(this, error.message)
        }
    }

    /**
     * Load the [mockTargetList] from file.
     *
     * @author lucka-me
     * @since 0.1
     */
    private fun loadData() {
        val filename = getString(R.string.data_filename)
        val file = File(filesDir, filename)
        val fileInputStream: FileInputStream
        val objectInputStream: ObjectInputStream

        if (!file.exists()) return

        try {
            fileInputStream = FileInputStream(file)
            objectInputStream = ObjectInputStream(fileInputStream)
            @Suppress("UNCHECKED_CAST")
            mockTargetList = objectInputStream.readObject() as ArrayList<MockTarget>
            objectInputStream.close()
            fileInputStream.close()
        } catch (error: Exception) {
            DialogKit.showSimpleAlert(this, error.message)
        }
    }

    /**
     * Update the icon of [fabService]
     *
     * @author lucka-me
     * @since 0.1.1
     */
    private fun updateFabService() {
        fabService.setImageDrawable(
            if (isMLPServiceOnline()) getDrawable(R.drawable.ic_cancel)
            else getDrawable(R.drawable.ic_start)
        )
    }

    /**
     * Used to detect if the [MockLocationProviderService] is running.
     *
     * ## Changelog
     * ### 0.1.1
     * - Use [ActivityManager.getRunningServices] instead of PreferenceManager
     *
     * @return True if is running, false if not.
     * @author lucka-me
     * @since 0.1
     */
    private fun isMLPServiceOnline(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (serviceInfo in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceInfo.service.className == MockLocationProviderService::class.java.name)
                return true
        }
        return false
    }

    /**
     * Used to detect if the Enabled mock location is on and available for MLP.
     *
     * @return True if the option is on and available for MLP, false if off or unavailable.
     *
     * @author lucka-me
     * @since 0.1.2
     */
    private fun isMockLocationEnabled(): Boolean {
        val testLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            testLocationManager.addTestProvider(
                getString(R.string.test_location_provider),
                false, false,false, false,
                true,true, true,
                Criteria.POWER_LOW, Criteria.ACCURACY_FINE
            )
        } catch (error: SecurityException) {
            showDeveloperOptionsDialog()
            return false
        }
        testLocationManager.removeTestProvider(getString(R.string.test_location_provider))
        return true
    }

    /**
     * Show a dialog to explain the Enable mock location option
     * and provide a button to open the Developer Options.
     *
     * @author lucka-me
     * @since 0.1.2
     */
    private fun showDeveloperOptionsDialog() {
        DialogKit.showDialog(
            this,
            R.string.mock_location_option_title,
            R.string.mock_location_option_text,
            positiveButtonTextId = R.string.settings,
            positiveButtonListener = { _, _ ->
                try {
                    startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
                } catch (error: Exception) {
                    DialogKit.showSimpleAlert(
                        this,
                        R.string.err_developer_options_failed
                    )
                }
            },
            negativeButtonTextId = R.string.cancel,
            cancelable = false
        )
    }

    /**
     * Show a dialog to add a new mock target.
     *
     * @author lucka-me
     * @since 0.1
     */
    private fun showAddMockDialog() {
        val dialogLayout = View.inflate(this, R.layout.dialog_add_mock_target, null)
        AlertDialog.Builder(this)
            .setTitle(R.string.add_mock_target_title)
            .setView(dialogLayout)
            .setPositiveButton(R.string.confirm) { _, _ ->
                val longitude = dialogLayout.longitudeEdit.text.toString().toDoubleOrNull()
                val latitude = dialogLayout.latitudeEdit.text.toString().toDoubleOrNull()
                if (longitude == null || latitude == null ||
                    longitude < -180 || longitude > 180||
                    latitude < -90 || latitude > 90
                ) {
                    Snackbar
                        .make(mainRecyclerView, R.string.err_coordinate_wrong, Snackbar.LENGTH_LONG)
                        .show()
                } else {
                    mockTargetList.add(MockTarget(longitude, latitude))
                    saveData()
                    mainRecyclerViewAdapter.notifyAddMockTarget()
                    Snackbar.make(
                        nestedScrollView,
                        R.string.add_mock_target_success,
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .setCancelable(false)
            .show()
    }
}
