package io.hasura.ndc.ir

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

val json = Json {
    classDiscriminator = "_type"
}

@Serializable
data class SchemaResponse (
    /**
     * Collections which are available for queries
     */
    val collections: List<CollectionInfo>,

    /**
     * Functions (i.e. collections which return a single column and row)
     */
    val functions: List<FunctionInfo>,

    /**
     * A list of object types which can be used as the types of arguments, or return types of
     * procedures. Names should not overlap with scalar type names.
     */
    @SerialName("object_types")
    val objectTypes: Map<String, ObjectType>,

    /**
     * Procedures which are available for execution as part of mutations
     */
    val procedures: List<ProcedureInfo>,

    /**
     * A list of scalar types which will be used as the types of collection columns
     */
    @SerialName("scalar_types")
    val scalarTypes: Map<String, ScalarType>
)

@Serializable
data class CollectionInfo (
    /**
     * Any arguments that this collection requires
     */
    val arguments: Map<String, ArgumentInfo>,

    /**
     * Description of the collection
     */
    val description: String? = null,

    /**
     * Any foreign key constraints enforced on this collection
     */
    @SerialName("foreign_keys")
    val foreignKeys: Map<String, ForeignKeyConstraint>,

    /**
     * The name of the collection
     *
     * Note: these names are abstract - there is no requirement that this name correspond to the
     * name of an actual collection in the database.
     */
    val name: String,

    /**
     * The name of the collection's object type
     */
    val type: String,

    /**
     * Any uniqueness constraints enforced on this collection
     */
    @SerialName("uniqueness_constraints")
    val uniquenessConstraints: Map<String, UniquenessConstraint>
)

@Serializable
data class ArgumentInfo (
    /**
     * Argument description
     */
    val description: String? = null,

    /**
     * The name of the type of this argument
     */
    val type: Type
)

@Serializable(with = TypeSerializer::class)
sealed class Type {
    @Serializable
    data class Named(
        val type: String = "named",
        val name: String
    ) : Type()

    @Serializable
    data class Nullable(
        val type: String = "nullable",
        @SerialName("underlying_type")
        val underlyingType: Type
    ) : Type()

    @Serializable
    data class Array(
        val type: String = "array",
        @SerialName("element_type")
        val elementType: Type
    ) : Type()

    @Serializable
    data class Predicate(
        val type: String = "predicate",
        @SerialName("object_type_name")
        val objectTypeName: String
    ) : Type()
}

object TypeSerializer : KSerializer<Type> {
    override val descriptor = buildClassSerialDescriptor("Type")

    override fun serialize(encoder: Encoder, value: Type) {
        val jsonEncoder = encoder as JsonEncoder
        val jsonElement = when (value) {
            is Type.Named -> buildJsonObject {
                put("type", "named")
                put("name", value.name)
            }
            is Type.Nullable -> buildJsonObject {
                put("type", "nullable")
                put("underlying_type", Json.encodeToJsonElement(value.underlyingType))
            }
            is Type.Array -> buildJsonObject {
                put("type", "array")
                put("element_type", Json.encodeToJsonElement(value.elementType))
            }
            is Type.Predicate -> buildJsonObject {
                put("type", "predicate")
                put("object_type_name", value.objectTypeName)
            }
        }
        jsonEncoder.encodeJsonElement(jsonElement)
    }

    override fun deserialize(decoder: Decoder): Type {
        val jsonDecoder = decoder as JsonDecoder
        val element = jsonDecoder.decodeJsonElement()
        val json = element.jsonObject
        return when (json["type"]?.jsonPrimitive?.content) {
            "named" -> Type.Named(
                name = json["name"]!!.jsonPrimitive.content
            )
            "nullable" -> Type.Nullable(
                underlyingType = Json.decodeFromJsonElement(json["underlying_type"]!!)
            )
            "array" -> Type.Array(
                elementType = Json.decodeFromJsonElement(json["element_type"]!!)
            )
            "predicate" -> Type.Predicate(
                objectTypeName = json["object_type_name"]!!.jsonPrimitive.content
            )
            else -> throw SerializationException("Unknown type")
        }
    }
}

@Serializable
data class ForeignKeyConstraint (
    /**
     * The columns on which you want want to define the foreign key.
     */
    @SerialName("column_mapping")
    val columnMapping: Map<String, String>,

    /**
     * The name of a collection
     */
    @SerialName("foreign_collection")
    val foreignCollection: String
)

@Serializable
data class UniquenessConstraint (
    /**
     * A list of columns which this constraint requires to be unique
     */
    @SerialName("unique_columns")
    val uniqueColumns: List<String>
)

@Serializable
data class FunctionInfo (
    /**
     * Any arguments that this collection requires
     */
    val arguments: Map<String, ArgumentInfo>,

    /**
     * Description of the function
     */
    val description: String? = null,

    /**
     * The name of the function
     */
    val name: String,

    /**
     * The name of the function's result type
     */
    @SerialName("result_type")
    val resultType: Type
)

