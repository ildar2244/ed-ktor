package com.example

import com.example.lessons.edRequestsRateLimit
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.routes.configureArticles
import io.ktor.server.application.*

fun main(args: Array<String>): Unit =
        io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureSerialization()
    configureRouting()
    configureSockets()
    configureArticles()
    edRequestsRateLimit()
}
