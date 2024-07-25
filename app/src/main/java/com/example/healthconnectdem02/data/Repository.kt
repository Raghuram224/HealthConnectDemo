package com.example.healthconnectdem02.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import javax.inject.Inject

class Repository @Inject constructor(context: Context) {
    val healthConnectClient = HealthConnectClient.getOrCreate(context)
}