package com.smartgift.routes 

import com.smartgift.model.GiftFormData
import com.smartgift.service.GPTService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import kotlinx.html.*

fun Route.formRoutes(gptService: GPTService) {

    // Show the input form
    get("/") {
        call.respondHtml {
            head { 
                    link(rel = "stylesheet", href = "/static/styles.css", type = "text/css")
                 }
            body {
                h1 { +"SmartGift 🎁" }
                form(action = "/submit", method = FormMethod.post) {
                    label {
                        +"Age Group: "
                        textInput(name = "age_group") { placeholder = "e.g. Teenager" }
                    }; br()

                    label {
                        +"Hobbies: "
                        textInput(name = "hobbies") { placeholder = "e.g. Drawing, Gaming" }
                    }; br()

                    label {
                        +"Relationship: "
                        textInput(name = "relationship") { placeholder = "e.g. Friend" }
                    }; br()

                    label {
                        +"Likes: "
                        textInput(name = "likes") { placeholder = "e.g. BTS, Cats" }
                    }; br()

                    label {
                        +"Interests: "
                        textInput(name = "interests") { placeholder = "e.g. Tech, Fashion" }
                    }; br()

                    label {
                        +"Occasion: "
                        textInput(name = "occasion") { placeholder = "e.g. Birthday" }
                    }; br()

                    label {
                        +"Budget: "
                        textInput(name = "budget") { placeholder = "e.g. 1000" }
                    }; br()

                    br()
                    submitInput { value = "Get Gift 🎁" }
                }
            }
        }
    }

    // Handle form submission
    post("/submit") {
        val params = call.receiveParameters()

        val formData = GiftFormData(
            ageGroup = params["age_group"] ?: "",
            relationship = params["relationship"] ?: "",
            interests = params["interests"] ?: "",
            occasion = params["occasion"] ?: "",
            budget = params["budget"] ?: ""
        )

        val recommendation = gptService.callGemini(formData)

        call.respondHtml {
            head { title { +"Gift Recommendation" } }
            body {
                h1 { +"🎉 Recommended Gift 🎉" }
                p { +recommendation }
                a(href = "/") { +"Go Back" }
            }
        }
    }
}
