package com.smartgift.service

import com.smartgift.model.GiftFormData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.statement.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import io.github.cdimascio.dotenv.dotenv

// ✅ Load environment only once at top level
val dotenv = dotenv()
val geminiApiKey = dotenv["GEMINI_API_KEY"] ?: error("GEMINI_API_KEY not set in .env file")

object GPTService {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    suspend fun callGemini(promptData: GiftFormData): String {
        val promptText = buildPromptFromGiftForm(promptData)

        return try {
            val response: HttpResponse = client.post("https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash:generateContent?key=$geminiApiKey") {
                contentType(ContentType.Application.Json)
                setBody(
                    mapOf(
                        "contents" to listOf(
                            mapOf(
                                "parts" to listOf(
                                    mapOf("text" to promptText)
                                )
                            )
                        )
                    )
                )
            }

            // Check for 200 OK before decoding
            if (response.status == HttpStatusCode.OK) {
                val geminiResponse = response.body<GeminiResponse>()
                val text = geminiResponse.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                return text ?: "Gemini returned no content."
            } else {
                // Try to parse and show error response
                val errorBody = response.body<ErrorResponse>()
                return "Gemini API Error ${errorBody.error.code}: ${errorBody.error.message}"
            }
        } catch (e: Exception) {
            println("Gemini API call failed: ${e.message}")
            e.printStackTrace()
            return "Error: Gemini API call failed."
        }
    }


    private fun buildPromptFromGiftForm(data: GiftFormData): String {
        return """
            Based on the following details:
            - Age Group: ${data.ageGroup}
            - Relationship: ${data.relationship}
            - Interests: ${data.interests}
            - Budget: ${data.budget}
            - Occasion: ${data.occasion}
            
            Give exactly 3–4 short and creative gift suggestions in a clear bullet-point format.
            Be concise, avoid long explanations. Example format:
            - 🎁 Gift idea 1
            - 🎁 Gift idea 2
            Only return the list.
        """.trimIndent()
    }

}

@Serializable
data class Candidate(
    val content: Content
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null
)
@Serializable
data class ErrorResponse(
    val error: ErrorDetail
)

@Serializable
data class ErrorDetail(
    val code: Int,
    val message: String
)