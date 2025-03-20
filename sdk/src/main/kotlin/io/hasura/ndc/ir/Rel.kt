package io.hasura.ndc.ir

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

// Assumed type aliases
typealias CollectionName = String
typealias DataConnectorColumnName = String

@Serializable
@JsonClassDiscriminator("type")
@OptIn(ExperimentalSerializationApi::class)
sealed interface Rel {
    @Serializable
    @SerialName("From")
    data class From(
        val collection: CollectionName,
        val columns: List<DataConnectorColumnName>
    ) : Rel

    @Serializable
    @SerialName("Limit")
    data class Limit(
        val input: Rel,
        val fetch: Int?, // corresponds to Option<usize>
        val skip: Int
    ) : Rel

    @Serializable
    @SerialName("Project")
    data class Project(
        val input: Rel,
        val exprs: List<RelExpression>
    ) : Rel

    @Serializable
    @SerialName("Filter")
    data class Filter(
        val input: Rel,
        val predicate: RelExpression
    ) : Rel

    @Serializable
    @SerialName("Sort")
    data class Sort(
        val input: Rel,
        val exprs: List<SortExpr>
    ) : Rel

    @Serializable
    @SerialName("Distinct")
    data class Distinct(
        val input: Rel
    ) : Rel

    @Serializable
    @SerialName("DistinctOn")
    data class DistinctOn(
        val input: Rel,
        val exprs: List<RelExpression>
    ) : Rel

    @Serializable
    @SerialName("Join")
    data class Join(
        val left: Rel,
        val right: Rel,
        val on: List<JoinOn>,
        val join_type: JoinType
    ) : Rel

    @Serializable
    @SerialName("Aggregate")
    data class Aggregate(
        val input: Rel,
        val group_by: List<RelExpression>,
        val aggregates: List<RelExpression>
    ) : Rel
}

@Serializable
@SerialName("CaseWhen")
data class CaseWhen(
    val `when`: RelExpression,
    val then: RelExpression
)

@Serializable
data class JoinOn(
    val left: RelExpression,
    val right: RelExpression
)

@Serializable
enum class JoinType {
    @SerialName("Left")
    Left,

    @SerialName("Right")
    Right,

    @SerialName("Inner")
    Inner,

    @SerialName("Full")
    Full
}

@Serializable
data class SortExpr(
    val expr: RelExpression,
    val asc: Boolean,
    val nulls_first: Boolean
)

@Serializable
@JsonClassDiscriminator("type")
@OptIn(ExperimentalSerializationApi::class)
sealed interface Literal {
    @Serializable
    @SerialName("Null")
    object Null : Literal

    @Serializable
    @SerialName("Boolean")
    data class BooleanLiteral(val value: Boolean?) : Literal

    @Serializable
    @SerialName("Float32")
    data class Float32(val value: Float?) : Literal

    @Serializable
    @SerialName("Float64")
    data class Float64(val value: Double?) : Literal

    @Serializable
    @SerialName("Int8")
    data class Int8(val value: Byte?) : Literal

    @Serializable
    @SerialName("Int16")
    data class Int16(val value: Short?) : Literal

    @Serializable
    @SerialName("Int32")
    data class Int32(val value: Int?) : Literal

    @Serializable
    @SerialName("Int64")
    data class Int64(val value: Long?) : Literal

    @Serializable
    @SerialName("UInt8")
    data class UInt8(val value: UByte?) : Literal

    @Serializable
    @SerialName("UInt16")
    data class UInt16(val value: UShort?) : Literal

    @Serializable
    @SerialName("UInt32")
    data class UInt32(val value: UInt?) : Literal

    @Serializable
    @SerialName("UInt64")
    data class UInt64(val value: ULong?) : Literal

    @Serializable
    @SerialName("Utf8")
    data class Utf8(val value: String?) : Literal

    @Serializable
    @SerialName("Date32")
    data class Date32(val value: Int?) : Literal

    @Serializable
    @SerialName("Date64")
    data class Date64(val value: Long?) : Literal

    @Serializable
    @SerialName("Time32Second")
    data class Time32Second(val value: Int?) : Literal

    @Serializable
    @SerialName("Time32Millisecond")
    data class Time32Millisecond(val value: Int?) : Literal

    @Serializable
    @SerialName("Time64Microsecond")
    data class Time64Microsecond(val value: Long?) : Literal

    @Serializable
    @SerialName("Time64Nanosecond")
    data class Time64Nanosecond(val value: Long?) : Literal

    @Serializable
    @SerialName("TimestampSecond")
    data class TimestampSecond(val value: Long?) : Literal

    @Serializable
    @SerialName("TimestampMillisecond")
    data class TimestampMillisecond(val value: Long?) : Literal

