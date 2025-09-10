package com.ebf.smartattendanceapp.Indicators

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ebf.smartattendanceapp.R

@Composable
fun FailureIndicator(onRetry: () -> Unit) {
    Icon(painter = painterResource(id = R.drawable.ic_error), contentDescription = "Failure", tint = Color.White, modifier = Modifier.size(120.dp))
    Spacer(modifier = Modifier.height(16.dp))
    Text("Authentication Failed", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
    Text("Please try again.", color = Color.White.copy(alpha = 0.8f), fontSize = 18.sp)
    Spacer(modifier = Modifier.height(32.dp))
    Button(onClick = onRetry) {
        Text("RETRY")
    }
}