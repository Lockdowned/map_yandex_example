package com.example.composemvvm.ui.map

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.composemvvm.R
import com.example.composemvvm.ui.main.viewmodel.MainEvents
import com.example.composemvvm.ui.main.viewmodel.MainViewModel


const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

@Composable
fun YandexMapScreen(state: MainViewModel.MainState, events: (MainEvents) -> Unit) {
    val context = LocalContext.current

    val permissionLocationProvided =
        permissionGrantedCallBack(onPermissionGranted = {}, onPermissionDenied = {})

    Surface(modifier = Modifier.fillMaxSize()) {

        Box(Modifier.fillMaxSize()) {
            YandexMap(state) // Add the Yandex Map to your UI

            Row(modifier = Modifier.align(Alignment.BottomCenter)) {
                Button(
                    modifier = Modifier.padding(end = 20.dp),
                    onClick = {
                        createRoutePermissionHandler(
                            context,
                            permissionLocationProvided
                        ) {
                            events(MainEvents.CreateRoute)
                        }
                    }) {
                    Text(text = stringResource(id = R.string.start_route))
                }
                Button(
                    modifier = Modifier.padding(end = 20.dp),
                    onClick = {
                        createRoutePermissionHandler(context, permissionLocationProvided) {
                            events(MainEvents.ZoomOnMe)
                        }
                    }) {
                    Text(text = stringResource(id = R.string.zoom_on_me))
                }
            }
        }
    }
}

private fun isLocationGranted(context: Context): Boolean = ContextCompat.checkSelfPermission(
    context,
    LOCATION_PERMISSION
) == PackageManager.PERMISSION_GRANTED

@Composable
fun permissionGrantedCallBack(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) =
    rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            onPermissionGranted()
        } else {
            onPermissionDenied()
        }
    }

fun createRoutePermissionHandler(
    context: Context,
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    onPermissionGranted: () -> Unit
) {
    if (isLocationGranted(context)) {
        onPermissionGranted()
    } else {
        launcher.launch(LOCATION_PERMISSION)
    }
}