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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.FirestoreSubjectService
import com.example.aprendeaprender.data.remote.FirestoreUserService
import com.example.aprendeaprender.data.repository.AuthRepository
import com.example.aprendeaprender.data.repository.ProfileRepository
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.ui.screens.auth.forgotpassword.ForgotPasswordScreen
import com.example.aprendeaprender.ui.screens.auth.login.LoginScreen
import com.example.aprendeaprender.ui.screens.auth.register.RegisterScreen
import com.example.aprendeaprender.ui.screens.auth.resetpassword.ResetPasswordEmailSentScreen
import com.example.aprendeaprender.ui.screens.auth.splash.SplashScreen
import com.example.aprendeaprender.ui.screens.auth.verifyemail.VerifyEmailScreen
import com.example.aprendeaprender.ui.screens.home.HomeRoute
import com.example.aprendeaprender.ui.screens.profile.ProfileRoute
import com.example.aprendeaprender.ui.screens.subjects.CreateSubjectScreen
import com.example.aprendeaprender.ui.screens.subjects.SubjectListScreen
import com.example.aprendeaprender.ui.screens.subjects.SubjectSuccessScreen
import com.example.aprendeaprender.viewmodel.AuthEvent
import com.example.aprendeaprender.viewmodel.AuthViewModel
import com.example.aprendeaprender.viewmodel.HomeViewModel
import com.example.aprendeaprender.viewmodel.ProfileViewModel
import com.example.aprendeaprender.viewmodel.SubjectViewModel
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

    val subjectRepository = remember {
        SubjectRepository(
            authService = authService,
            subjectService = FirestoreSubjectService()
        )
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel(authRepository) as T
            }
        }
    )

    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ProfileViewModel(profileRepository) as T
            }
        }
    )

    val homeViewModel: HomeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HomeViewModel(profileRepository) as T
            }
        }
    )

    val subjectViewModel: SubjectViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SubjectViewModel(subjectRepository) as T
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
            // ── Auth ──

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

            // ── Home ──

            composable(Routes.HOME) {
                val homeUiState by homeViewModel.uiState.collectAsState()

                LaunchedEffect(Unit) {
                    homeViewModel.cargarDatosHome()
                }

                HomeRoute(
                    uiState = homeUiState,
                    onNavigateToProfile = {
                        navController.navigate(Routes.PROFILE) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToSubjects = {
                        navController.navigate(Routes.SUBJECT_LIST) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToTasks = {
                        // TODO: módulo tareas
                    },
                    onNavigateToChallenges = {
                        // TODO: módulo retos
                    },
                    onEnrollSubjectClick = {
                        subjectViewModel.resetCreateForm()
                        navController.navigate(Routes.CREATE_SUBJECT) {
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ── Profile ──

            composable(Routes.PROFILE) {
                ProfileRoute(
                    viewModel = profileViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onCerrarSesionClick = {
                        authViewModel.cerrarSesion()
                    }
                )
            }

            // ── Subjects ──

            composable(Routes.CREATE_SUBJECT) {
                val createUiState by subjectViewModel.createUiState.collectAsState()

                LaunchedEffect(createUiState.materiaCreada) {
                    if (createUiState.materiaCreada) {
                        navController.navigate(Routes.SUBJECT_SUCCESS) {
                            popUpTo(Routes.CREATE_SUBJECT) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                CreateSubjectScreen(
                    uiState = createUiState,
                    onAsignaturaChange = subjectViewModel::onAsignaturaChange,
                    onInstructorChange = subjectViewModel::onInstructorChange,
                    onTemaChange = subjectViewModel::onTemaChange,
                    onAgregarTema = subjectViewModel::agregarTema,
                    onEliminarTema = subjectViewModel::eliminarTema,
                    onInscribirClick = subjectViewModel::inscribirMateria,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.SUBJECT_SUCCESS) {
                SubjectSuccessScreen(
                    onAddTaskClick = {
                        // TODO: navegar a crear tarea
                        subjectViewModel.resetCreateForm()
                        navController.navigateClearingStack(Routes.HOME)
                    },
                    onFinishClick = {
                        subjectViewModel.resetCreateForm()
                        navController.navigateClearingStack(Routes.HOME)
                    }
                )
            }

            composable(Routes.SUBJECT_LIST) {
                val listUiState by subjectViewModel.listUiState.collectAsState()

                LaunchedEffect(Unit) {
                    subjectViewModel.cargarMaterias()
                }

                SubjectListScreen(
                    uiState = listUiState,
                    onDeleteClick = subjectViewModel::eliminarMateria,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}