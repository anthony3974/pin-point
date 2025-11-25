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
                val navController = rememberNavController()

                Scaffold(
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
                            LocationScreen()
                        } // <-- Add closing brace here

                        composable(AppDestinations.FAVORITES_ROUTE) {
                            FavoritesPage()
                        } // <-- Add closing brace here
                    }

                }
            }
        }
    }
}
