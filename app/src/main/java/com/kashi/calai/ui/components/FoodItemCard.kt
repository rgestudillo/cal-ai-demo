package com.kashi.calai.ui.components

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FoodItemCard(title: String, kcal: Int, protein: String, carbs: String, fats: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
    ) {
        // Food Image Placeholder
        Box(
            modifier = Modifier
                .width(100.dp)
                .fillMaxHeight()
                .background(Color.Gray)
                .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
        )

        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    color = Color(0xFFF3F3F3),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("9:00am", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 12.sp)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_menu_compass), // Replace with flame icon
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("${kcal} kcal", color = Color.Gray, fontSize = 14.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🍗 $protein", fontSize = 12.sp)
                Text("🌾 $carbs", fontSize = 12.sp)
                Text("🛢 $fats", fontSize = 12.sp)
            }
        }
    }
}
