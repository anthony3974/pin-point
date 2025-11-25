package week11.st729217.pinpoint.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import week11.st729217.pinpoint.ui.screens.LoginScreen
import week11.st729217.pinpoint.ui.screens.RegisterScreen
import week11.st729217.pinpoint.ui.screens.HomeScreen
import week11.st729217.pinpoint.ui.screens.ProfileScreen

sealed class Route(val route: String) {
    object Splash: Route("splash")
    object Login: Route("login")
    object Register: Route("register")
    object Home: Route("home")
    object Profile: Route("profile")
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
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
                onOpenProfile = { navController.navigate(Route.Profile.route) }
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
