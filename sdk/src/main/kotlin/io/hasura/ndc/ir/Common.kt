package io.hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

const val VERSION = "0.1.6"

@Serializable
data class Query(
    /**
     * Aggregate fields of the query
     */
    val aggregates: Map<String, Aggregate>? = null,

    /**
     * Fields of the query
     */
    val fields: Map<String, Field>? = null,

    /**
     * Optionally limit to N results
     */
    val limit: UInt? = null,

    /**
     * Optionally offset from the Nth result
     */
    val offset: UInt? = null,

    @SerialName("order_by")
    val orderBy: OrderBy? = null,

    val predicate: Expression? = null
)

@Serializable
sealed class Aggregate {
    @Serializable
    @SerialName("column_count")
    data class ColumnCount(
        /**
         * The column to apply the count aggregate function to
         */
        val column: String,

        /**
         * Path to a nested field within an object column
         */
        @SerialName("field_path")
        val fieldPath: List<String>? = null,

        /**
         * Whether or not only distinct items should be counted
         */
        val distinct: Boolean
    ) : Aggregate()

    @Serializable
    @SerialName("single_column")
    data class SingleColumn(
        /**
         * The column to apply the aggregation function to
         */
        val column: String,

        /**
         * Path to a nested field within an object column
         */
        @SerialName("field_path")
        val fieldPath: List<String>? = null,

        /**
         * Single column aggregate function name.
         */
        val function: String
    ) : Aggregate()

    @Serializable
    @SerialName("star_count")
    object StarCount : Aggregate()
}

@Serializable
data class OrderBy(
    /**
     * The elements to order by, in priority order
     */
    val elements: List<OrderByElement>
)

@Serializable
data class OrderByElement(
    @SerialName("order_direction")
    val orderDirection: OrderDirection,
    val target: OrderByTarget
)

@Serializable
enum class OrderDirection {
    @SerialName("asc")
    ASC,
    @SerialName("desc")
    DESC
}

@Serializable
sealed class OrderByTarget {
    @Serializable
    @SerialName("column")
    data class Column(
        /**
         * The name of the column
         */
        val name: String,

        /**
         * Path to a nested field within an object column
         */
        @SerialName("field_path")
        val fieldPath: List<String>? = null,

        /**
         * Any relationships to traverse to reach this column
         */
        val path: List<PathElement>
    ) : OrderByTarget()

    @Serializable
    @SerialName("single_column_aggregate")
    data class SingleColumnAggregate(
        /**
         * The column to apply the aggregation function to
         */
        val column: String,

        /**
         * Path to a nested field within an object column
         */
        @SerialName("field_path")
        val fieldPath: List<String>? = null,

        /**
         * Single column aggregate function name.
         */
        val function: String,

        /**
         * Non-empty collection of relationships to traverse
         */
        val path: List<PathElement>
    ) : OrderByTarget()

    @Serializable
    @SerialName("star_count_aggregate")
    data class StarCountAggregate(
        /**
         * Non-empty collection of relationships to traverse
         */
        val path: List<PathElement>
    ) : OrderByTarget()
}

@Serializable
data class PathElement(
    /**
     * The name of the relationship to follow
     */
    val relationship: String,

    /**
     * Values to be provided to any collection arguments
     */
    val arguments: Map<String, RelationshipArgument>,

    /**
     * A predicate expression to apply to the target collection
     */
    val predicate: Expression? = null
)

@Serializable
sealed class RelationshipArgument {
    /**
     * The argument is provided by reference to a variable
     */
    @Serializable
    @SerialName("variable")
    data class Variable(
        val name: String
    ) : RelationshipArgument()

    /**
     * The argument is provided as a literal value
     */
    @Serializable
    @SerialName("literal")
    data class Literal(
        val value: JsonElement
    ) : RelationshipArgument()

    @Serializable
    @SerialName("column")
    data class Column(
        val name: String
    ) : RelationshipArgument()
}

@Serializable
sealed class Expression {
    @Serializable
    @SerialName("and")
    data class And(
        val expressions: List<Expression>
    ) : Expression()

    @Serializable
    @SerialName("or")
    data class Or(
        val expressions: List<Expression>
    ) : Expression()

    @Serializable
    @SerialName("not")
    data class Not(
        val expression: Expression
    ) : Expression()

    @Serializable
    @SerialName("unary_comparison_operator")
    data class UnaryComparisonOperator(
        val column: ComparisonTarget,
        val operator: UnaryComparisonOperatorType
    ) : Expression()