    @Serializable
    @SerialName("TimestampMicrosecond")
    data class TimestampMicrosecond(val value: Long?) : Literal

    @Serializable
    @SerialName("TimestampNanosecond")
    data class TimestampNanosecond(val value: Long?) : Literal

    @Serializable
    @SerialName("DurationSecond")
    data class DurationSecond(val value: Long?) : Literal

    @Serializable
    @SerialName("DurationMillisecond")
    data class DurationMillisecond(val value: Long?) : Literal

    @Serializable
    @SerialName("DurationMicrosecond")
    data class DurationMicrosecond(val value: Long?) : Literal

    @Serializable
    @SerialName("DurationNanosecond")
    data class DurationNanosecond(val value: Long?) : Literal
}

@Serializable
@JsonClassDiscriminator("type")
@OptIn(ExperimentalSerializationApi::class)
sealed interface RelExpression {
    @Serializable
    @SerialName("Literal")
    data class RelLiteral(val literal: Literal) : RelExpression

    @Serializable
    @SerialName("Column")
    data class Column(val index: Int) : RelExpression

    @Serializable
    @SerialName("Cast")
    data class Cast(val expr: RelExpression, val as_type: Literal) : RelExpression

    @Serializable
    @SerialName("TryCast")
    data class TryCast(val expr: RelExpression, val as_type: Literal) : RelExpression

    @Serializable
    @SerialName("Case")
    data class Case(val `when`: List<CaseWhen>, val default: RelExpression?) : RelExpression

    // Binary operators
    @Serializable
    @SerialName("And")
    data class And(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Or")
    data class Or(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Eq")
    data class Eq(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("NotEq")
    data class NotEq(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Lt")
    data class Lt(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("LtEq")
    data class LtEq(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Gt")
    data class Gt(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("GtEq")
    data class GtEq(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Plus")
    data class Plus(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Minus")
    data class Minus(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Multiply")
    data class Multiply(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Divide")
    data class Divide(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Modulo")
    data class Modulo(val left: RelExpression, val right: RelExpression) : RelExpression

    @Serializable
    @SerialName("Like")
    data class Like(val expr: RelExpression, val pattern: RelExpression) : RelExpression

    @Serializable
    @SerialName("ILike")
    data class ILike(val expr: RelExpression, val pattern: RelExpression) : RelExpression

    @Serializable
    @SerialName("NotLike")
    data class NotLike(val expr: RelExpression, val pattern: RelExpression) : RelExpression

    @Serializable
    @SerialName("NotILike")
    data class NotILike(val expr: RelExpression, val pattern: RelExpression) : RelExpression

    // Unary operators
    @Serializable
    @SerialName("Not")
    data class Not(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsNotNull")
    data class IsNotNull(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsNull")
    data class IsNull(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsTrue")
    data class IsTrue(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsFalse")
    data class IsFalse(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsUnknown")
    data class IsUnknown(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsNotTrue")
    data class IsNotTrue(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsNotFalse")
    data class IsNotFalse(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("IsNotUnknown")
    data class IsNotUnknown(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Negative")
    data class Negative(val expr: RelExpression) : RelExpression

    // Other operators
    @Serializable
    @SerialName("Between")
    data class Between(val low: RelExpression, val high: RelExpression) : RelExpression

    @Serializable
    @SerialName("NotBetween")
    data class NotBetween(val low: RelExpression, val high: RelExpression) : RelExpression

    @Serializable
    @SerialName("In")
    data class In(val expr: RelExpression, val list: List<RelExpression>) : RelExpression

    @Serializable
    @SerialName("NotIn")
    data class NotIn(val expr: RelExpression, val list: List<RelExpression>) : RelExpression

    // Scalar functions
    @Serializable
    @SerialName("ToLower")
    data class ToLower(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("ToUpper")
    data class ToUpper(val expr: RelExpression) : RelExpression

    // Aggregate functions
    @Serializable
    @SerialName("Average")
    data class Average(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("BoolAnd")
    data class BoolAnd(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("BoolOr")
    data class BoolOr(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Count")
    data class Count(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("FirstValue")
    data class FirstValue(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("LastValue")
    data class LastValue(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Max")
    data class Max(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Mean")
    data class Mean(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Median")
    data class Median(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Min")
    data class Min(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("StringAgg")
    data class StringAgg(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Sum")
    data class Sum(val expr: RelExpression) : RelExpression

    @Serializable
    @SerialName("Var")
    data class Var(val expr: RelExpression) : RelExpression
}

// Sent by V3 engine as the input to /query/rel
@Serializable
data class QueryRel(
    val rel: Rel,
)
