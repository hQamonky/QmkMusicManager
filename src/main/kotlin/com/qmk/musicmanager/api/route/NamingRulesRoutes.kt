package com.qmk.musicmanager.api.route

import com.qmk.musicmanager.api.model.BasicAPIResponse
import com.qmk.musicmanager.domain.model.Music
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
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, namingRules.toString()))
        }
        post {
            val namingRule = call.receiveNullable<NamingRule>()
            if (namingRule == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val newNamingRule = server.addNamingRule(namingRule.replace, namingRule.replaceBy, namingRule.priority)
            if (newNamingRule == null) {
                call.respond(HttpStatusCode.OK, BasicAPIResponse(false, "Error while adding naming rule."))
                return@post
            }
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, namingRule.toString()))
        }
    }
    route("/api/naming-rules/{id}") {
        get {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val namingRule = server.getNamingRule(id)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(true, namingRule.toString()))
        }
        post {
            val namingRuleId = call.parameters["id"]?.toInt()
            val namingRule = call.receiveNullable<NamingRule>()
            if (namingRuleId == null || namingRule == null || namingRuleId != namingRule.id) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            val successful =
                server.editNamingRule(namingRuleId, namingRule.replace, namingRule.replaceBy, namingRule.priority)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(successful))
        }
        delete {
            val id = call.parameters["id"]?.toInt()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }
            val successful = server.deleteNamingRule(id)
            call.respond(HttpStatusCode.OK, BasicAPIResponse(successful))
        }
    }
}
