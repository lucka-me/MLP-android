package labs.lucka.mlp

import android.location.Location

/**
 * A data class used to save data of a mock target
 *
 * ## Changelog
 * ### 0.2
 * - Switched to data class
 * - Add [title], [interval], [accuracy] and [altitude]
 *
 * ## Public Attribute
 * - [longitude]
 * - [latitude]
 * - [enabled]
 * - [title]
 * - [interval]
 * - [accuracy]
 * - [altitude]
 * ### Getters and Setters
 * - [location]
 *
 * @param [longitude] Longitude of the mock target
 * @param [latitude] Latitude of the mock target
 * @param [enabled] If enabled when MLP Service running
 *
 * @author lucka-me
 * @since 0.1
 *
 * @property [location] Getter and Setter of location ([longitude] and  [latitude])
 */
data class MockTarget(
    var longitude: Double, var latitude: Double, var enabled: Boolean = true,
    var title: String = "", var interval: Long = 5000,
    var accuracy: Float? = null, var altitude: Double? = null
) {

    var location: Location
        set(value) {
            longitude = value.longitude
            latitude = value.latitude
            accuracy = if (value.hasAccuracy()) value.accuracy else null
            altitude = if (value.hasAltitude()) value.altitude else null
        }
        get() {
            val location = Location("")
            location.longitude = longitude
            location.latitude = latitude
            if (accuracy != null) location.accuracy = accuracy!!
            if (altitude != null) location.altitude = altitude!!
            return location
        }
}