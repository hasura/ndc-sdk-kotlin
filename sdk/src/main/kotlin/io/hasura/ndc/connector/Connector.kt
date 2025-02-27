package io.hasura.ndc.connector

import io.hasura.ndc.ir.*
import io.micrometer.core.instrument.MeterRegistry
import java.nio.file.Path
import kotlinx.serialization.json.JsonArray

interface Connector<Configuration, State> {
    /**
     * Validate the configuration files provided by the user, returning a validated 'Configuration'
     */
    suspend fun parseConfiguration(configurationDir: Path): Configuration

    /**
     * Initialize the connector's in-memory state.
     * For example, any connection pools, prepared queries, or other managed resources would be allocated here.
     */
    suspend fun tryInitState(
        configuration: Configuration,
        metrics: MeterRegistry
    ): State

    /**
     * Update any metrics from the state
     */
    suspend fun fetchMetrics(configuration: Configuration, state: State) = Unit

    /**
     * Check the health of the connector.
     */
    suspend fun getHealthReadiness(configuration: Configuration, state: State) = Unit

    /**
     * Get the connector's capabilities.
     */
    fun getCapabilities(configuration: Configuration): Capabilities

    /**
     * Get the connector's schema.
     */
    suspend fun getSchema(configuration: Configuration): SchemaResponse

    /**
     * Explain a query by creating an execution plan
     */
    suspend fun queryExplain(
        configuration: Configuration,
        state: State,
        request: QueryRequest
    ): ExplainResponse

    /**
     * Explain a mutation by creating an execution plan
     */
    suspend fun mutationExplain(
        configuration: Configuration,
        state: State,
        request: MutationRequest
    ): ExplainResponse

    /**
     * Execute a mutation
     */
    suspend fun mutation(
        configuration: Configuration,
        state: State,
        request: MutationRequest
    ): MutationResponse

    /**
     * Execute a query
     */
    suspend fun query(
        configuration: Configuration,
        state: State,
        request: QueryRequest
    ): QueryResponse

    /**
     * Execute a sql query
     */
    suspend fun sql(
        configuration: Configuration,
        state: State,
        request: SQLRequest
    ): JsonArray
}