/**
 * The definition of an object type
 */
@Serializable
data class ObjectType (
    /**
     * Description of this type
     */
    val description: String? = null,

    /**
     * Fields defined on this object type
     */
    val fields: Map<String, ObjectField>
)

/**
 * The definition of an object field
 */
@Serializable
data class ObjectField (
    /**
     * The arguments available to the field - Matches implementation from CollectionInfo
     */
    val arguments: Map<String, ArgumentInfo>? = null,

    /**
     * Description of this field
     */
    val description: String? = null,

    /**
     * The type of this field
     */
    val type: Type
)

@Serializable
data class ProcedureInfo (
    /**
     * Any arguments that this collection requires
     */
    val arguments: Map<String, ArgumentInfo>,

    /**
     * Column description
     */
    val description: String? = null,

    /**
     * The name of the procedure
     */
    val name: String,

    /**
     * The name of the result type
     */
    @SerialName("result_type")
    val resultType: Type
)

/**
 * The definition of a scalar type, i.e. types that can be used as the types of columns.
 */
@Serializable
data class ScalarType (
    /**
     * A map from aggregate function names to their definitions. Result type names must be
     * defined scalar types declared in ScalarTypesCapabilities.
     */
    @SerialName("aggregate_functions")
    val aggregateFunctions: Map<String, AggregateFunctionDefinition>,

    /**
     * A map from comparison operator names to their definitions. Argument type names must be
     * defined scalar types declared in ScalarTypesCapabilities.
     */
    @SerialName("comparison_operators")
    val comparisonOperators: Map<String, ComparisonOperatorDefinition>,

    /**
     * A description of valid values for this scalar type. Defaults to
     * `TypeRepresentation::JSON` if omitted
     */
    val representation: TypeRepresentation? = null
)

/**
 * The definition of an aggregation function on a scalar type
 */
@Serializable
data class AggregateFunctionDefinition (
    /**
     * The scalar or object type of the result of this function
     */
    @SerialName("result_type")
    val resultType: Type
)

/**
 * The definition of a comparison operator on a scalar type
 */
@Serializable
data class ComparisonOperatorDefinition (
    val type: ComparisonOperatorDefinitionType,

    /**
     * The type of the argument to this operator
     */
    @SerialName("argument_type")
    val argumentType: Type? = null
)

@Serializable
enum class ComparisonOperatorDefinitionType(val value: String) {
    @SerialName("custom") Custom("custom"),
    @SerialName("equal") Equal("equal"),
    @SerialName("in") In("in");
}

/**
 * JSON booleans
 *
 * Any JSON string
 *
 * Any JSON number
 *
 * Any JSON number, with no decimal part
 *
 * A 8-bit signed integer with a minimum value of -2^7 and a maximum value of 2^7 - 1
 *
 * A 16-bit signed integer with a minimum value of -2^15 and a maximum value of 2^15 - 1
 *
 * A 32-bit signed integer with a minimum value of -2^31 and a maximum value of 2^31 - 1
 *
 * A 64-bit signed integer with a minimum value of -2^63 and a maximum value of 2^63 - 1
 *
 * An IEEE-754 single-precision floating-point number
 *
 * An IEEE-754 double-precision floating-point number
 *
 * Arbitrary-precision integer string
 *
 * Arbitrary-precision decimal string
 *
 * UUID string (8-4-4-4-12)
 *
 * ISO 8601 date
 *
 * ISO 8601 timestamp
 *
 * ISO 8601 timestamp-with-timezone
 *
 * GeoJSON, per RFC 7946
 *
 * GeoJSON Geometry object, per RFC 7946
 *
 * Base64-encoded bytes
 *
 * Arbitrary JSON
 *
 * One of the specified string values
 */
@Serializable
data class TypeRepresentation (
    val type: RepresentationType,

    @SerialName("one_of")
    val oneOf: List<String>? = null
)

@Serializable
enum class RepresentationType(val value: String) {
    @SerialName("bigdecimal") Bigdecimal("bigdecimal"),
    @SerialName("biginteger") Biginteger("biginteger"),
    @SerialName("bytes") Bytes("bytes"),
    @SerialName("date") Date("date"),
    @SerialName("float32") Float32("float32"),
    @SerialName("float64") Float64("float64"),
    @SerialName("geography") Geography("geography"),
    @SerialName("geometry") Geometry("geometry"),
    @SerialName("int16") Int16("int16"),
    @SerialName("int32") Int32("int32"),
    @SerialName("int64") Int64("int64"),
    @SerialName("int8") Int8("int8"),
    @SerialName("integer") Integer("integer"),
    @SerialName("json") JSON("json"),
    @SerialName("number") Number("number"),
    @SerialName("timestamp") Timestamp("timestamp"),
    @SerialName("timestamptz") Timestamptz("timestamptz"),
    @SerialName("boolean") TypeBoolean("boolean"),
    @SerialName("enum") TypeEnum("enum"),
    @SerialName("string") TypeString("string"),
    @SerialName("uuid") UUID("uuid");
}
