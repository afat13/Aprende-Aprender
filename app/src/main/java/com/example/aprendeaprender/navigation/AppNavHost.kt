package com.example.aprendeaprender.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aprendeaprender.data.remote.FirebaseAuthService
import com.example.aprendeaprender.data.remote.RealtimeSubjectService
import com.example.aprendeaprender.data.remote.RealtimeTaskService
import com.example.aprendeaprender.data.remote.FirestoreUserService
import com.example.aprendeaprender.data.repository.AuthRepository
import com.example.aprendeaprender.data.repository.ProfileRepository
import com.example.aprendeaprender.data.repository.SubjectRepository
import com.example.aprendeaprender.data.repository.TaskRepository
import com.example.aprendeaprender.ui.components.BottomNavBar
import com.example.aprendeaprender.ui.components.BottomNavDestination
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
import com.example.aprendeaprender.ui.screens.tasks.CreateTaskScreen
import com.example.aprendeaprender.ui.screens.tasks.TaskListScreen
import com.example.aprendeaprender.ui.theme.CyanAccent
import com.example.aprendeaprender.ui.theme.DarkBackground
import com.example.aprendeaprender.viewmodel.AuthEvent
import com.example.aprendeaprender.viewmodel.AuthViewModel
import com.example.aprendeaprender.viewmodel.HomeViewModel
import com.example.aprendeaprender.viewmodel.ProfileViewModel
import com.example.aprendeaprender.viewmodel.SubjectViewModel
import com.example.aprendeaprender.viewmodel.TaskViewModel
import kotlinx.coroutines.delay

// Rutas donde se muestra el BottomNavBar
private val bottomNavRoutes = setOf(
    Routes.HOME,
    Routes.SUBJECT_LIST,
    Routes.TASK_LIST,
    Routes.PROFILE
)

// Rutas donde se muestra el FAB de nueva tarea
private val fabRoutes = setOf(Routes.TASK_LIST)

