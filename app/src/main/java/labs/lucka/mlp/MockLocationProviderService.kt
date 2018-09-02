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
import android.support.v4.app.NotificationCompat
import org.jetbrains.anko.runOnUiThread
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.util.*

/**
 * A (foreground) service to provide mock location
 *
 * ## Attributes
 * ### Private
 * - [mockTargetList]
 * - [enabledMockTargetList]
 * - [locationManager]
 * - [timer]
 * - [currentTargetIndex]
 * - [notificationManager]
 * - [notificationId]
 * ### Static
 * - [INTERVAL]
 * - [ACCURACY]
 * - [CHANNEL_ID]
 * - [FOREGROUND_ID]
 *
 * ## Methods
 * ### Overridden
 * - [onStartCommand]
 * - [onDestroy]
 * - [onBind]
 * ### Private
 * - [pushNotification]
 *
 * @author lucka-me
 * @since 0.1
 *
 * @property [mockTargetList] ArrayList for mock targets
 * @property [enabledMockTargetList] ArrayList for enabled mock targets from [mockTargetList]
 * @property [locationManager] Used to send mock location
 * @property [timer] Used to provide mock location with [INTERVAL]
 * @property [currentTargetIndex] Used to identify which target in [enabledMockTargetList] should be sent
 * @property [notificationManager] Used to send notifications and create notification channel in O and above
 * @property [notificationId] Used as unique id for notifications
 * @property [INTERVAL] Interval between two mock location updates
 * @property [ACCURACY] Accuracy set for mock locations
 * @property [CHANNEL_ID] Used for notification channel
 * @property [FOREGROUND_ID] Used as id of foreground service notification
 */
class MockLocationProviderService : Service() {

    private var mockTargetList: ArrayList<MockTarget> = ArrayList(0)
    private var enabledMockTargetList: ArrayList<MockTarget> = ArrayList(0)
    private lateinit var locationManager: LocationManager
    private var timer: Timer = Timer(true)
    private var currentTargetIndex: Int = 0
    private lateinit var notificationManager: NotificationManager
    private var notificationId = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Setup notification and foreground service
        notificationManager =
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
        notificationId = 0

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
            pushNotification(error.message)
            stopSelf()
        }
        for (mockTarget in mockTargetList) {
            if (mockTarget.enabled)
                enabledMockTargetList.add(mockTarget)
        }

        // Setup location manager
        try {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.addTestProvider(
                LocationManager.GPS_PROVIDER,
                false, false,false,false,
                true,true, true,
                Criteria.POWER_LOW, Criteria.ACCURACY_FINE
            )
            locationManager.addTestProvider(
                LocationManager.NETWORK_PROVIDER,
                false, false,false,false,
                true,true, true,
                Criteria.POWER_LOW, Criteria.ACCURACY_FINE
            )
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true)
            locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true)
            locationManager.setTestProviderStatus(
                LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null,
                System.currentTimeMillis()
            )
            locationManager.setTestProviderStatus(
                LocationManager.NETWORK_PROVIDER,
                LocationProvider.AVAILABLE,
                null,
                System.currentTimeMillis()
            )
        } catch (error: Exception) {
            pushNotification(error.message)
            stopSelf()
        }


        // Setup timer task
        currentTargetIndex = 0
        timer = Timer(true)
        timer.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {

                    val location = enabledMockTargetList[currentTargetIndex].location
                    location.provider = LocationManager.GPS_PROVIDER
                    location.accuracy = ACCURACY
                    location.time = Date().time
                    location.elapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos()
                    try {
                        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location)
                        location.provider = LocationManager.NETWORK_PROVIDER
                        locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, location)
                    } catch (error: Exception) {
                        pushNotification(error.message)
                        stopSelf()
                    }
                    currentTargetIndex++
                    if (currentTargetIndex == enabledMockTargetList.size) currentTargetIndex = 0

                }
            }
        }, 0, INTERVAL)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {

        stopForeground(true)

        timer.cancel()
        timer.purge()

        try {
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, false)
            locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, false)
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER)
            locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER)
        } catch (error: Exception) {
            pushNotification(error.message)
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    /**
     * Push a notification
     *
     * @param [message] The message to notify
     *
     * @author lucka-me
     * @since 0.1.1
     */
    private fun pushNotification(message: String?) {
        notificationManager.notify(
            notificationId,
            NotificationCompat.Builder(this.applicationContext, CHANNEL_ID)
                .setContentTitle(getString(R.string.service_caught_error_title))
                .setContentText(message?: getString(R.string.service_caught_error_text_default))
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build()
        )
        notificationId++
    }

    companion object {
        const val INTERVAL = 5000L
        const val ACCURACY = 5.0F
        private const val CHANNEL_ID = "M.L.P. Notification"
        private const val FOREGROUND_ID = 1
    }
}