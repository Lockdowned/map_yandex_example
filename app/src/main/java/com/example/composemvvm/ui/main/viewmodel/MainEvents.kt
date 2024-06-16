package com.example.composemvvm.ui.main.viewmodel

import com.example.composemvvm.common.BottomNavigationScreens

sealed interface MainEvents {
    object ZoomOnMe : MainEvents
    object CreateRoute : MainEvents
}