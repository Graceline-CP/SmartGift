package com.smartgift

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.gson.*
import io.ktor.server.html.*
import kotlinx.html.*
import com.smartgift.model.GiftFormData
import com.smartgift.service.GPTService
import io.ktor.server.http.content.*
import io.ktor.http.ContentType


fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            gson()
        }
        routing {
            static("/static") {
            resources("static")
            }
            get("/") {
                call.respondText("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>SmartGift - Welcome</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            text-align: center;
                            margin-top: 100px;
                            background-color: #f9f9fb;
                        }
                        h1 {
                            font-size: 42px;
                            color: #4b0082;
                        }
                        p {
                            font-size: 20px;
                            color: #333;
                        }
                        button {
                            font-size: 22px;
                            padding: 15px 40px;
                            background-color: #6a0dad;
                            color: white;
                            border: none;
                            border-radius: 12px;
                            cursor: pointer;
                            transition: background-color 0.3s ease;
                        }
                        button:hover {
                            background-color: #5a009d;
                        }
                    </style>
                </head>
                <body>
                    <h1>🎁 Welcome to SmartGift</h1>
                    <p>Your personalized gift recommender</p>
                    <form action="/form">
                        <button type="submit">Start Survey</button>
                    </form>
                </body>
                </html>
                """.trimIndent(), ContentType.Text.Html)

            }
            get("/form") {
                call.respondText("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>SmartGift - Survey</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            background-color: #f2f2f2;
                            padding: 40px;
                        }
                        h2 {
                            text-align: center;
                            color: #4b0082;
                            font-size: 32px;
                        }
                        form {
                            background-color: #fff;
                            padding: 30px;
                            max-width: 400px;
                            margin: auto;
                            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
                            border-radius: 10px;
                            display: flex;
                            flex-direction: column;
                        }
                        label {
                            margin-bottom: 10px;
                            font-weight: 600;
                        }
                        input, select {
                            padding: 10px;
                            border: 1px solid #ccc;
                            border-radius: 6px;
                            margin-bottom: 20px;
                        }
                        button {
                            background-color: #6a0dad;
                            color: white;
                            padding: 12px;
                            font-size: 16px;
                            border: none;
                            border-radius: 8px;
                            cursor: pointer;
                            transition: background-color 0.3s ease;
                        }
                        button:hover {
                            background-color: #5a009d;
                        }
                    </style>
                </head>
                <body>
                    <h2>📝 Gift Survey</h2>
                    <form action="/submit-form" method="post">
                        <label>Age Group:
                            <input type="text" name="ageGroup" required />
                        </label>
                        <label>Budget:
                            <select name="budget" required>
                                <option>High</option>
                                <option>Medium</option>
                                <option>Low</option>
                            </select>
                        </label>
                        <label>Relationship:
                            <select name="relationship" required>
                                <option>Friend</option>
                                <option>Sibling</option>
                                <option>Partner</option>
                                <option>Parent</option>
                                <option>Other</option>
                            </select>
                        </label>
                        <label>Likes:
                            <input type="text" name="interests" required />
                        </label>
                        <label>Occasion:
                            <input type="text" name="occasion" required />
                        </label>
                        <button type="submit">Get My Gift 🎁</button>
                    </form>
                </body>
                </html>
                """.trimIndent(), ContentType.Text.Html)
            }
            post("/submit-form") {
                val params = call.receiveParameters()
                val ageGroup = params["ageGroup"] ?: ""
                val interests = params["interests"] ?: ""
                val relationship = params["relationship"] ?: ""
                val budget = params["budget"] ?: ""
                val occasion = params["occasion"] ?: ""

                val formData = GiftFormData(
                    ageGroup = ageGroup,
                    interests = interests,
                    relationship = relationship,
                    occasion = occasion,
                    budget = budget
                )
                val suggestion = GPTService.callGemini(formData) 

                call.respondText("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <title>SmartGift - Result</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            text-align: center;
                            background-color: #f4f0f8;
                            margin-top: 80px;
                        }
                        .card {
                            margin: auto;
                            padding: 40px 30px;
                            width: 90%;
                            max-width: 600px;
                            border-radius: 16px;
                            background-color: #ffffff;
                            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
                        }
                        h2 {
                            color: #6a0dad;
                            font-size: 32px;
                            margin-bottom: 24px;
                        }
                        .gift-item {
                            background-color: #f0e6ff;
                            padding: 16px 20px;
                            margin: 10px auto;
                            border-radius: 12px;
                            font-size: 18px;
                            font-weight: 500;
                            color: #4b0082;
                            max-width: 90%;
                            text-align: left;
                            box-shadow: 0 2px 8px rgba(106, 13, 173, 0.1);
                        }
                        a {
                            display: inline-block;
                            margin-top: 30px;
                            color: #6a0dad;
                            font-size: 16px;
                            text-decoration: none;
                            font-weight: bold;
                        }
                        a:hover {
                            text-decoration: underline;
                        }
                    </style>
                    </head>
                    <body>
                        <div class="card">
                            <h2>🎉 Perfect Gift Suggestion</h2>
                            ${suggestion.trim().lines().joinToString("\n") { "<div class=\"gift-item\">$it</div>" }}
                            <a href="/">Back to Home</a>
                        </div>
                    </body>
                    </html>
                """.trimIndent(), ContentType.Text.Html)
            }
            post("/recommend") {
                val params = call.receiveParameters()
                val userInput = GiftFormData(
                    ageGroup = params["ageGroup"] ?: "",
                    interests = params["interests"] ?: "",
                    relationship = params["relationship"] ?: "",
                    occasion = params["occasion"] ?: "",
                    budget = params["budget"] ?: ""
                )

                val suggestion = GPTService.callGemini(userInput)
                call.respondText("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>SmartGift - Result</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                            text-align: center;
                            background-color: #f4f0f8;
                            margin-top: 80px;
                        }
                        .card {
                            margin: auto;
                            padding: 40px 30px;
                            width: 90%;
                            max-width: 600px;
                            border-radius: 16px;
                            background-color: #ffffff;
                            box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
                        }
                        h2 {
                            color: #6a0dad;
                            font-size: 32px;
                            margin-bottom: 24px;
                        }
                        .gift-item {
                            background-color: #f0e6ff;
                            padding: 16px 20px;
                            margin: 10px auto;
                            border-radius: 12px;
                            font-size: 18px;
                            font-weight: 500;
                            color: #4b0082;
                            max-width: 90%;
                            text-align: left;
                            box-shadow: 0 2px 8px rgba(106, 13, 173, 0.1);
                        }
                        a {
                            display: inline-block;
                            margin-top: 30px;
                            color: #6a0dad;
                            font-size: 16px;
                            text-decoration: none;
                            font-weight: bold;
                        }
                        a:hover {
                            text-decoration: underline;
                        }
                    </style>

                </head>
                <body>
                    <div class="card">
                        <h2>🎉 Perfect Gift Suggestion</h2>
                        ${suggestion.trim().lines().joinToString("\n") { "<div class=\"gift-item\">$it</div>" }}
                        <a href="/">← Back to Home</a>
                    </div>
                </body>
                </html>
                """.trimIndent(), ContentType.Text.Html)

            }
        }
    }.start(wait = true)
}
