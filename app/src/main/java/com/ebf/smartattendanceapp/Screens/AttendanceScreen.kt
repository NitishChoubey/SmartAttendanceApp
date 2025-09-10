
import android.Manifest


import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.ebf.smartattendanceapp.CameraView.CameraView
import com.ebf.smartattendanceapp.Overlay.AttendanceOverlay
import com.ebf.smartattendanceapp.Rationale.PermissionRationale
import com.ebf.smartattendanceapp.ViewModel.AttendanceState
import com.ebf.smartattendanceapp.ViewModel.AttendanceViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState


// ===== BIOMETRIC LOGIC MODIFIED HERE =====
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AttendanceScreen(navController: NavController, activity: FragmentActivity, viewModel: AttendanceViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    )

    // Use DisposableEffect to safely manage the listener's lifecycle.
    // It starts when the screen is shown and stops when it's hidden.
    DisposableEffect(permissionsState.allPermissionsGranted) {
        if (permissionsState.allPermissionsGranted) {
            viewModel.onPermissionsGranted()
        }
        // onDispose is called when the composable leaves the screen
        onDispose {
            viewModel.stopListening()
        }
    }

    // This LaunchedEffect will trigger the biometric prompt when the state changes to AUTHENTICATING
    LaunchedEffect(state, activity) {
        if (state == AttendanceState.AUTHENTICATING) {
            // Moved prompt creation and authentication logic inside the effect
            // to ensure it uses the correct context and lifecycle.
            val executor = ContextCompat.getMainExecutor(activity)
            // ✅ FIX: This now correctly uses the imported androidx.biometric.BiometricPrompt
            val biometricPrompt = BiometricPrompt(
                activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    // ✅ FIX: This method signature now correctly overrides the one from the androidx library
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        viewModel.onBiometricSuccess()
                    }

                    // ✅ FIX: This method signature now correctly overrides the one from the androidx library
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        // Don't flag as failure if user simply cancels the prompt
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            viewModel.onBiometricFailure(errString.toString())
                        } else {
                            navController.popBackStack() // Go back if user cancels
                        }
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Identity Verification")
                .setSubtitle("Verify it's you to mark attendance")
                .setDeviceCredentialAllowed(true)   // allows PIN/Pattern/Password fallback

                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }


    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when {
            permissionsState.allPermissionsGranted -> {
                if (state == AttendanceState.SCANNING || state == AttendanceState.SUCCESS) {
                    CameraView(
                        onQrScanned = { viewModel.onQrScanned(it) },
                        lifecycleOwner = lifecycleOwner
                    )
                }
                AttendanceOverlay(
                    state = state,
                    navController = navController,
                    onRetry = {
                        navController.popBackStack()
                        navController.navigate("attendance")
                    }
                )
            }
            permissionsState.shouldShowRationale || !permissionsState.allPermissionsGranted
                -> {
                PermissionRationale("This feature needs Camera and Mic access for secure attendance. Please grant permissions.") {
                    permissionsState.launchMultiplePermissionRequest()
                }
            }
            else -> {
                // Initial state before permission request is launched
                LaunchedEffect(Unit) {
                    if (!permissionsState.allPermissionsGranted) {
                        permissionsState.launchMultiplePermissionRequest()
                    }
                }
                Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
    }
}

