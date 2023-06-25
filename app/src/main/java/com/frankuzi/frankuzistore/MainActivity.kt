package com.frankuzi.frankuzistore

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.frankuzi.frankuzistore.applications.domain.utils.Downloader
import com.frankuzi.frankuzistore.applications.domain.utils.InstalledApplicationsChecker
import com.frankuzi.frankuzistore.applications.presentation.Screen
import com.frankuzi.frankuzistore.applications.presentation.StoreViewModel
import com.frankuzi.frankuzistore.applications.presentation.components.ApplicationsListScreen
import com.frankuzi.frankuzistore.ui.theme.FrankuziStoreTheme
import com.frankuzi.frankuzistore.utils.LifecycleEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        App.downloader = Downloader(this)
        App.installedApplicationsChecker = InstalledApplicationsChecker(this)
        super.onCreate(savedInstanceState)

        setContent {
            FrankuziStoreTheme {

                val storeViewModel = hiltViewModel<StoreViewModel>()

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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

//                    ApplicationsListScreen(viewModel = storeViewModel, context = this)
                    Content(viewModel = storeViewModel)
                }
            }
        }
    }
}

@Composable
fun Content(viewModel: StoreViewModel) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.ApplicationsList,
        Screen.MyInfo,
    )
    var appBarTitle by remember {
        mutableStateOf("")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = {
            Text(text = appBarTitle)
        })},
        bottomBar = {
            BottomNavigation {
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

        NavHost(navController = navController, startDestination = Screen.ApplicationsList.route, Modifier.padding(innerPadding)) {
            composable(Screen.ApplicationsList.route) {
                appBarTitle = stringResource(id = Screen.ApplicationsList.resourceId)
                ApplicationsListScreen(viewModel = viewModel)
            }
            composable(Screen.MyInfo.route) {
                appBarTitle = stringResource(id = Screen.MyInfo.resourceId)
                Test()
            }
        }
    }
}

@Composable
fun Test() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Salut all")
    }
}