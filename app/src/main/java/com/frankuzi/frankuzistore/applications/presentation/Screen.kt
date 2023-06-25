package com.frankuzi.frankuzistore.applications.presentation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.frankuzi.frankuzistore.R

sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object ApplicationsList : Screen("applicationsList", R.string.applications_list, Icons.Default.Home)
    object MyInfo : Screen("myInfo", R.string.my_info, Icons.Default.AccountBox)
}