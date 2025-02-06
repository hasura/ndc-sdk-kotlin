package io.hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class MutationRequest(
    /**
     * The mutation operations to perform
     */
    val operations: List<MutationOperation>,

    /**
     * The relationships between collections involved in the entire mutation request
     */
    @SerialName("collection_relationships")
    val collectionRelationships: Map<String, Relationship>
)

@Serializable
sealed class MutationOperation {
    @Serializable
    @SerialName("procedure")
    data class Procedure(
        /**
         * The name of a procedure
         */
        val name: String,

        /**
         * Any named procedure arguments
         */
        val arguments: JsonObject,

        /**
         * The fields to return from the result, or null to return everything
         */
        val fields: NestedField? = null
    ) : MutationOperation()
}

@Serializable
sealed class NestedField {
    @Serializable
    @SerialName("object")
    data class NestedObject(
        val fields: Map<String, Field>
    ) : NestedField()

    @Serializable
    @SerialName("array")
    data class NestedArray(
        val fields: NestedField
    ) : NestedField()
}
