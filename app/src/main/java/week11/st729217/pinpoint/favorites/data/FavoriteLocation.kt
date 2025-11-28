package week11.st729217.pinpoint.favorites.data

import com.google.android.gms.maps.model.LatLng
import java.util.UUID

data class FavoriteLocation(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val location: LatLng
)
