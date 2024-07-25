package com.example.healthconnectdem02.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.healthconnectdem02.view.Home
import com.example.healthconnectdem02.viewmodel.HomeViewModel

@Composable
fun AppNavGraph(modifier: Modifier = Modifier) {

    val  navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screens.Home ){
        composable<Screens.Home> {
            val homeViewModel= hiltViewModel<HomeViewModel>()
            Home(
                homeViewModel = homeViewModel
            )
        }
    }
}