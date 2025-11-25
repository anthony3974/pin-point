package week11.st729217.pinpoint

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import java.util.UUID

class FavoritesViewModel : ViewModel() {
    val favoriteLocations = mutableStateListOf<FavoriteLocation>()

    init {
        favoriteLocations.addAll(
            listOf(
                FavoriteLocation(name = "Eiffel Tower", location = LatLng(48.8584, 2.2945)),
                FavoriteLocation(name = "Colosseum", location = LatLng(41.8902, 12.4922)),
                FavoriteLocation(name = "Sydney Opera House", location = LatLng(-33.8568, 151.2153))
            )
        )
    }

    fun updateName(id: UUID, newName: String) {
        val index = favoriteLocations.indexOfFirst { it.id == id }
        if (index != -1) {
            favoriteLocations[index] = favoriteLocations[index].copy(name = newName)
        }
    }

    fun removeFavorite(id: UUID) {
        favoriteLocations.removeAll { it.id == id }
    }
}
