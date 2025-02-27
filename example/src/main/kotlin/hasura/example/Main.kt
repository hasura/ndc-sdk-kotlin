package hasura.example

import io.hasura.ndc.connector.*
import io.hasura.ndc.ir.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.json.*
import java.nio.file.Path

fun main(args: Array<String>) {
    startServer(ExampleConnector(), args)
}

class ExampleConnector : Connector<Unit, Unit> {
    // Hardcoded users data
    private val users = listOf(
        mapOf(
            "id" to JsonPrimitive(1),
            "name" to JsonPrimitive("Alice"),
            "email" to JsonPrimitive("alice@example.com")
        ),
        mapOf(
            "id" to JsonPrimitive(2),
            "name" to JsonPrimitive("Bob"),
            "email" to JsonPrimitive("bob@example.com")
        )
    )

    override suspend fun parseConfiguration(configurationDir: Path) = Unit

    override suspend fun tryInitState(configuration: Unit, metrics: MeterRegistry) = Unit

    override fun getCapabilities(configuration: Unit) = Capabilities(
        query = QueryCapabilities(),
        mutation = MutationCapabilities()
    )

    override suspend fun getSchema(configuration: Unit) = SchemaResponse(
        scalarTypes = mapOf(
            "Int" to ScalarType(
                aggregateFunctions = emptyMap(),
                comparisonOperators = emptyMap()
            ),
            "String" to ScalarType(
                aggregateFunctions = emptyMap(),
                comparisonOperators = emptyMap()
            )
        ),
        objectTypes = mapOf(
            "user" to ObjectType(
                description = "A user in the system",
                fields = mapOf(
                    "id" to ObjectField(type = Type.Named(name = "Int")),
                    "name" to ObjectField(type = Type.Named(name = "String")),
                    "email" to ObjectField(type = Type.Named(name = "String"))
                )
            )
        ),
        collections = listOf(
            CollectionInfo(
                name = "users",
                description = "List of users",
                arguments = emptyMap(),
                type = "user",
                uniquenessConstraints = mapOf(
                    "id" to UniquenessConstraint(uniqueColumns = listOf("id"))
                ),
                foreignKeys = emptyMap()
            )
        ),
        functions = emptyList(),
        procedures = emptyList()
    )

    override suspend fun queryExplain(
        configuration: Unit,
        state: Unit,
        request: QueryRequest
    ) = ExplainResponse(mapOf("plan" to "Fetch all users from in-memory list"))

    override suspend fun query(
        configuration: Unit,
        state: Unit,
        request: QueryRequest
    ): QueryResponse {
        // For this example, we'll just return all users
        return QueryResponse(
            rowSets = listOf(
                RowSet(rows = users)
            )
        )
    }

    override suspend fun mutationExplain(
        configuration: Unit,
        state: Unit,
        request: MutationRequest
    ) = ExplainResponse(mapOf("plan" to "Mutations not supported"))

    override suspend fun mutation(
        configuration: Unit,
        state: Unit,
        request: MutationRequest
    ): MutationResponse {
        throw ConnectorError.NotSupported("Mutations are not supported")
    }

    override suspend fun sql(
      configuration: Unit,
      state: Unit,
      request: SQLRequest
    ): JsonArray {
        throw ConnectorError.NotSupported("SQL is not supported")
    }
}
