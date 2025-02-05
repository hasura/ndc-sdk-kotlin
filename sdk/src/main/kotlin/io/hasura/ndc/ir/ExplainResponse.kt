package io.hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class ExplainResponse (
    /**
     * A list of human-readable key-value pairs describing a query execution plan. For example,
     * a connector for a relational database might return the generated SQL and/or the output of
     * the `EXPLAIN` command. An API-based connector might encode a list of statically-known API
     * calls which would be made.
     */
    val details: Map<String, String>
)
