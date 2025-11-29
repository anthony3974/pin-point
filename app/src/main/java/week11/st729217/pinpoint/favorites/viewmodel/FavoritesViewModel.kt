package week11.st729217.pinpoint.favorites.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import week11.st729217.pinpoint.favorites.data.FavoriteLocation

class FavoritesViewModel : ViewModel() {
    private val _favoriteLocations = mutableStateListOf<FavoriteLocation>()
    val favoriteLocations: List<FavoriteLocation> = _favoriteLocations

    private val auth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

    init {
        listenForFavorites()
    }

    private fun listenForFavorites() {
        auth.addAuthStateListener { firebaseAuth ->
            val userId = firebaseAuth.currentUser?.uid
            if (userId != null) {
                // Path is now users/{userId}/favorites
                firestore.collection("users").document(userId).collection("favorites")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("FavoritesViewModel", "Error listening for favorites", error)
                            return@addSnapshotListener
                        }

                        snapshot?.let {
                            val favorites = it.toObjects<FavoriteLocation>()
                            _favoriteLocations.clear()
                            _favoriteLocations.addAll(favorites)
                        }
                    }
            } else {
                _favoriteLocations.clear()
            }
        }
    }

    fun addFavorite(latLng: LatLng, name: String) {
        val userId = auth.currentUser?.uid ?: return

        val newFavorite = FavoriteLocation(
            name = name,
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )

        firestore.collection("users").document(userId).collection("favorites").add(newFavorite)
            .addOnSuccessListener { Log.d("FavoritesViewModel", "Favorite added successfully") }
            .addOnFailureListener { e -> Log.e("FavoritesViewModel", "Error adding favorite", e) }
    }

    fun updateName(id: String, newName: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("favorites").document(id)
            .update("name", newName)
            .addOnSuccessListener { Log.d("FavoritesViewModel", "Favorite updated successfully") }
            .addOnFailureListener { e -> Log.e("FavoritesViewModel", "Error updating favorite", e) }
    }

    fun removeFavorite(id: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId).collection("favorites").document(id)
            .delete()
            .addOnSuccessListener { Log.d("FavoritesViewModel", "Favorite removed successfully") }
            .addOnFailureListener { e -> Log.e("FavoritesViewModel", "Error removing favorite", e) }
    }
}
