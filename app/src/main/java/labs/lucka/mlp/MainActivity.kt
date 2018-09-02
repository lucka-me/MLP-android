package labs.lucka.mlp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.widget.NestedScrollView
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

/**
 * MainActivity for MLP
 *
 * ## Changelog
 * ### 0.2
 * - Replace saveData and loadData with [DataKit]
 * - Migrate isServiceOnline] and isMockLocationEnabled to [MockLocationProviderService]
 * - Migrate showDeveloperOptionsDialog and showAddMockTargetDialog to [DialogKit]
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
 * - [updateFabService]
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
                try {
                    DataKit.saveData(this@MainActivity, mockTargetList)
                } catch (error: Exception) {
                    DialogKit.showSimpleAlert(this@MainActivity, error.message)
                }
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        //loadData()
        try {
            mockTargetList = DataKit.loadData(this)
        } catch (error: Exception) {
            DialogKit.showSimpleAlert(this@MainActivity, error.message)
        }

        mainRecyclerViewAdapter =
            MainRecyclerViewAdapter(this, mockTargetList, mainRecyclerViewListener)
        mainRecyclerView.layoutManager = LinearLayoutManager(this)
        mainRecyclerView.adapter = mainRecyclerViewAdapter

        fabAddMockTarget.setOnClickListener { _ ->
            DialogKit.showAddMockTargetDialog(
                this,
                mockTargetList,
                {
                    Snackbar
                        .make(mainRecyclerView, R.string.err_coordinate_wrong, Snackbar.LENGTH_LONG)
                        .show()
                },
                {
                    mainRecyclerViewAdapter.notifyAddMockTarget()
                    Snackbar.make(
                        nestedScrollView,
                        R.string.add_mock_target_success,
                        Snackbar.LENGTH_LONG
                    ).show()
                })
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
                if (MockLocationProviderService.isMockLocationEnabled(this)) {
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
     * Update the icon of [fabService]
     *
     * @author lucka-me
     * @since 0.1.1
     */
    private fun updateFabService() {
        fabService.setImageDrawable(getDrawable(
            if (MockLocationProviderService.isServiceOnline(this)) R.drawable.ic_cancel
            else R.drawable.ic_start
        ))
    }


}
