package week11.st729217.pinpoint.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import week11.st729217.pinpoint.ui.screens.LoginScreen
import week11.st729217.pinpoint.ui.screens.RegisterScreen

sealed class AuthRoute(val route: String) {
    object Login: AuthRoute("login")
    object Register: AuthRoute("register")
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = AuthRoute.Login.route) {
        composable(AuthRoute.Login.route) {
            LoginScreen(
                onLoginSuccess = { /* The MainActivity now handles this */ },
                onNavigateToRegister = { navController.navigate(AuthRoute.Register.route) }
            )
        }

        composable(AuthRoute.Register.route) {
            RegisterScreen(
                onRegisterSuccess = { /* The MainActivity now handles this */ },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
