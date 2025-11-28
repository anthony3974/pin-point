package week11.st729217.pinpoint.favorites.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import java.util.UUID
import week11.st729217.pinpoint.favorites.data.FavoriteLocation

class FavoritesViewModel : ViewModel() {
    val favoriteLocations = mutableStateListOf<FavoriteLocation>()

    fun addFavorite(latLng: LatLng) {
        val newFavorite = FavoriteLocation(
            name = "New Location", // Give it a default name
            location = latLng
        )
        favoriteLocations.add(newFavorite)
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
