package com.example.healthconnectdem02.navigation

import kotlinx.serialization.Serializable


@Serializable
sealed class Screens {

    @Serializable
    data object Home:Screens()
}