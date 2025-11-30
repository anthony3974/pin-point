package week11.st729217.pinpoint

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import week11.st729217.pinpoint.addFriendFeature.screen.AddFriendScreen
import week11.st729217.pinpoint.addFriendFeature.screen.MyFriendsScreen
import week11.st729217.pinpoint.favorites.ui.FavoritesPage
import week11.st729217.pinpoint.favorites.viewmodel.FavoritesViewModel
import week11.st729217.pinpoint.location.LocationScreen
import week11.st729217.pinpoint.profile.ui.ProfileScreen

sealed class MainScreen(val route: String, val title: String, val icon: ImageVector) {
    object Profile : MainScreen("profile", "Profile", Icons.Default.Person)
    object Location : MainScreen("location", "Map", Icons.Default.Place)
    object Favorites : MainScreen("favorites", "Favorites", Icons.Default.Favorite)
    object AddFriends : MainScreen("addFriends", "Add Friends", Icons.Default.Favorite)
    object MyFriends : MainScreen("myFriends", "My Friends", Icons.Default.Favorite)
}

val mainScreens = listOf(
    MainScreen.Profile,
    MainScreen.Location,
    MainScreen.Favorites,
    MainScreen.AddFriends,
    MainScreen.MyFriends
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(favoritesViewModel: FavoritesViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                mainScreens.forEach { screen ->
                    NavigationDrawerItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = navController.currentBackStackEntryAsState().value?.destination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(navController.currentBackStackEntryAsState().value?.destination?.route?.replaceFirstChar { it.uppercase() } ?: "PinPoint") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = MainScreen.Location.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(MainScreen.Location.route) {
                    LocationScreen(favoritesViewModel = favoritesViewModel)
                }
                composable(MainScreen.Favorites.route) {
                    FavoritesPage(favoritesViewModel = favoritesViewModel)
                }
                composable(MainScreen.Profile.route) {
                    ProfileScreen(
                        onSignOut = { FirebaseAuth.getInstance().signOut() },
                        onBack = { navController.popBackStack() } // Or navigate somewhere specific
                    )
                }
                composable (MainScreen.AddFriends.route){
                    AddFriendScreen(
                    )
                }
                composable (MainScreen.MyFriends.route){
                    MyFriendsScreen(
                    )
                }
            }
        }
    }
}
