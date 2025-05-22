package com.kashi.calai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalorieBox(
    calorieValue: String = "1250",
    calorieLabel: String = "Calories left",
    progress: Float = 0.7f // Set dynamically if needed
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(16.dp))
            .border(1.dp, Color.Gray.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = calorieValue,
                color = Color.Black,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = calorieLabel,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(60.dp),
                color = Color.Black,
                strokeWidth = 6.dp,
                trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
            )
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_compass), // Replace with flame or custom icon
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}