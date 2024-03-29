package com.frankuzi.frankuzistore.applications.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.frankuzi.frankuzistore.applications.presentation.ApplicationState
import com.frankuzi.frankuzistore.R

@Composable
fun ApplicationIcon(applicationName: String, imagePath: String, applicationState: ApplicationState, onIconClick: () -> Unit, onDownloadButtonClick: () -> Unit, onPlayButtonClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                onIconClick.invoke()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.baseline_image_24),
                error = painterResource(id = R.drawable.baseline_error_24),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .height(100.dp)
                    .width(100.dp),
                colorFilter = when (applicationState) {
                    is ApplicationState.Downloading -> {
                        ColorFilter.tint(color = Color.Gray, BlendMode.Multiply)
                    }
                    else -> {
                        null
                    }
                }
            )

            when (applicationState) {
                is ApplicationState.Downloading -> {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = applicationState.progress / 100f,
                            modifier = Modifier
                                .size(70.dp),
                            strokeWidth = 6.dp,
                            color = Color.Gray
                        )
                        Text(
                            text = applicationState.progress.toString(),
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
                else -> {}
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = applicationName,
                fontSize = 14.sp,
                maxLines = 2,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = {
                when (applicationState) {
                    is ApplicationState.NotDownloaded -> {
                        onDownloadButtonClick.invoke()
                    }
                    is ApplicationState.Downloaded -> {
                        onDownloadButtonClick.invoke()
                    }
                    is ApplicationState.Installed -> {
                        onPlayButtonClick.invoke()
                    }
                    else -> {}
                }
            },
            shape = RoundedCornerShape(20.dp),
            enabled = when (applicationState) {
                is ApplicationState.Downloading -> {
                    false
                }
                else -> {
                    true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = when (applicationState) {
                    is ApplicationState.Installed -> {
                        stringResource(id = R.string.play)
                    }
                    is ApplicationState.Downloaded -> {
                        stringResource(id = R.string.download)
                    }
                    is ApplicationState.Downloading -> {
                        stringResource(id = R.string.loading)
                    }
                    is ApplicationState.NotDownloaded -> {
                        stringResource(id = R.string.download)
                    }
                },
                maxLines = 1,
                fontSize = 10.sp,
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}