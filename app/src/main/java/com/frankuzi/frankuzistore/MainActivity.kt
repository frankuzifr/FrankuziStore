package com.frankuzi.frankuzistore

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.frankuzi.frankuzistore.applications.domain.utils.DownloadHandlerImpl
import com.frankuzi.frankuzistore.applications.domain.utils.Downloader
import com.frankuzi.frankuzistore.applications.presentation.StoreViewModel
import com.frankuzi.frankuzistore.applications.presentation.components.ApplicationsListScreen
import com.frankuzi.frankuzistore.ui.theme.FrankuziStoreTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.concurrent.thread

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private lateinit var _downloadHandler: DownloadHandlerImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        val downloadHandler = DownloadHandlerImpl(this)
        _downloadHandler = downloadHandler
        App.downloader = Downloader(this)
        super.onCreate(savedInstanceState)

        val isInstalled = mutableStateOf(false)

        thread {
            while (true) {
                Thread.sleep(2000)
                isInstalled.value = applicationIsInstalled()
            }
        }

        setContent {
            FrankuziStoreTheme {

                val storeViewModel = hiltViewModel<StoreViewModel>()

                storeViewModel.getApplications()
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ApplicationsListScreen(viewModel = storeViewModel)
//                    Greeting( {
//                        startApplication()
//                    }, isInstalled.value, _downloadHandler.isDownloading.value, _downloadHandler.progress.value)
                }
            }
        }
    }

    private fun applicationIsInstalled(): Boolean {
        val installedApplications =
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (application in installedApplications) {
            if (application.packageName != "com.frankuzi.obshchalka")
                continue

            return true
        }

        return false
    }
}

@Composable
fun Greeting(playClick: () -> Unit, isInstalled: Boolean, isDownloading: Boolean, progress: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Obshchalka",
        )
        Spacer(modifier = Modifier.height(5.dp))

        if (!isDownloading) {
            Button(onClick = { playClick.invoke() }) {
                Text(text = if (isInstalled) "Play" else "Install")
            }
        }

        if (isDownloading) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(progress = progress / 100f)
                Text(text = progress.toString())
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FrankuziStoreTheme {
        Greeting({}, true,true, 30)
    }
}