package week11.st729217.pinpoint.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import week11.st729217.pinpoint.auth.ui.LoginScreen
import week11.st729217.pinpoint.auth.ui.RegisterScreen
import week11.st729217.pinpoint.favorites.viewmodel.FavoritesViewModel
import week11.st729217.pinpoint.home.ui.HomeScreen
import week11.st729217.pinpoint.profile.ui.ProfileScreen

sealed class Route(val route: String) {
    object Login: Route("login")
    object Register: Route("register")
    object Home: Route("home")
    object Profile: Route("profile")
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    favoritesViewModel: FavoritesViewModel
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    NavHost(navController = navController, startDestination = if (auth.currentUser != null) Route.Home.route else Route.Login.route) {
        composable(Route.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Route.Register.route) }
            )
        }

        composable(Route.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Route.Home.route) {
                        popUpTo(Route.Register.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Route.Home.route) {
            HomeScreen(
                onOpenProfile = { navController.navigate(Route.Profile.route) },
                favoritesViewModel = favoritesViewModel
            )
        }

        composable(Route.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(Route.Login.route) {
                        popUpTo(Route.Home.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
