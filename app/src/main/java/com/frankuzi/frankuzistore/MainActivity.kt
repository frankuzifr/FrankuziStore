package com.frankuzi.frankuzistore

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
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
import com.frankuzi.frankuzistore.applications.domain.utils.DownloadHandler
import com.frankuzi.frankuzistore.ui.theme.FrankuziStoreTheme
import kotlin.concurrent.thread


class MainActivity : ComponentActivity() {
    private lateinit var _downloadHandler: DownloadHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        _downloadHandler = DownloadHandler(this)

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
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting( {
                        startApplication()
                    }, isInstalled.value, _downloadHandler.isDownloading.value, _downloadHandler.progress.value)
                }
            }
        }
    }

    private fun applicationIsInstalled(): Boolean {
        val installedApplications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (application in installedApplications) {
            if (application.packageName != "com.frankuzi.obshchalka")
                continue

            return true
        }

        return false
    }

    private fun startApplication() {
        val launchIntentForPackage = packageManager.getLaunchIntentForPackage("com.frankuzi.obshchalka")

        val applicationIsInstalled = applicationIsInstalled()

        if (applicationIsInstalled) {
            if (launchIntentForPackage != null)
                startActivity(launchIntentForPackage)

            return
        }

//        if (!hasStoragePermission())
//            return


        _downloadHandler.enqueueDownload("https://firebasestorage.googleapis.com/v0/b/frankuzi-store.appspot.com/o/obshchalka.apk?alt=media&token=16bd380c-7682-41fc-ab96-738ac0718cb2")
    }

    private fun hasStoragePermission(): Boolean {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            return if (Environment.isExternalStorageManager()) {
                true
            } else {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data = Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivity(intent)
                }

                false
            }

        } else {
            return when {
                checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                    true
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    false
                }
                else -> {
                    requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
                    false
                }
            }
        }
    }

    private fun checkChangedPackages(context: Context) {

        while (true) {
            Thread.sleep(2000)
            val packageManagerApps = context.packageManager
            val sequenceNumber = getSequenceNumber(context)
            Log.d("LOOOOG", "sequenceNumber = $sequenceNumber")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                val changedPackages = packageManagerApps.getChangedPackages(sequenceNumber)

                if (changedPackages != null) {
                    // Packages are changed

                    // Get the list of changed packages
                    // the list includes new, updated and deleted apps
                    val changedPackagesNames = changedPackages.packageNames

                    var appName: CharSequence

                    for (packageName in changedPackagesNames) {
                        try {
                            appName = packageManagerApps.getApplicationLabel(
                                packageManagerApps.getApplicationInfo(
                                    packageName, 0,
                                )
                            )

                            // Either a new or an updated app
                            Log.d(
                                "LOOOOG",
                                "New Or Updated App: $packageName , appName = ${appName.toString()}"
                            )
                        } catch (e: PackageManager.NameNotFoundException) {
                            // The app is deleted
                            Log.d("LOOOOG", "Deleted App: $packageName")
                        }
                    }
                    saveSequenceNumber(context, changedPackages.sequenceNumber)
                } else {
                    // packages not changed
                }
            }
        }
    }

    private fun getSequenceNumber(context: Context): Int {
        val sharedPrefFile = context.getSharedPreferences("your_file_name", MODE_PRIVATE)
        return sharedPrefFile.getInt("sequence_number", 0)
    }

    private fun saveSequenceNumber(context: Context, newSequenceNumber: Int) {
        val sharedPrefFile = context.getSharedPreferences("your_file_name", MODE_PRIVATE)
        val editor = sharedPrefFile.edit()
        editor.putInt("sequence_number", newSequenceNumber)
        editor.apply()
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