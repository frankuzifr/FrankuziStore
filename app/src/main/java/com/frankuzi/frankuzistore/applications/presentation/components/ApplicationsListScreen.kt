package com.frankuzi.frankuzistore.applications.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import com.frankuzi.frankuzistore.applications.presentation.StoreViewModel
import com.frankuzi.frankuzistore.utils.myLog

@Composable
fun ApplicationsListScreen(viewModel: StoreViewModel) {
    var getApplicationState = viewModel.getApplicationState.collectAsStateWithLifecycle()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = {
            Text(text = "My applications")
        })},
        bottomBar = {
            BottomBar()
        }
    ) {
        it

        myLog("Recompose")
        when (val applicationsRequestState = getApplicationState.value) {
            is ApplicationsRequestState.Error -> ErrorView(applicationsRequestStateSuccess = applicationsRequestState)
            ApplicationsRequestState.Loading -> LoadingView()
            is ApplicationsRequestState.Success -> SuccessView(
                applicationsRequestStateSuccess = applicationsRequestState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun SuccessView(applicationsRequestStateSuccess: ApplicationsRequestState.Success, viewModel: StoreViewModel) {
    val applications = applicationsRequestStateSuccess.applications.collectAsStateWithLifecycle()
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 15.dp, end = 15.dp, top = 15.dp)
    ){
        items(applications.value) { application ->
            ApplicationIcon(
                applicationName = application.applicationName,
                imagePath = application.imageUrl,
                applicationState = application.applicationState,
                onDownloadButtonClick = {
                    viewModel.downloadApplication(application)
                }
            )
        }
    }
}

@Composable
fun ErrorView(applicationsRequestStateSuccess: ApplicationsRequestState.Error) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${applicationsRequestStateSuccess.message}",
        )
//        Button(onClick = { viewModel.getApplications() }) {
//            Text(text = "Update")
//        }
    }
}

@Composable
fun LoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Loading",
        )
    }
}

@Composable
fun BottomBar() {
    val selectedIndex = remember {
        mutableStateOf(0)
    }
    BottomNavigation(elevation = 10.dp) {
        
        BottomNavigationItem(
            icon = {
                Icon(imageVector = Icons.Default.Home, "Applications")
            },
            label = {
                Text(text = "Applications")
            },
            selected = selectedIndex.value == 0,
            onClick = {
                selectedIndex.value = 0
            }
        )
        BottomNavigationItem(
            icon = {
                Icon(imageVector = Icons.Default.AccountBox, "Info")
            },
            label = {
                Text(text = "Info")
            },
            selected = selectedIndex.value == 1,
            onClick = {
                selectedIndex.value = 1
            }
        )
    }
}