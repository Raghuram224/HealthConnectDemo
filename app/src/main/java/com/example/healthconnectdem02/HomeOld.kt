package com.example.healthconnectdem02

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import kotlin.time.Duration.Companion.minutes

@Composable
fun HomeOld(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val availabilityStatus = HealthConnectClient.getSdkStatus(context, providerPackageName)
    if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
        return // early return as there is no viable integration
    }
    if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
        // Optionally redirect to package installer to find a provider, for example:
        val uriString =
            "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
        context.startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setPackage("com.android.vending")
                data = Uri.parse(uriString)
                putExtra("overlay", true)
                putExtra("callerId", context.packageName)
            }
        )
        return
    }
    val healthConnectClient = HealthConnectClient.getOrCreate(context)


    LaunchedEffect(key1 = Unit) {


//        aggregateStepsIntoMonths(
//            healthConnectClient = healthConnectClient
//        )
        /*aggregateTodaySteps(
            healthConnectClient = healthConnectClient
        )*/
//        insertSteps(healthConnectClient = healthConnectClient)
        getTodaySteps(healthConnectClient = healthConnectClient)
    }



    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Hello world")

    }


// Issue operations with healthConnectClient
}

suspend fun aggregateStepsIntoMonths(
    healthConnectClient: HealthConnectClient,
) {
    val endTime = LocalDateTime.now()
    val startTime = endTime.minusMonths(1)

    try {
        val response =
            healthConnectClient.aggregateGroupByPeriod(
                AggregateGroupByPeriodRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Period.ofMonths(1)
                )
            )
        for (monthlyResult in response) {
            Log.i("Total result", monthlyResult.result.toString())
            // The result may be null if no data is available in the time range
            val totalSteps = monthlyResult.result[StepsRecord.COUNT_TOTAL]
            Log.i("Total steps", totalSteps.toString())
        }
    } catch (e: Exception) {
        Log.i("Total steps", e.toString())
        // Run error handling here
    }
}

suspend fun aggregateStepsIntoMinutes(
    healthConnectClient: HealthConnectClient,
    startTime: LocalDateTime,
    endTime: LocalDateTime
) {
    try {
        val response =
            healthConnectClient.aggregateGroupByDuration(
                AggregateGroupByDurationRequest(
                    metrics = setOf(StepsRecord.COUNT_TOTAL),
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                    timeRangeSlicer = Duration.ofMinutes(10L)
                )
            )
        for (durationResult in response) {
            // The result may be null if no data is available in the time range
            val totalSteps = durationResult.result[StepsRecord.COUNT_TOTAL]
            Log.i("total steps", totalSteps?.minutes?.inWholeMinutes?.minutes.toString())
        }
    } catch (e: Exception) {
        // Run error handling here
    }
}

/*

suspend fun aggregateTodaySteps(
    healthConnectClient: HealthConnectClient,
) {
    val endTime = LocalDateTime.now()
    val startTime = endTime.minusDays(1)

    try {
        val response = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        // The result may be null if no data is available in the time range
        val stepCount = response[StepsRecord.COUNT_TOTAL]
        Log.i("Steps", stepCount.toString())
    } catch (e: Exception) {
        // Run error handling here
        Log.i("Exception", e.toString())
    }

}

*/
suspend fun aggregateTodaySteps(
    healthConnectClient: HealthConnectClient
) {
    val today = LocalDateTime.now()
    val endTime = today.toLocalDate().atStartOfDay()
    // Calculate the start of the day
    val startTime = today.minusDays(2)


    try {
        val response: AggregationResult = healthConnectClient.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )

        val stepCount = response?.get(StepsRecord.COUNT_TOTAL)
        Log.i("Steps", "Today's steps: $stepCount")
    } catch (e: Exception) {
        Log.e("Exception", "Error aggregating steps", e)
    }
}
suspend fun insertSteps(healthConnectClient: HealthConnectClient) {
//    val today = LocalTime.now().minute
    val today = LocalDateTime.now().toInstant(ZoneOffset.UTC)
    val startTime = today
    val endTime =startTime.minusSeconds(100)
    val START_ZONE_OFFSET = ZoneOffset.ofHours(5) // Replace 5 with your desired offset in hours
    val END_ZONE_OFFSET = ZoneOffset.ofHours(5) // Replace 5 with your desired offset in hours

    try {
        val stepsRecord = StepsRecord(
            count = 222,
            startTime = startTime,
            endTime = endTime,
            startZoneOffset = START_ZONE_OFFSET,
            endZoneOffset = END_ZONE_OFFSET,
        )
        healthConnectClient.insertRecords(listOf(stepsRecord))
    } catch (e: Exception) {
        Log.i("Exception",e.toString())
        // Run error handling here
    }
}
suspend fun getTodaySteps(
    healthConnectClient: HealthConnectClient,

) {
    var steps =0
    val endTime = LocalDateTime.now()
    val startTime = LocalDate.now().atStartOfDay()
    try {
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        for (stepRecord in response.records) {
            steps+= stepRecord.count.toInt()
            Log.i("Steps record",stepRecord.count.toString())
            // Process each step record
        }
        Log.i("Total steps",steps.toString())
    } catch (e: Exception) {
        // Run error handling here
    }
}
