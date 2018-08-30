package labs.lucka.mlp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_add_mock_target.view.*
import java.io.*

class MainActivity : AppCompatActivity() {

    private var mockTargetList: ArrayList<MockTarget> = ArrayList(0)
    private lateinit var mainRecyclerViewAdapter: MainRecyclerViewAdapter
    private val mainRecyclerViewListener: MainRecyclerViewAdapter.MainRecyclerViewListener =
        object : MainRecyclerViewAdapter.MainRecyclerViewListener {

            override fun onRemovedAt(index: Int) {
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

        fabService.setImageDrawable(
            if (isMLPServiceOnline()) getDrawable(R.drawable.ic_cancel)
            else getDrawable(R.drawable.ic_start)
        )

        fabService.setOnClickListener { _ ->
            if (isMLPServiceOnline()) {
                val mlpService = Intent(this, MockLocationProviderService::class.java)
                stopService(mlpService)
                PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(getString(R.string.pref_is_service_online_key), false)
                    .apply()
                fabService.setImageDrawable(getDrawable(R.drawable.ic_start))
            } else {
                saveData()
                val mlpService = Intent(this, MockLocationProviderService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(mlpService)
                } else {
                    startService(mlpService)
                }
                fabService.setImageDrawable(getDrawable(R.drawable.ic_cancel))
            }
        }
    }

    /*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }*/

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

    private fun isMLPServiceOnline(): Boolean {
        return PreferenceManager
            .getDefaultSharedPreferences(this)
            .getBoolean(getString(R.string.pref_is_service_online_key), false)
    }
}
