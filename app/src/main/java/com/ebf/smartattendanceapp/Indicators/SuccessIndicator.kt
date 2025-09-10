package com.ebf.smartattendanceapp.Indicators

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ebf.smartattendanceapp.R
import kotlinx.coroutines.delay

@Composable
fun SuccessIndicator(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(2500)
        navController.popBackStack()
    }
    Icon(painter = painterResource(id = R.drawable.ic_check_circle), contentDescription = "Success", tint = Color.White, modifier = Modifier.size(120.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text("Attendance Marked!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
}