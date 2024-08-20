package com.example.healthconnectdem02.view

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.request.AggregateRequest
import com.example.healthconnectdem02.providerPackageName
import com.example.healthconnectdem02.viewmodel.HomeViewModel
import java.time.Instant
import java.util.concurrent.TimeUnit

@Composable
fun Home(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel,
) {
    val stepsCount by homeViewModel.stepsData.collectAsState()
    val context = LocalContext.current

    val availabilityStatus = HealthConnectClient.getSdkStatus(context, providerPackageName)
    if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
        Log.i("availability","un available")
         // early return as there is no viable integration
    }
    Log.i(" availability","Available")
    Log.i("availability",availabilityStatus.toString())
    if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
        // Optionally redirect to package installer to find a provider, for example:
        val uriString =
            "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", context.packageName)
            }
        )

    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) {innerPadding->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Hello world")
            Text(
                modifier = Modifier
                    .padding(8.dp),
                text = stepsCount,
                style = TextStyle(
                    fontSize = 22.sp,
                    color = Color.Red

                )
            )

            Button(
                modifier = Modifier
                    .padding(8.dp),
                onClick = { homeViewModel.getTodaySteps()}) {
                Text(text = "Get today steps")
            }
            Button(
                modifier = Modifier
                    .padding(8.dp),
                onClick = { homeViewModel.getMonthlySteps() }) {
                Text(text = "Get this month total steps")
            }
            Button(
                modifier = Modifier
                    .padding(8.dp),
                onClick = { homeViewModel.getHeartRate()}) {
                Text(text = "Get heart rate min")
            }
           Button(
               modifier = Modifier
                   .padding(8.dp),
               onClick = { homeViewModel.getStepsMinutes() }) {
               Text(text = "Get step minutes")
           }

            Button(
                modifier = Modifier
                    .padding(8.dp),
                onClick = {homeViewModel.insertSteps() }) {

                Text(text = "Insert steps")
            }

        }
    }
}

