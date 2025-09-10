package com.ebf.smartattendanceapp.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ebf.smartattendanceapp.Network.AttendanceRepository
import com.ebf.smartattendanceapp.UltrasonicDetector.UltrasonicDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AttendanceViewModel : ViewModel() {
    private val _state = MutableStateFlow(AttendanceState.REQUESTING_PERMISSIONS)
    val state = _state.asStateFlow()

    private val ultrasonicDetector = UltrasonicDetector()
    private var listeningJob: Job? = null
    private val attendanceRepository = AttendanceRepository()
    private var hasMarkedAttendance = false

    fun onPermissionsGranted() {
        if (_state.value == AttendanceState.REQUESTING_PERMISSIONS) {
            _state.value = AttendanceState.LISTENING_FOR_AUDIO
            startListeningForUltrasonicSound()
        }
    }

    private fun startListeningForUltrasonicSound() {
        stopListening() // Ensure any previous job is cancelled
        listeningJob = viewModelScope.launch(Dispatchers.IO) {
            ultrasonicDetector.startListening()
            // Collect the flow to listen for detection events
            ultrasonicDetector.isHearingUltrasonic.collect { isHearing ->
                if (isHearing && isActive) {
                    // Switch to the main thread to update the UI state
                    launch(Dispatchers.Main) {
                        _state.update { currentState ->
                            // Only transition if we are in the listening state
                            if (currentState == AttendanceState.LISTENING_FOR_AUDIO) {
                                AttendanceState.AUTHENTICATING
                            } else {
                                currentState
                            }
                        }
                        stopListening() // Stop listening once detected
                    }
                }
            }
        }
    }

    fun stopListening() {
        listeningJob?.cancel()
        ultrasonicDetector.stopListening()
    }

    fun onBiometricSuccess() {
        _state.value = AttendanceState.SCANNING
    }

    fun onBiometricFailure(error: String) {
        _state.value = AttendanceState.FAILURE
        stopListening() // Also stop listening on failure
    }

    fun onQrScanned(qrValue: String?) {
        if (_state.value == AttendanceState.SCANNING && !hasMarkedAttendance) {
            if (qrValue != null && qrValue.startsWith("CLASS_SESSION_")) {
                hasMarkedAttendance = true
                viewModelScope.launch {
                    val success = withContext(Dispatchers.IO) {
                        attendanceRepository.markAttendance(qrValue)
                    }
                    _state.value = if (success) AttendanceState.SUCCESS else AttendanceState.FAILURE
                }
            } else {
                // This would be a failure in a real app if an invalid QR is scanned
                // For prototype, we'll keep it in scanning state.
            }
        }
    }

    fun resetState() {
        _state.value = AttendanceState.REQUESTING_PERMISSIONS
        hasMarkedAttendance = false
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}