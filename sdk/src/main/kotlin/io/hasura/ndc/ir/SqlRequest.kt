package io.hasura.ndc.ir

import kotlinx.serialization.Serializable

@Serializable
data class SQLRequest(
    val sql: String
)
