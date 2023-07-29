package com.frankuzi.frankuzistore.applications.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationInfo
import com.frankuzi.frankuzistore.applications.domain.model.ApplicationsRequestState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun ApplicationsListScreen(
    getApplicationState: ApplicationsRequestState,
    onRefreshListener: () -> Unit,
    onIconClick: (Int) -> Unit,
    onDownloadButtonClick: (ApplicationInfo) -> Unit,
    onPlayButtonClick: (ApplicationInfo) -> Unit
) {
    var isLoading by remember {
        mutableStateOf(false)
    }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = isLoading)
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = {
            onRefreshListener.invoke()
        },
        indicator = { state, refreshTrigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = refreshTrigger
            )
        }
    ) {
        when (getApplicationState) {
            is ApplicationsRequestState.Failed -> {
                isLoading = false
                ErrorView(applicationsRequestStateSuccess = getApplicationState)
            }
            ApplicationsRequestState.Loading -> {
                isLoading = true
                LoadingView()
            }
            is ApplicationsRequestState.Success -> {
                isLoading = false
                SuccessView(
                    applicationsRequestStateSuccess = getApplicationState,
                    onIconClick = onIconClick,
                    onDownloadButtonClick = onDownloadButtonClick,
                    onPlayButtonClick = onPlayButtonClick
                )
            }
        }
    }
}

@Composable
fun SuccessView(
    applicationsRequestStateSuccess: ApplicationsRequestState.Success,
    onIconClick: (Int) -> Unit,
    onDownloadButtonClick: (ApplicationInfo) -> Unit,
    onPlayButtonClick: (ApplicationInfo) -> Unit
) {
    val applications = applicationsRequestStateSuccess.applications.collectAsStateWithLifecycle()
    val language = Locale.current.language

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalArrangement = Arrangement.spacedBy(15.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 15.dp, end = 15.dp, top = 15.dp)
    ){
        itemsIndexed(applications.value) { index, application ->
            ApplicationIcon(
                applicationName =
                if (language == "ru")
                    application.ruApplicationName
                else
                    application.applicationName,
                imagePath = application.imageUrl,
                applicationState = application.applicationState,
                onIconClick = {
                    onIconClick.invoke(index)
                },
                onDownloadButtonClick = {
                    onDownloadButtonClick.invoke(application)
                },
                onPlayButtonClick = {
                    onPlayButtonClick.invoke(application)
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