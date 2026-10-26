package com.albin.todoapp.infrastructure.ai

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

enum class ParameterType {
    STRING,
    BOOLEAN
}

data class ToolParameter(
    val name: String,
    val description: String,
    val type: ParameterType = ParameterType.STRING,
    val isRequired: Boolean = true
)

interface AgentTool {
    val name: String
    val description: String
    val parameters: List<ToolParameter>

    suspend fun execute(arguments: Map<String, Any?>): Map<String, Any?>
}

fun AgentTool.toFunctionDeclaration(): JsonObject = buildJsonObject {
    put("name", name)
    put("description", description)
    putJsonObject("parameters") {
        put("type", "object")
        putJsonObject("properties") {
            parameters.forEach { parameter ->
                putJsonObject(parameter.name) {
                    put("type", if (parameter.type == ParameterType.STRING) "string" else "boolean")
                    put("description", parameter.description)
                }
            }
        }
        val required = parameters.filter { it.isRequired }
        if (required.isNotEmpty()) {
            putJsonArray("required") { required.forEach { add(it.name) } }
        }
    }
}
