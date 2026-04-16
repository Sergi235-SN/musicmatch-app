package com.musicmatch.mobile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.musicmatch.mobile.navigation.Screen
import com.musicmatch.mobile.viewmodel.MatchViewModel

@Composable
fun SwipeCardStack(
    viewModel: MatchViewModel,
    navController: NavController
) {

    val profile = viewModel.currentProfile

    if (profile == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            EmptyMatchesState(viewModel.message ?: "No hay más músicos")
        }
        return
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        ProfileMatchCard(
            profile = profile,
            viewModel = viewModel,
            onLike = {
                viewModel.swipe(liked = true)
            },
            onDislike = {
                viewModel.swipe(liked = false)
            },
            onOpenProfile = {
                navController.navigate(
                    Screen.PublicProfile.createRoute(profile.id)
                )
            }
        )
    }
}

@Composable
fun EmptyMatchesState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message)
    }
}