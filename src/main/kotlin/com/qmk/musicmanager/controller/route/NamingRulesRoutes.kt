package com.qmk.musicmanager.controller.route

import com.qmk.musicmanager.controller.model.BasicAPIResponse
import com.qmk.musicmanager.controller.model.ServerError
import com.qmk.musicmanager.domain.model.NamingRule
import com.qmk.musicmanager.server
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.namingRulesRoutes() {
    route("/api/naming-rules") {
        get {
            val namingRules = server.getNamingRules()
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, namingRules))
        }
        post {
            val namingRule = call.receiveNullable<NamingRule>()
            if (namingRule == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result = server.addNamingRule(namingRule.replace, namingRule.replaceBy, namingRule.priority)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.Created, BasicAPIResponse(true, result))
        }
    }
    route("/api/naming-rules/{id}") {
        get {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val result = server.getNamingRule(id)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@get
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
        post {
            val namingRuleId = call.parameters["id"]?.toInt()
            val namingRule = call.receiveNullable<NamingRule>()
            if (namingRuleId == null || namingRule == null || namingRuleId != namingRule.id) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val result =
                server.editNamingRule(namingRuleId, namingRule.replace, namingRule.replaceBy, namingRule.priority)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
        delete {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val result = server.deleteNamingRule(id)
            if (result is ServerError) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, result))
                return@delete
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, result))
        }
    }
}