    @Serializable
    @SerialName("binary_comparison_operator")
    data class BinaryComparisonOperator(
        val column: ComparisonTarget,
        val operator: String,
        val value: ComparisonValue
    ) : Expression()

    @Serializable
    @SerialName("exists")
    data class Exists(
        @SerialName("in_collection")
        val inCollection: ExistsInCollection,
        val predicate: Expression? = null
    ) : Expression()
}

@Serializable
sealed class ComparisonTarget {
    @Serializable
    @SerialName("column")
    data class Column(
        /**
         * The name of the column
         */
        val name: String,

        /**
         * Path to a nested field within an object column
         */
        @SerialName("field_path")
        val fieldPath: List<String>? = null,

        /**
         * Any relationships to traverse to reach this column
         */
        val path: List<PathElement>
    ) : ComparisonTarget()

    @Serializable
    @SerialName("root_collection_column")
    data class RootCollectionColumn(
        /**
         * The name of the column
         */
        val name: String,

        /**
         * Path to a nested field within an object column
         */
        @SerialName("field_path")
        val fieldPath: List<String>? = null
    ) : ComparisonTarget()
}

@Serializable
enum class UnaryComparisonOperatorType {
    @SerialName("is_null")
    IS_NULL
}

@Serializable
sealed class ComparisonValue {
    @Serializable
    @SerialName("column")
    data class Column(
        val column: ComparisonTarget
    ) : ComparisonValue()

    @Serializable
    @SerialName("scalar")
    data class Scalar(
        val value: JsonElement
    ) : ComparisonValue()

    @Serializable
    @SerialName("variable")
    data class Variable(
        val name: String
    ) : ComparisonValue()
}

@Serializable
sealed class ExistsInCollection {
    @Serializable
    @SerialName("related")
    data class Related(
        val relationship: String,
        /**
         * Values to be provided to any collection arguments
         */
        val arguments: Map<String, RelationshipArgument>
    ) : ExistsInCollection()

    @Serializable
    @SerialName("unrelated")
    data class Unrelated(
        /**
         * The name of a collection
         */
        val collection: String,
        /**
         * Values to be provided to any collection arguments
         */
        val arguments: Map<String, RelationshipArgument>
    ) : ExistsInCollection()

    @Serializable
    @SerialName("nested_collection")
    data class NestedCollection(
        @SerialName("column_name")
        val columnName: String,
        val arguments: Map<String, Argument>? = null,
        /**
         * Path to a nested collection via object columns
         */
        @SerialName("field_path")
        val fieldPath: List<String>? = null
    ) : ExistsInCollection()
}

@Serializable
sealed class Field {
    @Serializable
    @SerialName("column")
    data class Column(
        val column: String,

        /**
         * When the type of the column is a (possibly-nullable) array or object,
         * the caller can request a subset of the complete column data, by specifying
         * fields to fetch here. If omitted, the column data will be fetched in full.
         */
        val fields: NestedField? = null,
        val arguments: Map<String, Argument>? = null
    ) : Field()

    @Serializable
    @SerialName("relationship")
    data class Relationship(
        val query: Query,

        /**
         * The name of the relationship to follow for the subquery
         */
        val relationship: String,

        /**
         * Values to be provided to any collection arguments
         */
        val arguments: Map<String, RelationshipArgument>
    ) : Field()
}

@Serializable
sealed class Argument {
    /**
     * The argument is provided by reference to a variable
     */
    @Serializable
    @SerialName("variable")
    data class Variable(
        val name: String
    ) : Argument()

    /**
     * The argument is provided as a literal value
     */
    @Serializable
    @SerialName("literal")
    data class Literal(
        val value: JsonElement
    ) : Argument()
}

@Serializable
data class Relationship(
    /**
     * A mapping between columns on the source collection to columns on the target collection
     */
    @SerialName("column_mapping")
    val columnMapping: Map<String, String>,

    @SerialName("relationship_type")
    val relationshipType: RelationshipType,

    /**
     * The name of a collection
     */
    @SerialName("target_collection")
    val targetCollection: String,

    /**
     * Values to be provided to any collection arguments
     */
    val arguments: Map<String, RelationshipArgument>
)

@Serializable
enum class RelationshipType {
    @SerialName("object")
    OBJECT,
    @SerialName("array")
    ARRAY
}
