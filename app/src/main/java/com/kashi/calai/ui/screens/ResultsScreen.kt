// Updated ResultScreen with AI integration
package com.kashi.calai.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import com.kashi.calai.ai.NutritionAI
import com.kashi.calai.ai.NutritionAnalysis

@Composable
fun ResultScreen(navController: NavController, imageUri: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nutritionAI = remember { NutritionAI() }

    var nutritionData by remember { mutableStateOf<NutritionAnalysis?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf(1) }

    val painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context)
            .data(Uri.parse(imageUri))
            .crossfade(true)
            .build()
    )

    // Analyze nutrition when screen loads
    LaunchedEffect(imageUri) {
        scope.launch {
            nutritionAI.analyzeNutrition(Uri.parse(imageUri), context)
                .onSuccess { data ->
                    nutritionData = data
                    isLoading = false
                }
                .onFailure { exception ->
                    error = exception.message
                    isLoading = false
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        if (isLoading) {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.Black)
                    Text(
                        text = "Analyzing nutrition...",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else if (error != null) {
            // Error state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Error analyzing image",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                    Text(
                        text = error ?: "Unknown error",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                    ) {
                        Text("Go Back", color = Color.White)
                    }
                }
            }
        } else {
            // Success state with nutrition data
            nutritionData?.let { data ->
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Image with floating labels
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = "Food Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dynamic floating nutrition labels
                        data.foodItems.forEachIndexed { index, item ->
                            val positions = listOf(
                                Alignment.TopStart to Modifier.padding(start = 24.dp, top = 24.dp),
                                Alignment.TopEnd to Modifier.padding(end = 24.dp, top = 60.dp),
                                Alignment.BottomStart to Modifier.padding(start = 24.dp, bottom = 24.dp),
                                Alignment.BottomEnd to Modifier.padding(end = 24.dp, bottom = 60.dp)
                            )

                            if (index < positions.size) {
                                NutritionLabel(
                                    text = "${item.name}\n${item.calories * quantity}",
                                    modifier = Modifier
                                        .align(positions[index].first)
                                        .then(positions[index].second)
                                )
                            }
                        }
                    }

                    // Bottom Card with dynamic data
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp)
                        ) {
                            // Title and quantity
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = data.mealType,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = data.dishName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                }

                                // Quantity selector
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(24.dp))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Decrease",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                if (quantity > 1) quantity--
                                            }
                                    )
                                    Text(
                                        text = quantity.toString(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = Color.Gray,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable { quantity++ }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Dynamic nutrition grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                NutritionItem(
                                    icon = Icons.Default.Face,
                                    label = "Calories",
                                    value = (data.totalCalories * quantity).toString(),
                                    color = Color.Black
                                )
                                NutritionItem(
                                    icon = Icons.Default.Face,
                                    label = "Carbs",
                                    value = data.macroNutrients.carbs,
                                    color = Color(0xFFFF9800)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                NutritionItem(
                                    icon = Icons.Default.Face,
                                    label = "Protein",
                                    value = data.macroNutrients.protein,
                                    color = Color(0xFFE91E63)
                                )
                                NutritionItem(
                                    icon = Icons.Default.Face,
                                    label = "Fats",
                                    value = data.macroNutrients.fats,
                                    color = Color(0xFF2196F3)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Dynamic health score
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Health score",
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                                Text(
                                    text = "${data.healthScore}/${data.healthScoreOutOf}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Dynamic health score progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(data.healthScore.toFloat() / data.healthScoreOutOf)
                                        .height(8.dp)
                                        .background(
                                            when {
                                                data.healthScore >= 8 -> Color(0xFF4CAF50)
                                                data.healthScore >= 6 -> Color(0xFFFF9800)
                                                else -> Color(0xFFFF5722)
                                            },
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        // Retake photo
                                        navController.popBackStack()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.Black
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Retake")
                                }

                                Button(
                                    onClick = {
                                        navController.navigate("overview") {
                                            popUpTo("overview") { inclusive = true }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Black,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text("Add to Log")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NutritionItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}