package io.hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class MutationResponse (
    /**
     * The results of each mutation operation, in the same order as they were received
     */
    @SerialName("operation_results")
    val operationResults: List<MutationOperationResults>
)

@Serializable
sealed class MutationOperationResults {
    abstract val type: String

    @Serializable
    @SerialName("procedure")
    data class Procedure(
        override val type: String = "procedure",
        val result: JsonElement
    ) : MutationOperationResults()
}
