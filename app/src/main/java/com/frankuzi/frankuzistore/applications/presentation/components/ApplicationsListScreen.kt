package com.frankuzi.frankuzistore.applications.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import com.frankuzi.frankuzistore.applications.presentation.StoreViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun ApplicationsListScreen(viewModel: StoreViewModel) {
    val getApplicationState = viewModel.applicationsInfo.collectAsStateWithLifecycle()
    var isLoading by remember {
        mutableStateOf(false)
    }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            viewModel.updateApplicationsInfo()
        },
        indicator = { state, refreshTrigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = refreshTrigger
            )
        }
    ) {
        when (val applicationsRequestState = getApplicationState.value) {
            is ApplicationsRequestState.Failed -> {
                isLoading = false
                ErrorView(applicationsRequestStateSuccess = applicationsRequestState)
            }
            ApplicationsRequestState.Loading -> {
                isLoading = true
                LoadingView()
            }
            is ApplicationsRequestState.Success -> {
                isLoading = false
                SuccessView(
                    applicationsRequestStateSuccess = applicationsRequestState,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun SuccessView(applicationsRequestStateSuccess: ApplicationsRequestState.Success, viewModel: StoreViewModel) {
    val applications = applicationsRequestStateSuccess.applications.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                },
                onPlayButtonClick = {
                    val intent = context.packageManager.getLaunchIntentForPackage(application.packageName)
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun ErrorView(applicationsRequestStateSuccess: ApplicationsRequestState.Failed) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${applicationsRequestStateSuccess.message}",
        )
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