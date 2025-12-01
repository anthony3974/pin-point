package week11.st729217.pinpoint.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import week11.st729217.pinpoint.favorites.ui.FavoritesPage
import week11.st729217.pinpoint.favorites.viewmodel.FavoritesViewModel

object AppDestinations {
    const val LOCATION_ROUTE = "location"
    const val FAVORITES_ROUTE = "favorites"
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Location : Screen(AppDestinations.LOCATION_ROUTE, "Map", Icons.Default.Place)
    object Favorites : Screen(AppDestinations.FAVORITES_ROUTE, "Favorites", Icons.Default.Favorite)
}

val items = listOf(
    Screen.Location,
    Screen.Favorites,
)

@Composable
fun HomeScreen(
    onOpenProfile: () -> Unit,
    favoritesViewModel: FavoritesViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenProfile) {
                Icon(Icons.Default.Person, contentDescription = "Profile")
            }
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.LOCATION_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.LOCATION_ROUTE) {
                LocationScreen(favoritesViewModel = favoritesViewModel)
            }
            composable(AppDestinations.FAVORITES_ROUTE) {
                FavoritesPage(favoritesViewModel = favoritesViewModel)
            }
        }

    }
}
