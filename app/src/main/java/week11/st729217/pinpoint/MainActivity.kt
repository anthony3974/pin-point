package week11.st729217.pinpoint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import week11.st729217.pinpoint.ui.theme.PinpointTheme

object AppDestinations {
    const val LOCATION_ROUTE = "location"
    const val FAVORITES_ROUTE = "favorites"
}

sealed class Screen(val route: String, val title: String, val icon: Int) {
    object Location :
        Screen(AppDestinations.LOCATION_ROUTE, "Map", R.drawable.ic_map)

    object Favorites : Screen(
        AppDestinations.FAVORITES_ROUTE,
        "Favorites",
        R.drawable.ic_favorites
    )
}

val items = listOf(
    Screen.Location,
    Screen.Favorites,
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PinpointTheme {
                // 2. Set up the NavController
                val navController = rememberNavController()

                Scaffold(
                    // 3. Add a Bottom Navigation Bar
                    bottomBar = {
                        NavigationBar {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            painterResource(id = screen.icon),
                                            contentDescription = screen.title
                                        )
                                    },
                                    label = { Text(screen.title) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            // Pop up to the start destination of the graph to
                                            // avoid building up a large stack of destinations
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            // Avoid multiple copies of the same destination when re-selecting the same item
                                            launchSingleTop = true
                                            // Restore state when re-selecting a previously selected item
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    // 4. Set up the NavHost to swap screens
                    NavHost(
                        navController = navController,
                        startDestination = AppDestinations.LOCATION_ROUTE, // Your map screen is the start
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(AppDestinations.LOCATION_ROUTE) {
                            LocationScreen() // Your existing map screen
                        }
                        composable(AppDestinations.FAVORITES_ROUTE) {
                            FavoritesPage() // Your new favorites screen!
                        }
                    }
                }
            }
        }
    }
}
