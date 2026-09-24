package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Employee
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmployeeDetailScreen
import com.example.ui.screens.RegisterEmployeeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.EmployeeViewModel
import kotlinx.coroutines.launch

sealed class Screen {
    data object Dashboard : Screen()
    data class Register(val existingEmployee: Employee? = null) : Screen()
    data class Detail(val employeeId: Long) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EmployeeRegisterApp()
            }
        }
    }
}

@Composable
fun EmployeeRegisterApp(
    viewModel: EmployeeViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val allEmployees by viewModel.allEmployees.collectAsStateWithLifecycle()

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearSnackbar()
            }
        }
    }

    // Handle system back button
    BackHandler(enabled = currentScreen !is Screen.Dashboard) {
        currentScreen = Screen.Dashboard
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (val screen = currentScreen) {
            is Screen.Dashboard -> {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = {
                        currentScreen = Screen.Register(existingEmployee = null)
                    },
                    onNavigateToDetail = { employeeId ->
                        currentScreen = Screen.Detail(employeeId)
                    }
                )
            }
            is Screen.Register -> {
                RegisterEmployeeScreen(
                    viewModel = viewModel,
                    existingEmployee = screen.existingEmployee,
                    onNavigateBack = {
                        currentScreen = Screen.Dashboard
                    }
                )
            }
            is Screen.Detail -> {
                EmployeeDetailScreen(
                    employeeId = screen.employeeId,
                    viewModel = viewModel,
                    onNavigateBack = {
                        currentScreen = Screen.Dashboard
                    },
                    onNavigateToEdit = { emp ->
                        currentScreen = Screen.Register(existingEmployee = emp)
                    }
                )
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

