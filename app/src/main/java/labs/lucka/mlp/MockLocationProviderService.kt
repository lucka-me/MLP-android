package labs.lucka.mlp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Criteria
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.*

class MockLocationProviderService : Service() {

    private var mockTargetList: ArrayList<MockTarget> = ArrayList(0)
    private lateinit var locationManager: LocationManager
    private var timer: Timer = Timer(true)
    private var currentTargetIndex: Int = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Setup foreground service
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            notificationChannel.description =
                getString(R.string.service_notification_channel_description)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        startForeground(
            FOREGROUND_ID,
            NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.service_notification_text))
                .setContentIntent(PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    0
                ))
                .setSmallIcon(R.drawable.ic_start)
                .build()
        )

        // Load data
        val filename = getString(R.string.data_filename)
        val file = File(filesDir, filename)
        val fileInputStream: FileInputStream
        val objectInputStream: ObjectInputStream
        if (!file.exists()) {
            stopSelf()
        }
        try {
            fileInputStream = FileInputStream(file)
            objectInputStream = ObjectInputStream(fileInputStream)
            @Suppress("UNCHECKED_CAST")
            mockTargetList = objectInputStream.readObject() as ArrayList<MockTarget>
            objectInputStream.close()
            fileInputStream.close()
        } catch (error: Exception) {
            stopSelf()
        }

        // Setup location manager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false,false,false,true,true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE)
        locationManager.addTestProvider(LocationManager.NETWORK_PROVIDER, false, false,false,false,true,true, true, Criteria.POWER_LOW, Criteria.ACCURACY_FINE)
        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
        locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
        locationManager.setTestProviderStatus(LocationManager.GPS_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis())
        locationManager.setTestProviderStatus(LocationManager.NETWORK_PROVIDER, LocationProvider.AVAILABLE, null, System.currentTimeMillis())

        // Setup timer task
        currentTargetIndex = 0
        timer = Timer(true)
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    for (i in currentTargetIndex until mockTargetList.size) {
                        if (mockTargetList[i].enabled) {
                            currentTargetIndex = i
                            val location = mockTargetList[i].location
                            location.provider = LocationManager.GPS_PROVIDER
                            location.accuracy = ACCURACY
                            location.time = Date().time
                            location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
                            location.provider = LocationManager.NETWORK_PROVIDER
                            locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, location)
                            Log.i("TESTMLP", "传入模拟位置序号：" + currentTargetIndex)
                            currentTargetIndex++
                            break
                        }
                    }
                    if (currentTargetIndex == mockTargetList.size) currentTargetIndex = 0

                }
            }
        }, 0, INTERVAL)

        // Set the online state
        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(getString(R.string.pref_is_service_online_key), true)
            .apply()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {

        stopForeground(true)

        timer.cancel()
        timer.purge()

        locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
        locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
        locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)

        PreferenceManager
            .getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(getString(R.string.pref_is_service_online_key), false)
            .apply()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        const val INTERVAL = 5000L
        const val ACCURACY = 5.0F
        private const val CHANNEL_ID = "M.L.P. Notification"
        private const val FOREGROUND_ID = 1
    }
}