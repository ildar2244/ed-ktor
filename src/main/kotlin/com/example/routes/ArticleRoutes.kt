package com.example.routes

import com.example.dao.dao
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import io.ktor.server.resources.post as postResource
import io.ktor.server.resources.put
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.server.routing.post as postRoute
import kotlinx.html.*

@Resource("/articles")
class Articles(val sort: String? = " new") {
    @Resource("new")
    class New(val parent: Articles = Articles())

    @Resource("{id}")
    class Id(val parent: Articles = Articles(), val id: Long) {
        @Resource("edit")
        class Edit(val parent: Id)
    }
}

fun Application.configureArticles() {
    install(Resources)
    routing {
        get<Articles> { article ->
            call.respondText("List of articles sorted starting from ${article.sort}")
        }
        get<Articles.New> {
            call.respondText("Create a new article")
        }
        postResource<Articles> {
            call.respondText("An article is saved", status = HttpStatusCode.Created)
        }
        get<Articles.Id> { article ->
            // Show an article with id ${article.id} ...
            call.respondText("An article with id ${article.id}", status = HttpStatusCode.OK)
        }
        get<Articles.Id.Edit> { article ->
            // Show a page with fields for editing an article ...
            call.respondText("Edit an article with id ${article.parent.id}", status = HttpStatusCode.OK)
        }
        put<Articles.Id> { article ->
            call.respondText("An article with id ${article.id} updated", status = HttpStatusCode.OK)
        }
        delete<Articles.Id> { article ->
            // Delete an article ...
            call.respondText("An article with id ${article.id} deleted", status = HttpStatusCode.OK)
        }

        get {
            call.respondHtml {
                body {
                    //для href требуется instance Application, поэтому this@configureArticles
                    this@configureArticles.apply {
                        p {
                            val link: String = href(Articles())
                            a(link) { +"Get all articles" }
                        }
                        p {
                            val link: String = href(Articles.New())
                            a(link) { +"Create a new article" }
                        }
                        p {
                            val link: String = href(Articles.Id.Edit(Articles.Id(id = 123)))
                            a(link) { +"Edit an exising article" }
                        }
                        p {
                            val urlBuilder =
                                URLBuilder(URLProtocol.HTTPS, "ktor.io", parameters = parametersOf("token", "123"))
                            href(Articles(sort = null), urlBuilder)
                            val link: String = urlBuilder.buildString()
                            i { a(link) { +link } }
                        }
                    }
                }
            }
        }
    }
}

fun Application.configureArticlesDB() {
    routing {
        get {
            val sb = StringBuilder("An existing articles: ")
            val articles = dao.allArticles()
            val result = articles.joinTo(sb).toString()
            call.respondText(result, status = HttpStatusCode.OK)
        }

        postRoute {
            val formParameters = call.receiveParameters()
            val title = formParameters.getOrFail("title")
            val body = formParameters.getOrFail("body")
            val article = dao.addNewArticle(title, body)
            call.respondRedirect("/articles/${article?.id}")
        }

        get("{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val article = dao.article(id)?.toString() ?: ""
            call.respondText(article, status = HttpStatusCode.OK)
        }

        get("{id}/edit") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val article = dao.article(id)?.toString() ?: ""
            call.respondText("Edit: $article", status = HttpStatusCode.OK)
        }

        postRoute("{id}") {
            val id = call.parameters.getOrFail<Int>("id").toInt()
            val formParameters = call.receiveParameters()
            when (formParameters.getOrFail("_action")) {
                "update" -> {
                    val title = formParameters.getOrFail("title")
                    val body = formParameters.getOrFail("body")
                    dao.editArticle(id, title, body)
                    call.respondRedirect("/articles/$id")
                }
                "delete" -> {
                    dao.deleteArticle(id)
                    call.respondRedirect("/articles")
                }
            }
        }
    }
}