package com.example.healthconnectdem02.viewmodel

import android.util.Log
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.AggregateGroupByDurationRequest
import androidx.health.connect.client.request.AggregateGroupByPeriodRequest
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.response.InsertRecordsResponse
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Length
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthconnectdem02.data.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: Repository
):ViewModel() {

    private val _stepsData= MutableStateFlow("")
    val stepsData = _stepsData.asStateFlow()

    val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
    val latestStartOfSession = ZonedDateTime.now().minusMinutes(30)
    val offset = Random.nextDouble()
    val startOfSession = startOfDay.plusSeconds(
        (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
    )
    val endOfSession = startOfSession.plusMinutes(30)

    fun getMonthlySteps() {
        val endTime = LocalDateTime.now()
        val startTime = endTime.minusMonths(1)
        viewModelScope.launch {
            try {
                val response =
                    repository.healthConnectClient.aggregateGroupByPeriod(
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
                    Log.i("Total month steps", totalSteps.toString())
                    updateStepsData(data = totalSteps.toString()+" monthly steps")


                }
            } catch (e: Exception) {
                Log.i("Total steps", e.toString())
                // Run error handling here
            }
        }

    }

    private fun updateStepsData(data:String){
        _stepsData.value = data
    }
    fun getTodaySteps() {
      /*  var steps =0
        val endTime = LocalDateTime.now()
        val startTime = LocalDate.now().atStartOfDay()
       viewModelScope.launch {
           try {
               val response = repository.healthConnectClient.readRecords(
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
               updateStepsData(data = "$steps today steps")
           } catch (e: Exception) {
               Log.i("exception",e.toString())
               // Run error handling here
           }
       }*/

        viewModelScope.launch {
            val stepsRecords = readStepsForToday(repository.healthConnectClient)
            displaySteps(stepsRecords)

        }
    }
    fun getHeartRate(

    ) {
        val endTime = LocalDateTime.now()
        val startTime = LocalDate.now().atStartOfDay()
        viewModelScope.launch {
            try {
                val response =
                    repository.healthConnectClient.aggregate(
                        AggregateRequest(
                            setOf(HeartRateRecord.BPM_MAX, HeartRateRecord.BPM_MIN),
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                // The result may be null if no data is available in the time range
                val minimumHeartRate = response[HeartRateRecord.BPM_MIN]
                val maximumHeartRate = response[HeartRateRecord.BPM_MAX]

                updateStepsData( data = minimumHeartRate.toString()+" heart min rate")
            } catch (e: Exception) {
                Log.i("Exception",e.toString())
                // Run error handling here
            }
        }
    }
    fun getStepsMinutes(

    ) {
        var totalSteps:Long = 0L
        val endTime = LocalDateTime.now()
        val startTime = LocalDate.now().atStartOfDay()
        viewModelScope.launch {
            try {
                val response =
                    repository.healthConnectClient.aggregateGroupByDuration(
                        AggregateGroupByDurationRequest(
                            metrics = setOf(StepsRecord.COUNT_TOTAL),
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime),
                            timeRangeSlicer = Duration.ofMinutes(1L)
                        )
                    )
                for (durationResult in response) {
                    // The result may be null if no data is available in the time range
                    totalSteps = durationResult.result[StepsRecord.COUNT_TOTAL]?:0

                }
                updateStepsData(data = totalSteps.minutes.toString()+" move min")
            } catch (e: Exception) {
                Log.i("Exception",e.toString())
                // Run error handling here
            }
        }
    }

    fun insertSteps(){
        viewModelScope.launch {
            try {
                val response =writeExerciseSession(
                    start = startOfSession,
                    end = endOfSession,
                    healthConnectClient = repository.healthConnectClient
                )

                Log.i("Response",response.recordIdsList.toString())
            }catch (e:Exception){
                Log.i("Exception",e.toString())
            }

        }
    }

    suspend fun writeExerciseSession(
        start: ZonedDateTime,
        end: ZonedDateTime,
        healthConnectClient: HealthConnectClient,
    ): InsertRecordsResponse {
        return healthConnectClient.insertRecords(
            listOf(

                StepsRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    count = 122
                ),
                DistanceRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    distance = Length.meters((1000 + 100 * Random.nextInt(20)).toDouble())
                ),
                TotalCaloriesBurnedRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    energy = Energy.calories((140 + Random.nextInt(20)) * 0.01)
                )
            )
        )
    }

    private suspend fun getStepsCount(startTime: Instant, endTime: Instant): Long {
        val readRequest = ReadRecordsRequest(
            recordType = StepsRecord::class,
            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
        )

        val records = repository.healthConnectClient.readRecords(readRequest)
        return records.records.sumOf { it.count }
    }
    suspend fun readStepsForToday(healthConnectClient: HealthConnectClient): List<StepsRecord> {
        val today = LocalDate.now()
        val startOfDay = today.atStartOfDay().toInstant(ZoneOffset.UTC)
        val endOfDay = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)

        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startOfDay, endOfDay)
            )
        )


        return response.records
    }
    private fun displaySteps(stepsRecords: List<StepsRecord>) {
        var totalSteps = 0L
        for (record in stepsRecords) {
            totalSteps += record.count
        }
        updateStepsData(data = "$totalSteps today steps")
        // Now, you can use totalSteps to update your UI
        Log.d("HealthConnect", "Total steps for today: $totalSteps")
    }










}