package labs.lucka.mlp

import android.location.Location
import java.io.Serializable

/**
 * A serializable class used to save data of a mock target
 *
 * ## Public Attribute
 * - [longitude]
 * - [latitude]
 * - [enabled]
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
class MockTarget(var longitude: Double, var latitude: Double, var enabled: Boolean = true)
    : Serializable {

    var location: Location
        set(value) {
            this.longitude = value.longitude
            this.latitude = value.latitude
        }
        get() {
            val location = Location("")
            location.longitude = this.longitude
            location.latitude = this.latitude
            return location
        }
}