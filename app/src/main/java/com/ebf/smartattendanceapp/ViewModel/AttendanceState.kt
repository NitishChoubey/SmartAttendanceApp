package com.ebf.smartattendanceapp.ViewModel

enum class AttendanceState {

    REQUESTING_PERMISSIONS,
    LISTENING_FOR_AUDIO,
    AUTHENTICATING,
    SCANNING,
    SUCCESS,
    FAILURE
}