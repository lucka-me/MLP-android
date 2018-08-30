package labs.lucka.mlp

import android.location.Location
import java.io.Serializable

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