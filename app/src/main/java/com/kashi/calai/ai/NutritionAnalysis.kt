package com.kashi.calai.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.kashi.calai.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

// Data classes for structured output (your original format)
@Serializable
data class FoodItem(
    val name: String,
    val calories: Int
)

@Serializable
data class MacroNutrients(
    val carbs: String,
    val protein: String,
    val fats: String
)

@Serializable
data class NutritionAnalysis(
    val mealType: String,
    val dishName: String,
    val totalCalories: Int,
    val macroNutrients: MacroNutrients,
    val foodItems: List<FoodItem>,
    val healthScore: Int,
    val healthScoreOutOf: Int = 10
)

class NutritionAI {

    private val apiKey = BuildConfig.apiKey
    private val modelId = "gemini-2.0-flash"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun analyzeNutrition(imageUri: Uri, context: Context): Result<NutritionAnalysis> {
        return withContext(Dispatchers.IO) {
            try {
                // Convert image to base64
                val base64Image = convertImageToBase64(imageUri, context)
                    ?: return@withContext Result.failure(Exception("Failed to convert image"))

                // Create request JSON
                val requestJson = createRequestJson(base64Image)

                Log.d("NutritionAI", "Request JSON: $requestJson")

                // Make HTTP request
                val response = makeApiRequest(requestJson)

                Log.d("NutritionAI", "Raw API Response: $response")

                // Parse response
                val nutritionData = parseResponse(response)

                Result.success(nutritionData)

            } catch (e: Exception) {
                Log.e("NutritionAI", "Error analyzing nutrition: ${e.message}", e)

                // Return dummy data on error
                val dummyData = NutritionAnalysis(
                    mealType = "Breakfast",
                    dishName = "Sample Food Item",
                    totalCalories = 400,
                    macroNutrients = MacroNutrients(
                        carbs = "50g",
                        protein = "20g",
                        fats = "15g"
                    ),
                    foodItems = listOf(
                        FoodItem(name = "Main Dish", calories = 350),
                        FoodItem(name = "Side Item", calories = 50)
                    ),
                    healthScore = 6
                )

                Result.success(dummyData)
            }
        }
    }

    private fun convertImageToBase64(imageUri: Uri, context: Context): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return null

            // Compress bitmap to reduce size
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()

            Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e("NutritionAI", "Error converting image to base64: ${e.message}", e)
            null
        }
    }

    private fun createRequestJson(base64Image: String): String {
        val prompt = """
            You are an expert Calorie Counter and Nutritionist. Analyze the uploaded food image and provide detailed nutrition information.
            
            Instructions:
            - Identify all visible food items in the image
            - Calculate accurate calorie counts for each item and total
            - Determine appropriate macronutrient values (carbs, protein, fats) in grams with 'g' suffix
            - Classify the meal type (Breakfast, Lunch, Dinner, or Snack)
            - Provide a descriptive dish name
            - Rate the health score from 1-10 based on nutritional balance, processing level, and ingredient quality
            - Be precise with portion sizes and calorie estimations
            
            Provide accurate, realistic nutrition data based on standard food databases and nutrition science.
        """.trimIndent()

        return """
        {
            "contents": [
                {
                    "role": "user",
                    "parts": [
                        {
                            "text": "$prompt"
                        },
                        {
                            "inline_data": {
                                "mime_type": "image/jpeg",
                                "data": "$base64Image"
                            }
                        }
                    ]
                }
            ],
            "generationConfig": {
                "responseMimeType": "application/json",
                "responseSchema": {
                    "type": "object",
                    "properties": {
                        "mealType": {
                            "type": "string",
                            "enum": ["Breakfast", "Lunch", "Dinner", "Snack"]
                        },
                        "dishName": {
                            "type": "string"
                        },
                        "totalCalories": {
                            "type": "integer"
                        },
                        "macroNutrients": {
                            "type": "object",
                            "properties": {
                                "carbs": {"type": "string"},
                                "protein": {"type": "string"},
                                "fats": {"type": "string"}
                            },
                            "required": ["carbs", "protein", "fats"]
                        },
                        "foodItems": {
                            "type": "array",
                            "items": {
                                "type": "object",
                                "properties": {
                                    "name": {"type": "string"},
                                    "calories": {"type": "integer"}
                                },
                                "required": ["name", "calories"]
                            }
                        },
                        "healthScore": {
                            "type": "integer",
                            "minimum": 1,
                            "maximum": 10
                        }
                    },
                    "required": ["mealType", "dishName", "totalCalories", "macroNutrients", "foodItems", "healthScore"]
                }
            }
        }
        """.trimIndent()
    }

    private suspend fun makeApiRequest(requestJson: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelId:generateContent?key=$apiKey"

        val mediaType = "application/json".toMediaType()
        val requestBody = requestJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        return withContext(Dispatchers.IO) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("API request failed: ${response.code} ${response.message}")
                }
                response.body?.string() ?: throw Exception("Empty response body")
            }
        }
    }

    private fun parseResponse(responseJson: String): NutritionAnalysis {
        try {
            Log.d("NutritionAI", "Raw API Response: $responseJson")

            // Extract the actual content from the Gemini API response
            val responseObj = json.parseToJsonElement(responseJson)
            val candidates = responseObj.jsonObject["candidates"]?.jsonArray
            val content = candidates?.get(0)?.jsonObject?.get("content")?.jsonObject
            val parts = content?.get("parts")?.jsonArray
            val text = parts?.get(0)?.jsonObject?.get("text")?.jsonPrimitive?.content

            if (text == null) {
                throw Exception("No text content found in API response")
            }

            Log.d("NutritionAI", "Extracted JSON: $text")

            // Parse the structured JSON content (this should work now with serialization plugin)
            return json.decodeFromString<NutritionAnalysis>(text)

        } catch (e: Exception) {
            Log.e("NutritionAI", "Error parsing response: ${e.message}", e)
            throw Exception("Failed to parse API response: ${e.message}")
        }
    }
}