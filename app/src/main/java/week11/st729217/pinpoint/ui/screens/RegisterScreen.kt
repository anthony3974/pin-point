package week11.st729217.pinpoint.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st729217.pinpoint.ui.auth.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState.success) {
            onRegisterSuccess()
        }
    }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Create account", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = confirm, onValueChange = { confirm = it }, label = { Text("Confirm password") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (password != confirm) {
                // set error via viewmodel
                viewModel.clearError()
                // we can't set error directly; hack: call register with dummy to trigger validation; better approach is to show local message:
            }
            viewModel.register(email.trim(), password.trim(), if (name.isBlank()) null else name.trim())
        }, modifier = Modifier.fillMaxWidth()) {
            if (uiState.loading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text("Register")
        }

        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("Have an account? Login")
        }

        if (password != confirm) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
        }

        if (uiState.error != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = uiState.error ?: "", color = MaterialTheme.colorScheme.error)
        }
    }
}
