package com.example.composemvvm.ui.main.viewmodel

import android.annotation.SuppressLint
import android.location.Location
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composemvvm.domain.IMapUseCase
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltViewModel
class MainViewModel @Inject constructor(
    private val fusedLocationProviderClient: FusedLocationProviderClient,
    private val coordinatesUseCase: IMapUseCase
) : ViewModel() {

    companion object {
        const val MAIN_ADDRESS = "Екатеренбург, ул. Восточная, 7Г"
    }

    val screenState = MutableStateFlow(MainState())

    private fun createRoute() {
        resetMapMode()
        viewModelScope.launch {
            runCatching {
                val currentLocation =
                    getLatestLocation(fusedLocationProviderClient) ?: return@launch
                val destinationLocation = coordinatesUseCase.getCoordinates(MAIN_ADDRESS)

                screenState.update {
                    it.copy(
                        mapMode = MapMode.CreateRoute(
                            start = Point(currentLocation.latitude, currentLocation.longitude),
                            destination = Point(
                                destinationLocation.first,
                                destinationLocation.second
                            )
                        )
                    )
                }
            }
        }
    }

    private fun zoomOnMePerform() {
        viewModelScope.launch {
            resetMapMode()
            val myLatestLocation = getLatestLocation(fusedLocationProviderClient)
            myLatestLocation?.let { location ->
                screenState.update {
                    it.copy(
                        mapMode = MapMode.ShowMyLocation(location.latitude, location.longitude)
                    )
                }
            }
        }
    }

    private fun resetMapMode() {
        screenState.update {
            it.copy(mapMode = MapMode.None)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun getLatestLocation(fusedLocationProviderClient: FusedLocationProviderClient): Location? {
        return withContext(Dispatchers.Default) {
            suspendCancellableCoroutine { continuation ->
                try {
                    val task = fusedLocationProviderClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        null
                    )
                    task.addOnSuccessListener { location ->
                        continuation.resume(location)
                    }.addOnFailureListener { e ->
                        continuation.resumeWithException(e)
                    }
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
                continuation.invokeOnCancellation {
                }
            }
        }
    }

    fun onEvent(events: MainEvents) {
        when (events) {
            MainEvents.ZoomOnMe -> {
                zoomOnMePerform()
            }

            MainEvents.CreateRoute -> {
                createRoute()
            }
        }
    }

    @Immutable
    data class MainState(
        val mapMode: MapMode = MapMode.None
    )
}

sealed interface MapMode {
    data class ShowMyLocation(val latitude: Double, val longitude: Double) : MapMode
    data class CreateRoute(val start: Point, val destination: Point) : MapMode
    object None : MapMode
}