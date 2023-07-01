package com.example

import com.example.dao.DatabaseFactory
import com.example.lessons.edRequestsRateLimit
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.routes.configureArticles
import com.example.routes.configureArticlesDB
import io.ktor.server.application.*

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    DatabaseFactory.init(environment.config)
    configureSerialization()
    configureRouting()
    configureSockets()
    configureArticles()
    edRequestsRateLimit()
    configureArticlesDB()
}
