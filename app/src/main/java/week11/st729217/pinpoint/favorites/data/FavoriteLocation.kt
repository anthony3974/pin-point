package week11.st729217.pinpoint.favorites.data

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

// Firestore requires a no-argument constructor, so we provide default values.
data class FavoriteLocation(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
) {
    // This allows us to still use the LatLng object in our app's UI code.
    // Firestore will ignore this property.
    @get:Exclude
    val location: LatLng
        get() = LatLng(latitude, longitude)
}