private fun NavHostController.navigateClearingStack(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomNav = currentRoute in bottomNavRoutes
    val showFab = currentRoute in fabRoutes

    // Repositories
    val authService = remember { FirebaseAuthService() }
    val firestoreUserService = remember { FirestoreUserService() }
    val authRepository = remember { AuthRepository(authService = authService, userService = firestoreUserService) }
    val profileRepository = remember { ProfileRepository(authService = authService, userService = firestoreUserService) }
    val subjectRepository = remember { SubjectRepository(authService = authService, subjectService = RealtimeSubjectService()) }
    val taskRepository = remember { TaskRepository(authService = authService, taskService = RealtimeTaskService()) }

    // ViewModels
    val authViewModel: AuthViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(authRepository) as T
    })
    val profileViewModel: ProfileViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(profileRepository) as T
    })
    val homeViewModel: HomeViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(profileRepository) as T
    })
    val subjectViewModel: SubjectViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SubjectViewModel(subjectRepository) as T
    })
    val taskViewModel: TaskViewModel = viewModel(factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = TaskViewModel(taskRepository, subjectRepository) as T
    })

    val loginUiState by authViewModel.loginUiState.collectAsState()
    val registerUiState by authViewModel.registerUiState.collectAsState()
    val forgotPasswordUiState by authViewModel.forgotPasswordUiState.collectAsState()
    val verifyEmailUiState by authViewModel.verifyEmailUiState.collectAsState()

    LaunchedEffect(authViewModel) {
        authViewModel.authEvents.collect { event ->
            when (event) {
                AuthEvent.NavigateToHome -> navController.navigateClearingStack(Routes.HOME)
                AuthEvent.NavigateToLogin -> navController.navigateClearingStack(Routes.LOGIN)
                AuthEvent.NavigateToVerifyEmail -> navController.navigateClearingStack(Routes.VERIFY_EMAIL)
                AuthEvent.NavigateToResetPasswordEmailSent -> {
                    navController.navigate(Routes.RESET_PASSWORD_EMAIL_SENT) { launchSingleTop = true }
                }
                is AuthEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = context.getString(event.messageResId))
                }
            }
        }
    }

    // Destino seleccionado del BottomNav
    val selectedBottomNav = when (currentRoute) {
        Routes.HOME -> BottomNavDestination.HOME
        Routes.SUBJECT_LIST -> BottomNavDestination.SUBJECTS
        Routes.TASK_LIST -> BottomNavDestination.TASKS
        Routes.PROFILE -> BottomNavDestination.PROFILE
        else -> BottomNavDestination.HOME
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = DarkBackground,
        bottomBar = {
            if (showBottomNav) {
                BottomNavBar(
                    selectedDestination = selectedBottomNav,
                    onDestinationSelected = { destination ->
                        when (destination) {
                            BottomNavDestination.HOME -> navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                            BottomNavDestination.SUBJECTS -> navController.navigate(Routes.SUBJECT_LIST) {
                                launchSingleTop = true
                            }
                            BottomNavDestination.TASKS -> navController.navigate(Routes.TASK_LIST) {
                                launchSingleTop = true
                            }
                            BottomNavDestination.CHALLENGES -> { /* TODO */ }
                            BottomNavDestination.PROFILE -> navController.navigate(Routes.PROFILE) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        taskViewModel.resetCreateForm()
                        taskViewModel.cargarMaterias()
                        navController.navigate(Routes.CREATE_TASK) { launchSingleTop = true }
                    },
                    containerColor = CyanAccent,
                    contentColor = Color(0xFF0D1B2A),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Nueva tarea",
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.padding(innerPadding)
        ) {
            // ── Auth ──
            composable(Routes.SPLASH) {
                LaunchedEffect(Unit) { delay(1200); authViewModel.verificarSesion() }
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
                    onForgotPasswordClick = { navController.navigate(Routes.FORGOT_PASSWORD) { launchSingleTop = true } },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) { launchSingleTop = true } }
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
                    onBackToLoginClick = { navController.popBackStack() }
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
                    onBackToLoginClick = { navController.popBackStack() }
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
                    onVolverLoginClick = { navController.navigateClearingStack(Routes.LOGIN) }
                )
            }

            // ── Home ──
            composable(Routes.HOME) {
                val homeUiState by homeViewModel.uiState.collectAsState()
                LaunchedEffect(Unit) { homeViewModel.cargarDatosHome() }

                HomeRoute(
                    uiState = homeUiState,
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) { launchSingleTop = true } },
                    onNavigateToSubjects = { navController.navigate(Routes.SUBJECT_LIST) { launchSingleTop = true } },
                    onNavigateToTasks = { navController.navigate(Routes.TASK_LIST) { launchSingleTop = true } },
                    onNavigateToChallenges = { },
                    onEnrollSubjectClick = {
                        subjectViewModel.resetCreateForm()
                        navController.navigate(Routes.CREATE_SUBJECT) { launchSingleTop = true }
                    }
                )
            }

            // ── Profile ──
            composable(Routes.PROFILE) {
                ProfileRoute(
                    viewModel = profileViewModel,
                    onBackClick = { navController.popBackStack() },
                    onCerrarSesionClick = { authViewModel.cerrarSesion() }
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
                        subjectViewModel.resetCreateForm()
                        taskViewModel.resetCreateForm()
                        taskViewModel.cargarMaterias()
                        navController.navigate(Routes.CREATE_TASK) {
                            popUpTo(Routes.SUBJECT_SUCCESS) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onFinishClick = {
                        subjectViewModel.resetCreateForm()
                        navController.navigateClearingStack(Routes.HOME)
                    }
                )
            }

            composable(Routes.SUBJECT_LIST) {
                val listUiState by subjectViewModel.listUiState.collectAsState()
                LaunchedEffect(Unit) { subjectViewModel.cargarMaterias() }
                SubjectListScreen(
                    uiState = listUiState,
                    onDeleteClick = subjectViewModel::eliminarMateria,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // ── Tasks ──
            composable(Routes.TASK_LIST) {
                val listUiState by taskViewModel.listUiState.collectAsState()
                LaunchedEffect(Unit) { taskViewModel.cargarTareas() }
                TaskListScreen(
                    uiState = listUiState,
                    onEstadoChange = taskViewModel::cambiarEstado,
                    onDeleteClick = taskViewModel::eliminarTarea,
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Routes.CREATE_TASK) {
                val createUiState by taskViewModel.createUiState.collectAsState()
                LaunchedEffect(Unit) { taskViewModel.cargarMaterias() }
                LaunchedEffect(createUiState.tareaCreada) {
                    if (createUiState.tareaCreada) {
                        navController.navigate(Routes.TASK_LIST) {
                            popUpTo(Routes.CREATE_TASK) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
                CreateTaskScreen(
                    uiState = createUiState,
                    onTituloChange = taskViewModel::onTituloChange,
                    onDescripcionChange = taskViewModel::onDescripcionChange,
                    onFechaEntregaChange = taskViewModel::onFechaEntregaChange,
                    onPrioridadChange = taskViewModel::onPrioridadChange,
                    onEstadoChange = taskViewModel::onEstadoChange,
                    onSubjectChange = taskViewModel::onSubjectChange,
                    onCrearClick = taskViewModel::crearTarea,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}