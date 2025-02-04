package hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class CapabilitiesResponse (
    val capabilities: Capabilities,
    val version: String
)

/**
 * Describes the features of the specification which a data connector implements.
 */
@Serializable
data class Capabilities (
    val mutation: MutationCapabilities,
    val query: QueryCapabilities,
    val relationships: RelationshipCapabilities? = null
)

@Serializable
data class MutationCapabilities (
    /**
     * Does the connector support explaining mutations
     */
    val explain: JsonObject? = null,

    /**
     * Does the connector support executing multiple mutations in a transaction.
     */
    val transactional: JsonObject? = null
)

@Serializable
data class QueryCapabilities (
    /**
     * Does the connector support aggregate queries
     */
    val aggregates: JsonObject? = null,

    /**
     * Does the connector support EXISTS predicates
     */
    val exists: ExistsCapabilities? = null,

    /**
     * Does the connector support explaining queries
     */
    val explain: JsonObject? = null,

    /**
     * Does the connector support nested fields
     */
    @SerialName("nested_fields")
    val nestedFields: NestedFieldCapabilities? = null,

    /**
     * Does the connector support queries which use variables
     */
    val variables: JsonObject? = null
)

/**
 * Does the connector support EXISTS predicates
 */
@Serializable
data class ExistsCapabilities (
    /**
     * Does the connector support ExistsInCollection::NestedCollection
     */
    @SerialName("nested_collections")
    val nestedCollections: JsonObject? = null
)

/**
 * Does the connector support nested fields
 */
@Serializable
data class NestedFieldCapabilities (
    /**
     * Does the connector support aggregating values within nested fields
     */
    val aggregates: JsonObject? = null,

    /**
     * Does the connector support filtering by values of nested fields
     */
    @SerialName("filter_by")
    val filterBy: JsonObject? = null,

    /**
     * Does the connector support ordering by values of nested fields
     */
    @SerialName("order_by")
    val orderBy: JsonObject? = null
)

@Serializable
data class RelationshipCapabilities (
    /**
     * Does the connector support ordering by an aggregated array relationship?
     */
    @SerialName("order_by_aggregate")
    val orderByAggregate: JsonObject? = null,

    /**
     * Does the connector support comparisons that involve related collections (ie. joins)?
     */
    @SerialName("relation_comparisons")
    val relationComparisons: JsonObject? = null
)
