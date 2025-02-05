package io.hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class ErrorResponse (
    /**
     * Any additional structured information about the error
     */
    val details: JsonElement?,

    /**
     * A human-readable summary of the error
     */
    val message: String
)
