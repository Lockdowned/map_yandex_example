package com.example.composemvvm.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.composemvvm.R
import com.example.composemvvm.ui.main.viewmodel.MainViewModel
import com.example.composemvvm.ui.main.viewmodel.MapMode
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.Error
import com.yandex.runtime.image.ImageProvider


class YandexMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MapView(context, attrs, defStyleAttr) {
    init {
        MapKitFactory.initialize(context)
    }
}

private fun getMapLifecycleObserver(mapView: YandexMapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START, Lifecycle.Event.ON_STOP -> {
                mapView.onStart()
                MapKitFactory.getInstance().apply {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }
            }

            else -> {} // Handle other events if needed
        }
    }

@Composable
fun YandexMap(state: MainViewModel.MainState) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        onDispose {
            // Release resources when the Composable function is removed from the composition
            MapKitFactory.getInstance().onStop()
        }
    }

    val yandexMapView = remember {
        YandexMapView(context)
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(key1 = lifecycle, key2 = yandexMapView) {
        val lifecycleObserver = getMapLifecycleObserver(yandexMapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    val mapRef = yandexMapView.mapWindow.map

    val drivingRouteListener = remember {
        onCreateRoutePerform(mapRef)
    }


    val zoomLevel = 14.0f
    val azimuth = 0.0f
    val tilt = 0.0f

    AndroidView(
        factory = {
            yandexMapView.apply {}
        }
    ) {
        when (val mode = state.mapMode) {
            is MapMode.CreateRoute -> {
                crateRouteYandex(mode, drivingRouteListener, mapRef, zoomLevel, azimuth, tilt)
            }

            is MapMode.ShowMyLocation -> {
                showMyLocationYandex(mode, mapRef, zoomLevel, azimuth, tilt, context)

            }

            MapMode.None -> {}
        }
    }
}

private fun showMyLocationYandex(
    mode: MapMode.ShowMyLocation,
    mapRef: Map,
    zoomLevel: Float,
    azimuth: Float,
    tilt: Float,
    context: Context
) {
    val targetLocation = Point(mode.latitude, mode.longitude)

    // Move the camera to the target location
    moveMap(mapRef, targetLocation, zoomLevel, azimuth, tilt)

    mapRef.mapObjects.clear()

    // Load the original bitmap from your resource
    val originalBitmap =
        BitmapFactory.decodeResource(context.resources, R.raw.location)

    val newWidth = 100 // in pixels
    val newHeight = 100 // in pixels

    val scaledBitmap =
        Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)

    val imageProvider = ImageProvider.fromBitmap(scaledBitmap)

    mapRef.mapObjects.addPlacemark().apply {
        geometry = targetLocation
        setIcon(imageProvider)
    }
}

private fun crateRouteYandex(
    mode: MapMode.CreateRoute,
    drivingRouteListener: DrivingSession.DrivingRouteListener,
    mapRef: Map,
    zoomLevel: Float,
    azimuth: Float,
    tilt: Float
) {
    val drivingOptions = DrivingOptions().apply {
        routesCount = 1
    }
    val vehicleOptions = VehicleOptions()

    val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(
        DrivingRouterType.COMBINED
    )

    val points = buildList {
        add(RequestPoint(mode.start, RequestPointType.WAYPOINT, null, null))
        add(RequestPoint(mode.destination, RequestPointType.WAYPOINT, null, null))
    }

    drivingRouter.requestRoutes(
        points,
        drivingOptions,
        vehicleOptions,
        drivingRouteListener
    )

    moveMap(mapRef, mode.destination, zoomLevel, azimuth, tilt)
}

private fun moveMap(
    mapRef: Map,
    point: Point,
    zoomLevel: Float,
    azimuth: Float,
    tilt: Float
) {
    mapRef.move(CameraPosition(point, zoomLevel, azimuth, tilt))
}

private fun onCreateRoutePerform(mapRef: Map) =
    object : DrivingSession.DrivingRouteListener {
        override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
            if (drivingRoutes.isNotEmpty()) {
                val bestRoute = drivingRoutes[0] // Select the best route from the list (index 0)
                val mapObjects = mapRef.mapObjects // Get the MapObjectCollection for your mapView

                val polylineMapObject =
                    mapObjects.addPolyline(bestRoute.geometry) // Add a PolylineMapObject to the map using the geometry of the best route

                // Optionally, you can customize the appearance of the route (e.g., color, width, etc.)
                polylineMapObject.setStrokeColor(Color.BLUE)
                polylineMapObject.strokeWidth = 4f
            } else {
                // Handle case when no routes are available
                // (e.g., display an error message or take alternative actions)
            }
        }

        override fun onDrivingRoutesError(p0: Error) {
            Log.d("YandexMap", "onDrivingRoutesError: $p0")
        }
    }