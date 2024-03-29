package com.frankuzi.frankuzistore

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
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
import coil.compose.AsyncImage
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import com.frankuzi.frankuzistore.applications.domain.utils.Downloader
import com.frankuzi.frankuzistore.applications.domain.utils.InstalledApplicationsChecker
import com.frankuzi.frankuzistore.applications.presentation.AboutMeViewModel
import com.frankuzi.frankuzistore.applications.presentation.ApplicationState
import com.frankuzi.frankuzistore.applications.presentation.Screen
import com.frankuzi.frankuzistore.applications.presentation.StoreViewModel
import com.frankuzi.frankuzistore.applications.presentation.components.AboutMeScreen
import com.frankuzi.frankuzistore.applications.presentation.components.ApplicationsListScreen
import com.frankuzi.frankuzistore.ui.theme.FrankuziStoreTheme
import com.frankuzi.frankuzistore.ui.theme.White
import com.frankuzi.frankuzistore.ui.theme.defaultBackground
import com.frankuzi.frankuzistore.utils.LifecycleEventListener
import com.frankuzi.frankuzistore.utils.myLog
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
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

@RequiresApi(Build.VERSION_CODES.O)
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
        mutableFloatStateOf(if (sheetState.isExpanded) 20 * sheetState.progress else 20 - 20 * sheetState.progress)
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
            title = stringResource(id = R.string.permission_required),
            description = stringResource(id = R.string.normal_operation_application),
            onConfirmClick = {
                context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES))
                dialogVisibility = false
            },
            confirmText = stringResource(id = R.string.yes),
            onDismissClick = {
                dialogVisibility = false
            },
            dismissText = stringResource(id = R.string.no),
            onDismissRequest = {
                dialogVisibility = false
            }
        )

    val state by storeViewModel.applicationsInfo.collectAsStateWithLifecycle()
    val aboutMeRequestState by aboutMeViewModel.aboutMeInfo.collectAsStateWithLifecycle()

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                BottomSheetContent(
                    selectedApplicationIndex = storeViewModel.selectedApplication.value,
                    getApplicationState = state,
                    onDownloadClick = { applicationInfo ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!context.packageManager.canRequestPackageInstalls()) {
                                dialogVisibility = true
                            } else {
                                storeViewModel.downloadApplication(applicationInfo)
                            }
                        }
                    },
                    onCancelClick = { applicationInfo ->
                        storeViewModel.cancelDownload(applicationInfo)
                    },
                    onPlayClick = { applicationInfo ->
                        val intent = context.packageManager.getLaunchIntentForPackage(applicationInfo.packageName)
                        context.startActivity(intent)
                    },
                    onDeleteClick = { applicationInfo ->
                        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                        intent.data = Uri.parse("package:${applicationInfo.packageName}")
                        context.startActivity(intent)
                    }
                )
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
                    backgroundColor = MaterialTheme.colors.background,
                )
            },
            bottomBar = {
                BottomNavigation(
                    elevation = 4.dp,
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onSecondary
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
                composable(
                    route = Screen.ApplicationsList.route,
                    enterTransition = {
                        when (initialState.destination.route) {
                            Screen.MyInfo.route -> {
                                slideInHorizontally(initialOffsetX = { -1500 }, animationSpec = tween(400))
                            }

                            else -> null
                        }
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            Screen.MyInfo.route -> {
                                slideOutHorizontally(targetOffsetX = { -1500 }, animationSpec = tween(400))
                            }

                            else -> null
                        }
                    }
                ) {
                    appBarTitle = stringResource(id = Screen.ApplicationsList.resourceId)
                    ApplicationsListScreen(
                        getApplicationState = state,
                        onRefreshListener = {
                            storeViewModel.updateApplicationsInfo()
                        },
                        onIconClick = { applicationInfoIndex ->
                            storeViewModel.selectedApplication.value = applicationInfoIndex
                            coroutineScope.launch {
                                sheetState.expand()
                            }
                        },
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
                        }
                    )
                }
                composable(
                    route = Screen.MyInfo.route,
                    enterTransition = {
                        when (initialState.destination.route) {
                            Screen.ApplicationsList.route -> {
                                slideInHorizontally(initialOffsetX = { 1500 }, animationSpec = tween(400))
                            }

                            else -> null
                        }
                    },
                    exitTransition = {
                        when (targetState.destination.route) {
                            Screen.ApplicationsList.route -> {
                                slideOutHorizontally(targetOffsetX = { 1500 }, animationSpec = tween(400))
                            }

                            else -> null
                        }
                    }
                ) {
                    appBarTitle = stringResource(id = Screen.MyInfo.resourceId)
                    AboutMeScreen(
                        aboutMeRequestState = aboutMeRequestState,
                        onTryAgain = aboutMeViewModel::updateInfo
                    )
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
    confirmText: String,
    onDismissClick: () -> Unit,
    dismissText: String,
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
                Text(text = confirmText)
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
                Text(text = dismissText)
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

@Composable
fun BottomSheetContent(
    selectedApplicationIndex: Int,
    getApplicationState: ApplicationsRequestState,
    onDownloadClick: (ApplicationInfo) -> Unit,
    onPlayClick: (ApplicationInfo) -> Unit,
    onCancelClick: (ApplicationInfo) -> Unit,
    onDeleteClick: (ApplicationInfo) -> Unit
) {
    val context = LocalContext.current
    val language = Locale.current.language

    when(getApplicationState) {
        is ApplicationsRequestState.Success -> {
            val state = getApplicationState.applications.collectAsStateWithLifecycle()
            val applicationInfo = state.value[selectedApplicationIndex]
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {

                            val strokeWidth = 1f * density
                            val y = size.height - strokeWidth / 2

                            drawLine(
                                Color.LightGray,
                                Offset(0f, y),
                                Offset(size.width, y),
                                strokeWidth
                            )
                        }
                        .padding(20.dp)
                ) {
                    AsyncImage(
                        model = applicationInfo.imageUrl,
                        contentDescription = null,
                        placeholder = painterResource(id = R.drawable.baseline_image_24),
                        error = painterResource(id = R.drawable.baseline_error_24),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .height(100.dp)
                            .width(100.dp)
                    )
                    Spacer(modifier = Modifier.width(20.dp))
                    Column(
                        modifier = Modifier
                            .padding(top = 5.dp)
                    ) {
                        Text(
                            text =
                            if (language == "ru")
                                applicationInfo.ruApplicationName
                            else
                                applicationInfo.applicationName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                        when(val applicationState = applicationInfo.applicationState) {
                            is ApplicationState.NotDownloaded -> {
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    onClick = {
                                        onDownloadClick.invoke(applicationInfo)
                                    }
                                ) {
                                    Text(text = stringResource(id = R.string.download))
                                }
                            }
                            is ApplicationState.Downloading -> {
                                Row {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {

                                        LinearProgressIndicator(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(40.dp)
                                                .clip(RoundedCornerShape(20.dp)),
                                            progress = applicationState.progress / 100f,
                                            color = Color.Green
                                        )
                                        Text(
                                            text = "${applicationState.progress}%",
                                            textAlign = TextAlign.Center,
                                            color = Color.Black
                                        )
                                        IconButton(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .padding(end = 10.dp)
                                                .align(Alignment.CenterEnd),
                                            onClick = {
                                                onCancelClick.invoke(applicationInfo)
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.baseline_close_24),
                                                contentDescription = "Cancel download",
                                                tint = Color.Black
                                            )
                                        }
                                    }
                                }
                            }
                            is ApplicationState.Installed -> {
                                Row {
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .weight(1f),
                                        shape = RoundedCornerShape(20.dp),
                                        onClick = {
                                            onPlayClick.invoke(applicationInfo)
                                        }
                                    ) {
                                        Text(text = stringResource(id = R.string.play))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .weight(1f),
                                        shape = RoundedCornerShape(20.dp),
                                        onClick = {
                                            onDeleteClick.invoke(applicationInfo)
                                        },
                                    ) {
                                        Text(text = stringResource(id = R.string.delete))
                                    }
                                }
                            }
                            is ApplicationState.Downloaded -> {
                                Button(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    onClick = {
                                        onDownloadClick.invoke(applicationInfo)
                                    }
                                ) {
                                    Text(text = stringResource(id = R.string.download))
                                }
                            }
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    item {
                        Text(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, top = 10.dp),
                            text =
                            if (language == "ru")
                                applicationInfo.ruDescription
                            else
                                applicationInfo.description
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        TextButton(
                            modifier = Modifier
                                .padding(start = 20.dp, end = 20.dp, bottom = 10.dp),
                            onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(applicationInfo.githubUrl))
                            context.startActivity(intent)
                        }) {
                            Text(text = stringResource(id = R.string.open_github))
                        }
                    }
                }
            }
        }
        else -> {}
    }
}