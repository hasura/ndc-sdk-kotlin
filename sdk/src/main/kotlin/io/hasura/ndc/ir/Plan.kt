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
sealed interface Plan {
    @Serializable
    @SerialName("From")
    data class From(
        val collection: CollectionName,
        val columns: List<DataConnectorColumnName>
    ) : Plan

    @Serializable
    @SerialName("Limit")
    data class Limit(
        val input: Plan,
        val fetch: Int?, // corresponds to Option<usize>
        val skip: Int
    ) : Plan

    @Serializable
    @SerialName("Project")
    data class Project(
        val input: Plan,
        val exprs: List<PlanExpression>
    ) : Plan

    @Serializable
    @SerialName("Filter")
    data class Filter(
        val input: Plan,
        val predicate: PlanExpression
    ) : Plan

    @Serializable
    @SerialName("Sort")
    data class Sort(
        val input: Plan,
        val exprs: List<SortExpr>
    ) : Plan

    @Serializable
    @SerialName("Distinct")
    data class Distinct(
        val input: Plan
    ) : Plan

    @Serializable
    @SerialName("DistinctOn")
    data class DistinctOn(
        val input: Plan,
        val exprs: List<PlanExpression>
    ) : Plan

    @Serializable
    @SerialName("Join")
    data class Join(
        val left: Plan,
        val right: Plan,
        val on: List<JoinOn>,
        val join_type: JoinType
    ) : Plan

    @Serializable
    @SerialName("Aggregate")
    data class Aggregate(
        val input: Plan,
        val group_by: List<PlanExpression>,
        val aggregates: List<PlanExpression>
    ) : Plan
}

@Serializable
data class JoinOn(
    val left: PlanExpression,
    val right: PlanExpression
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
    val expr: PlanExpression,
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
sealed interface PlanExpression {
    @Serializable
    @SerialName("Literal")
    data class PlanLiteral(val literal: Literal) : PlanExpression

    @Serializable
    @SerialName("Column")
    data class Column(val index: Int) : PlanExpression

    // Binary operators
    @Serializable
    @SerialName("And")
    data class And(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Or")
    data class Or(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Eq")
    data class Eq(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("NotEq")
    data class NotEq(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Lt")
    data class Lt(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("LtEq")
    data class LtEq(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Gt")
    data class Gt(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("GtEq")
    data class GtEq(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Plus")
    data class Plus(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Minus")
    data class Minus(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Multiply")
    data class Multiply(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Divide")
    data class Divide(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Modulo")
    data class Modulo(val left: PlanExpression, val right: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Like")
    data class Like(val expr: PlanExpression, val pattern: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("ILike")
    data class ILike(val expr: PlanExpression, val pattern: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("NotLike")
    data class NotLike(val expr: PlanExpression, val pattern: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("NotILike")
    data class NotILike(val expr: PlanExpression, val pattern: PlanExpression) : PlanExpression

    // Unary operators
    @Serializable
    @SerialName("Not")
    data class Not(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsNotNull")
    data class IsNotNull(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsNull")
    data class IsNull(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsTrue")
    data class IsTrue(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsFalse")
    data class IsFalse(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsUnknown")
    data class IsUnknown(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsNotTrue")
    data class IsNotTrue(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsNotFalse")
    data class IsNotFalse(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("IsNotUnknown")
    data class IsNotUnknown(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Negative")
    data class Negative(val expr: PlanExpression) : PlanExpression

    // Other operators
    @Serializable
    @SerialName("Between")
    data class Between(val low: PlanExpression, val high: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("NotBetween")
    data class NotBetween(val low: PlanExpression, val high: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("In")
    data class In(val expr: PlanExpression, val list: List<PlanExpression>) : PlanExpression

    @Serializable
    @SerialName("NotIn")
    data class NotIn(val expr: PlanExpression, val list: List<PlanExpression>) : PlanExpression

    // Scalar functions
    @Serializable
    @SerialName("ToLower")
    data class ToLower(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("ToUpper")
    data class ToUpper(val expr: PlanExpression) : PlanExpression

    // Aggregate functions
    @Serializable
    @SerialName("Average")
    data class Average(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("BoolAnd")
    data class BoolAnd(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("BoolOr")
    data class BoolOr(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Count")
    data class Count(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("FirstValue")
    data class FirstValue(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("LastValue")
    data class LastValue(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Max")
    data class Max(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Mean")
    data class Mean(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Median")
    data class Median(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Min")
    data class Min(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("StringAgg")
    data class StringAgg(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Sum")
    data class Sum(val expr: PlanExpression) : PlanExpression

    @Serializable
    @SerialName("Var")
    data class Var(val expr: PlanExpression) : PlanExpression
}

// Sent by V3 engine as the input to /sql
@Serializable
data class SQLPlan(
    val plan: Plan,
)