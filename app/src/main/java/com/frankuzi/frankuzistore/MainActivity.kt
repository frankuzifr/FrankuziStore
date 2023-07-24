package com.frankuzi.frankuzistore

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
        mutableStateOf(if (sheetState.isExpanded) 20 * sheetState.progress else 20 - 20 * sheetState.progress)
    }
    var selectedApplication: ApplicationInfo? by remember {
        mutableStateOf(null)
    }
    var selectedApplicationIndex by remember {
        mutableStateOf(0)
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

    val state by storeViewModel.applicationsInfo.collectAsStateWithLifecycle()
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                BottomSheetContent(
                    selectedApplicationIndex = selectedApplicationIndex,
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
                    onCancelClick = {

                    },
                    onPlayClick = { applicationInfo ->
                        val intent = context.packageManager.getLaunchIntentForPackage(applicationInfo.packageName)
                        context.startActivity(intent)
                    },
                    onDeleteClick = {

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
                    ApplicationsListScreen(
                        getApplicationState = state,
                        onRefreshListener = {
                            storeViewModel.updateApplicationsInfo()
                        },
                        onIconClick = { applicationInfoIndex ->
                            selectedApplicationIndex = applicationInfoIndex
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

@Composable
fun BottomSheetContent(
    selectedApplicationIndex: Int,
    getApplicationState: ApplicationsRequestState,
    onDownloadClick: (ApplicationInfo) -> Unit,
    onPlayClick: (ApplicationInfo) -> Unit,
    onCancelClick: (ApplicationInfo) -> Unit,
    onDeleteClick: (ApplicationInfo) -> Unit
) {
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
                            text = applicationInfo.applicationName,
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
                                    Text(text = "Download")
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
                                            textAlign = TextAlign.Center
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
                                                contentDescription = "Cancel download"
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
                                        Text(text = "Play")
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
                                        Text(text = "Delete")
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
                                    Text(text = "Download")
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
                                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 10.dp),
                            text = "sdasmkmfam kamfkmda.,fnmadn f,andmf, na,dnfma ndjfna dnf ,amdnf, namdfn andfnnb a,dnfb n,adf\" +\n" +
                                    "                        \"adfman,dfmna,mdfna,mndf,mandf,mnam,dfnam,ndfmnamdnfman mdnfam,n f,\" +\n" +
                                    "                        \"a dmnfa,m ndf,mandfm,namdn fmandf,mn a,dmfn a,mnf,amndfn am,fdn \" +\n" +
                                    "                        \"a.dfna., dnfm.an dm.fn amndf,n adnf ,andf. manf \" +\n" +
                                    "                        \"ad.mfn a.mdnf .amndf. na.fn a.fn \" +\n" +
                                    "                        \"adf na.,mdfn .amdnf sdasmkmfam kamfkmda.,fnmadn f,andmf, na,dnfma ndjfna dnf ,amdnf, namdfn andfnnb a,dnfb n,adf\" +\n" +
                                    "                        \"adfman,dfmna,mdfna,mndf,mandf,mnam,dfnam,ndfmnamdnfman mdnfam,n f,\" +\n" +
                                    "                        \"a dmnfa,m ndf,mandfm,namdn fmandf,mn a,dmfn a,mnf,amndfn am,fdn \" +\n" +
                                    "                        \"a.dfna., dnfm.an dm.fn amndf,n adnf ,andf. manf \" +\n" +
                                    "                        \"ad.mfn a.mdnf .amndf. na.fn a.fn \" +\n" +
                                    "                        \"adf na.,mdfn .amdnf sdasmkmfam kamfkmda.,fnmadn f,andmf, na,dnfma ndjfna dnf ,amdnf, namdfn andfnnb a,dnfb n,adf\" +\n" +
                                    "                        \"adfman,dfmna,mdfna,mndf,mandf,mnam,dfnam,ndfmnamdnfman mdnfam,n f,\" +\n" +
                                    "                        \"a dmnfa,m ndf,mandfm,namdn fmandf,mn a,dmfn a,mnf,amndfn am,fdn \" +\n" +
                                    "                        \"a.dfna., dnfm.an dm.fn amndf,n adnf ,andf. manf \" +\n" +
                                    "                        \"ad.mfn a.mdnf .amndf. na.fn a.fn \" +\n" +
                                    "                        \"adf na.,mdfn .amdnf sdasmkmfam kamfkmda.,fnmadn f,andmf, na,dnfma ndjfna dnf ,amdnf, namdfn andfnnb a,dnfb n,adf\" +\n" +
                                    "                        \"adfman,dfmna,mdfna,mndf,mandf,mnam,dfnam,ndfmnamdnfman mdnfam,n f,\" +\n" +
                                    "                        \"a dmnfa,m ndf,mandfm,namdn fmandf,mn a,dmfn a,mnf,amndfn am,fdn \" +\n" +
                                    "                        \"a.dfna., dnfm.an dm.fn amndf,n adnf ,andf. manf \" +\n" +
                                    "                        \"ad.mfn a.mdnf .amndf. na.fn a.fn \" +\n" +
                                    "                        \"adf na.,mdfn .amdnf sdasmkmfam kamfkmda.,fnmadn f,andmf, na,dnfma ndjfna dnf ,amdnf, namdfn andfnnb a,dnfb n,adf\" +\n" +
                                    "                        \"adfman,dfmna,mdfna,mndf,mandf,mnam,dfnam,ndfmnamdnfman mdnfam,n f,\" +\n" +
                                    "                        \"a dmnfa,m ndf,mandfm,namdn fmandf,mn a,dmfn a,mnf,amndfn am,fdn \" +\n" +
                                    "                        \"a.dfna., dnfm.an dm.fn amndf,n adnf ,andf. manf \" +\n" +
                                    "                        \"ad.mfn a.mdnf .amndf. na.fn a.fn \" +\n" +
                                    "                        \"adf na.,mdfn .amdnf sdasmkmfam kamfkmda.,fnmadn f,andmf, na,dnfma ndjfna dnf ,amdnf, namdfn andfnnb a,dnfb n,adf\" +\n" +
                                    "                        \"adfman,dfmna,mdfna,mndf,mandf,mnam,dfnam,ndfmnamdnfman mdnfam,n f,\" +\n" +
                                    "                        \"a dmnfa,m ndf,mandfm,namdn fmandf,mn a,dmfn a,mnf,amndfn am,fdn \" +\n" +
                                    "                        \"a.dfna., dnfm.an dm.fn amndf,n adnf ,andf. manf \" +\n" +
                                    "                        \"ad.mfn a.mdnf .amndf. na.fn a.fn \" +\n" +
                                    "                        \"adf na.,mdfn .amdnf sdasmkmfam kamfkmda.,fnmadn f,andmf, na,dnfma ndjfna dnf ,amdnf, namdfn andfnnb a,dnfb n,adf\" +\n" +
                                    "                        \"adfman,dfmna,mdfna,mndf,mandf,mnam,dfnam,ndfmnamdnfman mdnfam,n f,\" +\n" +
                                    "                        \"a dmnfa,m ndf,mandfm,namdn fmandf,mn a,dmfn a,mnf,amndfn am,fdn \" +\n" +
                                    "                        \"a.dfna., dnfm.an dm.fn amndf,n adnf ,andf. manf \" +\n" +
                                    "                        \"ad.mfn a.mdnf .amndf. na.fn a.fn \" +\n" +
                                    "                        \"adf na.,mdfn .amdnf ")
                    }
                }
            }
        }
        else -> {}
    }
}