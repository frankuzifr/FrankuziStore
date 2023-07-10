package com.frankuzi.frankuzistore

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.applications.domain.utils.Downloader
import com.frankuzi.frankuzistore.applications.domain.utils.InstalledApplicationsChecker
import com.frankuzi.frankuzistore.applications.presentation.AboutMeViewModel
import com.frankuzi.frankuzistore.applications.presentation.Screen
import com.frankuzi.frankuzistore.applications.presentation.StoreViewModel
import com.frankuzi.frankuzistore.applications.presentation.components.AboutMeScreen
import com.frankuzi.frankuzistore.applications.presentation.components.ApplicationsListScreen
import com.frankuzi.frankuzistore.ui.theme.FrankuziStoreTheme
import com.frankuzi.frankuzistore.ui.theme.White
import com.frankuzi.frankuzistore.ui.theme.defaultBackground
import com.frankuzi.frankuzistore.utils.LifecycleEventListener
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        App.downloader = Downloader(this)
        App.installedApplicationsChecker = InstalledApplicationsChecker(this)
        super.onCreate(savedInstanceState)

        setContent {
            FrankuziStoreTheme {

                val systemUiController = rememberSystemUiController()
                val isSystemInDarkTheme = isSystemInDarkTheme()

                DisposableEffect(systemUiController, isSystemInDarkTheme) {
                    if (isSystemInDarkTheme) {
                        systemUiController.setSystemBarsColor(
                            color = defaultBackground,
                            darkIcons = false
                        )
                    } else {
                        systemUiController.setSystemBarsColor(
                            color = White,
                            darkIcons = true
                        )
                    }

                    onDispose {}
                }

                val storeViewModel = hiltViewModel<StoreViewModel>()
                val aboutMeViewModel = hiltViewModel<AboutMeViewModel>()

                LifecycleEventListener {event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            storeViewModel.startActualizeApplications()
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            storeViewModel.stopActualizeApplications()
                        }
                        else -> {}
                    }
                }

                storeViewModel.updateApplicationsInfo()
                aboutMeViewModel.updateInfo()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Content(storeViewModel = storeViewModel, aboutMeViewModel = aboutMeViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Content(storeViewModel: StoreViewModel, aboutMeViewModel: AboutMeViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val navController = rememberNavController()
    val items = listOf(
        Screen.ApplicationsList,
        Screen.MyInfo,
    )
    var appBarTitle by remember {
        mutableStateOf("")
    }
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Collapsed
    )
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = sheetState
    )
    val bottomSheetRadius by remember {
        mutableStateOf(if (sheetState.isExpanded) 20 * sheetState.progress else 20 - 20 * sheetState.progress)
    }
    var selectedApplication: ApplicationInfo? by remember {
        mutableStateOf(null)
    }

    BackHandler {
        if (sheetState.isExpanded) {
            coroutineScope.launch {
                sheetState.collapse()
            }
            return@BackHandler
        }

        val activity = (context as? Activity)
        activity?.finish()
    }

    var dialogVisibility by remember {
        mutableStateOf(false)
    }

    val scaffoldState = rememberScaffoldState()

    if (dialogVisibility)
        Dialog(
            title = "Dialog 1",
            description = "Dialog 1",
            onConfirmClick = {
                context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
                dialogVisibility = false
            },
            onDismissClick = {
                dialogVisibility = false
            },
            onDismissRequest = {
                dialogVisibility = false
            }
        )

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                selectedApplication?.let {
                    Text(
                        text = it.applicationName,
                        fontSize = 60.sp
                    )
                }
            }
        },
        sheetPeekHeight = 0.dp,
        sheetShape = RoundedCornerShape(topStart = bottomSheetRadius, topEnd = bottomSheetRadius),
        sheetElevation = 0.dp,
    ) {

        Scaffold(
            scaffoldState = scaffoldState,
            modifier = Modifier
                .fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = appBarTitle)
                    },
                    elevation = 0.dp,
                    backgroundColor = defaultBackground,
                )
            },
            bottomBar = {
                BottomNavigation(
                    elevation = 4.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach{ screen ->
                        BottomNavigationItem(
                            icon = {
                                Icon(imageVector = screen.icon, contentDescription = null)
                            },
                            label = {
                                Text(text = stringResource(id = screen.resourceId))
                            },
                            selected = currentDestination?.hierarchy?.any {
                                it.route == screen.route
                            } == true,
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
                startDestination = Screen.ApplicationsList.route, Modifier.padding(innerPadding)
            ) {
                composable(Screen.ApplicationsList.route) {
                    appBarTitle = stringResource(id = Screen.ApplicationsList.resourceId)
                    val state by storeViewModel.applicationsInfo.collectAsStateWithLifecycle()
                    ApplicationsListScreen(
                        getApplicationState = state,
                        onRefreshListener = {
                            storeViewModel.updateApplicationsInfo()
                        },
                        onIconClick = { applicationInfo ->
                            selectedApplication = applicationInfo
                            coroutineScope.launch {
                                sheetState.expand()
                            }
                        },~
                        onDownloadButtonClick = { applicationInfo ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                if (!context.packageManager.canRequestPackageInstalls()) {
                                    dialogVisibility = true
                                } else {
                                    storeViewModel.downloadApplication(applicationInfo)
                                }
                            }
                        },
                        onPlayButtonClick = { applicationInfo ->
                            val intent = context.packageManager.getLaunchIntentForPackage(applicationInfo.packageName)
                            context.startActivity(intent)
                        },
                        sheetState = sheetState,
                        scaffoldState = bottomSheetScaffoldState
                    )
                }
                composable(Screen.MyInfo.route) {
                    appBarTitle = stringResource(id = Screen.MyInfo.resourceId)
                    AboutMeScreen(viewModel = aboutMeViewModel)
                }
            }
        }
    }
}

@Composable
fun Dialog(
    title: String,
    description: String,
    onConfirmClick: () -> Unit,
    onDismissClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        shape = RoundedCornerShape(8.dp),
        onDismissRequest = {
            onDismissRequest.invoke()
        },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                ),
                onClick = {
                    onConfirmClick.invoke()
                },
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Text(text = "Yes")
            }
        },
        dismissButton = {
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.surface
                ),
                onClick = {
                    onDismissClick.invoke()
                },
                elevation = ButtonDefaults.elevation(0.dp)
            ) {
                Text(text = "No")
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = description)
        }
    )
}