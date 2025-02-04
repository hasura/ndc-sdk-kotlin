package hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class QueryRequest(
    /**
     * The collection being queried
     */
    val collection: String,
    
    /**
     * Arguments to be provided to the collection
     */
    val arguments: Map<String, Argument> = emptyMap(),
    
    /**
     * The query to be executed
     */
    val query: Query,
    
    /**
     * The relationships between collections involved in the entire query
     */
    @SerialName("collection_relationships")
    val collectionRelationships: Map<String, Relationship> = emptyMap(),
    
    /**
     * Variables to be used in the query
     */
    val variables: List<Map<String, JsonElement>> = emptyList()
)

@Serializable
enum class UnaryOperator {
    @SerialName("is_null")
    IS_NULL
}
