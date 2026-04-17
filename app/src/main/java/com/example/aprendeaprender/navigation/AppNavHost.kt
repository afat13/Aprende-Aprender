package com.example.aprendeaprender.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.FirestoreUserService
import com.example.aprendeaprender.data.repository.AuthRepository
import com.example.aprendeaprender.data.repository.ProfileRepository
import com.example.aprendeaprender.ui.screens.auth.forgotpassword.ForgotPasswordScreen
import com.example.aprendeaprender.ui.screens.auth.login.LoginScreen
import com.example.aprendeaprender.ui.screens.auth.register.RegisterScreen
import com.example.aprendeaprender.ui.screens.auth.resetpassword.ResetPasswordEmailSentScreen
import com.example.aprendeaprender.ui.screens.auth.splash.SplashScreen
import com.example.aprendeaprender.ui.screens.auth.verifyemail.VerifyEmailScreen
import com.example.aprendeaprender.ui.screens.home.HomeRoute
import com.example.aprendeaprender.ui.screens.home.HomeUiState
import com.example.aprendeaprender.ui.screens.profile.ProfileRoute
import com.example.aprendeaprender.viewmodel.AuthEvent
import com.example.aprendeaprender.viewmodel.AuthViewModel
import com.example.aprendeaprender.viewmodel.ProfileViewModel
import com.example.aprendeaprender.viewmodel.ProfileViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import kotlinx.coroutines.delay

private fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val authService = remember { FirebaseAuthService() }
    val firestoreUserService = remember { FirestoreUserService() }

    val authRepository = remember {
        AuthRepository(
            authService = authService,
            firestoreUserService = firestoreUserService
        )
    }

    val profileRepository = remember {
        ProfileRepository(
            authService = authService,
            firestoreUserService = firestoreUserService
        )
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                    return AuthViewModel(authRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    return ProfileViewModel(profileRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val loginUiState by authViewModel.loginUiState.collectAsState()
    val registerUiState by authViewModel.registerUiState.collectAsState()
    val forgotPasswordUiState by authViewModel.forgotPasswordUiState.collectAsState()
    val verifyEmailUiState by authViewModel.verifyEmailUiState.collectAsState()

    LaunchedEffect(authViewModel) {
        authViewModel.authEvents.collect { event ->
            when (event) {
                AuthEvent.NavigateToHome -> {
                    navController.navigateClearingStack(Routes.HOME)
                }

                AuthEvent.NavigateToLogin -> {
                    navController.navigateClearingStack(Routes.LOGIN)
                }

                AuthEvent.NavigateToVerifyEmail -> {
                    navController.navigateClearingStack(Routes.VERIFY_EMAIL)
                }

                AuthEvent.NavigateToResetPasswordEmailSent -> {
                    navController.navigate(Routes.RESET_PASSWORD_EMAIL_SENT) {
                        launchSingleTop = true
                    }
                }

                is AuthEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.messageResId)
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.SPLASH) {
                LaunchedEffect(Unit) {
                    delay(1200)
                    authViewModel.verificarSesion()
                }

                SplashScreen()
            }

            composable(Routes.LOGIN) {
                LoginScreen(
                    email = loginUiState.correo,
                    password = loginUiState.contrasena,
                    isLoading = loginUiState.cargando,
                    errorResId = loginUiState.mensajeErrorResId,
                    onEmailChange = authViewModel::onLoginCorreoChange,
                    onPasswordChange = authViewModel::onLoginContrasenaChange,
                    onLoginClick = authViewModel::iniciarSesion,
                    onForgotPasswordClick = {
                        navController.navigate(Routes.FORGOT_PASSWORD) {
                            launchSingleTop = true
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Routes.REGISTER) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    nombre = registerUiState.nombre,
                    apellido = registerUiState.apellido,
                    edad = registerUiState.edad,
                    carrera = registerUiState.carrera,
                    email = registerUiState.correo,
                    password = registerUiState.contrasena,
                    confirmPassword = registerUiState.confirmarContrasena,
                    acceptedTerms = registerUiState.aceptaTerminos,
                    isLoading = registerUiState.cargando,
                    errorResId = registerUiState.mensajeErrorResId,
                    onNombreChange = authViewModel::onRegisterNombreChange,
                    onApellidoChange = authViewModel::onRegisterApellidoChange,
                    onEdadChange = authViewModel::onRegisterEdadChange,
                    onCarreraChange = authViewModel::onRegisterCarreraChange,
                    onEmailChange = authViewModel::onRegisterCorreoChange,
                    onPasswordChange = authViewModel::onRegisterContrasenaChange,
                    onConfirmPasswordChange = authViewModel::onRegisterConfirmarContrasenaChange,
                    onAcceptedTermsChange = authViewModel::onRegisterAceptaTerminosChange,
                    onRegisterClick = authViewModel::registrarUsuario,
                    onBackToLoginClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    email = forgotPasswordUiState.correo,
                    isLoading = forgotPasswordUiState.cargando,
                    errorResId = forgotPasswordUiState.mensajeErrorResId,
                    successResId = forgotPasswordUiState.mensajeExitoResId,
                    onEmailChange = authViewModel::onForgotPasswordCorreoChange,
                    onSendLinkClick = authViewModel::enviarCorreoRecuperacion,
                    onBackToLoginClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.VERIFY_EMAIL) {
                VerifyEmailScreen(
                    uiState = verifyEmailUiState,
                    onReenviarCorreoClick = authViewModel::reenviarCorreoVerificacion,
                    onYaVerifiqueClick = authViewModel::revisarEstadoVerificacion,
                    onVolverLoginClick = authViewModel::cerrarSesion
                )
            }

            composable(Routes.RESET_PASSWORD_EMAIL_SENT) {
                ResetPasswordEmailSentScreen(
                    correo = forgotPasswordUiState.correoEnviadoA,
                    onVolverLoginClick = {
                        navController.navigateClearingStack(Routes.LOGIN)
                    }
                )
            }

            composable(Routes.HOME) {
                HomeRoute(
                    uiState = HomeUiState(
                        userName = "Andrés",
                        inProgressTasks = 0,
                        overdueTasks = 0,
                        completedTasks = 0,
                        todayTasks = 0,
                        progress = 0f
                    ),
                    onNavigateToProfile = {
                        navController.navigate(Routes.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSubjects = {
                        // pendiente
                    },
                    onNavigateToTasks = {
                        // pendiente
                    },
                    onNavigateToChallenges = {
                        // pendiente
                    },
                    onEnrollSubjectClick = {
                        // pendiente
                    }
                )
            }

            composable(Routes.PROFILE) {
                ProfileRoute(
                    viewModel = profileViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}