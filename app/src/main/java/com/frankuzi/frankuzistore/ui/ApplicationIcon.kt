package com.frankuzi.frankuzistore.ui

import android.widget.GridLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.frankuzi.frankuzistore.applications.ApplicationState
import com.frankuzi.frankuzistore.R

@Composable
fun ApplicationIcon(applicationName: String, imagePath: String, applicationState: ApplicationState) {
    Column(
        modifier = Modifier
            .width(200.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box {
            AsyncImage(
                model = imagePath,
                contentDescription = null,
                placeholder = painterResource(id = R.drawable.genshin),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Red)
                    .height(120.dp)
                    .width(120.dp),
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
        Text(
            text = applicationName,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Button(
            onClick = {
                /* Do something! */
            },
            shape = RoundedCornerShape(20.dp),
            enabled = when (applicationState) {
                is  ApplicationState.Downloading -> {
                    false
                }
                else -> {
                    true
                }
            },
            modifier = Modifier
                .width(120.dp)
        ) {
            Text(
                text = when (applicationState) {
                    is ApplicationState.Downloaded -> {
                        "Play"
                    }
                    is ApplicationState.Downloading -> {
                        "Downloading"
                    }
                    is ApplicationState.NotDownloaded -> {
                        "Download"
                    }
                },
                maxLines = 1,
                fontSize = 12.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ApplicationIconPreview() {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(10) {
            ApplicationIcon(applicationName = "Obshalka", imagePath = "", applicationState = ApplicationState.Downloading(69))
        }
    }
}