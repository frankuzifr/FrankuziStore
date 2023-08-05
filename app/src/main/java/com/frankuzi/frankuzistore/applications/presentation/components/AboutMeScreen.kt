package com.frankuzi.frankuzistore.applications.presentation.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.frankuzi.frankuzistore.R
import com.frankuzi.frankuzistore.applications.domain.model.AboutMeInfo
import com.frankuzi.frankuzistore.applications.domain.model.AboutMeRequestState

@Composable
fun AboutMeScreen(
    aboutMeRequestState: AboutMeRequestState,
    onTryAgain: () -> Unit
) {

    when (aboutMeRequestState) {
        is AboutMeRequestState.Success -> {
            AboutMeSuccessView(aboutMeInfo = aboutMeRequestState.aboutMeInfo)
        }
        is AboutMeRequestState.Failed -> {
            AboutMeFailedView(onTryAgain)
        }
        AboutMeRequestState.Loading -> {
            AboutMeLoadingView()
        }
    }
}

@Composable
fun AboutMeSuccessView(aboutMeInfo: AboutMeInfo) {

    val context = LocalContext.current
    val clipboard: ClipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(160.dp)
            ) {
                val borderWidth = 2.dp
                val tripleColorsBrush = remember {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFF0023FF),
                            Color(0xFFFF0000),
                        )
                    )
                }

                AsyncImage(
                    model = aboutMeInfo.imageUrl,
                    contentDescription = null,
                    placeholder = painterResource(id = R.drawable.baseline_image_24),
                    error = painterResource(id = R.drawable.baseline_error_24),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp)
                        .border(
                            BorderStroke(borderWidth, MaterialTheme.colors.onSecondary),
                            CircleShape
                        )
                        .padding(borderWidth)
                        .clip(CircleShape)
                        .align(Alignment.Center)
                        .clickable {
                            val intent = Intent().apply {
                                action = Intent.ACTION_VIEW
                                data = Uri.parse(aboutMeInfo.socialLink)
                            }
                            context.startActivity(intent)
                        }
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(end = 10.dp)
                ) {
                    TextButton(
                        onClick = {

                        },
                        enabled = false,
                        modifier = Modifier
                    ) {
                        Text(
                            text = aboutMeInfo.name,
                            fontSize = 18.sp,
                            color = MaterialTheme.colors.onSecondary
                        )
                    }
                    TextButton(
                        onClick = {
                            val clip = ClipData.newPlainText(aboutMeInfo.email, aboutMeInfo.email)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(
                                context,
                                context.resources.getText(R.string.email_was_copied_clipboard),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                    ) {
                        Text(
                            text = aboutMeInfo.email,
                            color = MaterialTheme.colors.onSecondary
                        )
                    }
                    TextButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(aboutMeInfo.githubLink))
                            context.startActivity(intent);
                        },
                        modifier = Modifier
                    ) {
                        Text(
                            text = stringResource(id = R.string.my_github),
                            color = MaterialTheme.colors.onSecondary
                        )
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .padding(start = 10.dp, end = 10.dp)
        ) {
            item {
                Text(
                    text = aboutMeInfo.description
                )
            }
        }
    }
}

@Composable
fun AboutMeLoadingView() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun AboutMeFailedView(
    tryAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(id = R.string.error))
        Spacer(modifier = Modifier.height(5.dp))
        Button(onClick = {
            tryAgain.invoke()
        }) {
            Text(text = stringResource(id = R.string.try_again))
        }
    }
}