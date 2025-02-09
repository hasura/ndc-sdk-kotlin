package io.hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Query responses may return multiple RowSets when using queries with variables. Else,
 * there should always be exactly one RowSet
 */
@Serializable(with = QueryResponseSerializer::class)
data class QueryResponse(
    val rowSets: List<RowSet>
)

private class QueryResponseSerializer : KSerializer<QueryResponse> {
    private val listSerializer = ListSerializer(RowSet.serializer())

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: QueryResponse) {
        listSerializer.serialize(encoder, value.rowSets)
    }

    override fun deserialize(decoder: Decoder): QueryResponse {
        return QueryResponse(listSerializer.deserialize(decoder))
    }
}

@Serializable
data class RowSet(
    /**
     * The results of the aggregates returned by the query
     */
    val aggregates: JsonObject? = null,

    /**
     * The rows returned by the query, corresponding to the query's fields
     */
    val rows: List<Map<String, JsonElement>>? = null
)

@Serializable
@JvmInline
value class RowFieldValue(
    val value: JsonElement
)
