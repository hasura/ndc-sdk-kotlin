package io.hasura.ndc.connector

import io.opentelemetry.api.OpenTelemetry
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.Tracer
import io.opentelemetry.context.Context
import io.opentelemetry.exporter.otlp.http.metrics.OtlpHttpMetricExporter
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import java.net.InetAddress
import java.util.concurrent.TimeUnit

object Telemetry {
    enum class Protocol {
        GRPC, HTTP_PROTOBUF
    }

    private var sdk: OpenTelemetrySdk? = null
    private var tracer: Tracer = OpenTelemetry.noop().tracerProvider.get("ndc-sdk-kotlin")

    val USER_VISIBLE_SPAN_ATTRIBUTE: Attributes = Attributes.builder()
        .put(AttributeKey.stringKey("internal.visibility"), "user")
        .build()

    fun initTelemetry(
        defaultServiceName: String = "ndc-sdk-kotlin",
        defaultEndpoint: String? = null,  // Changed to nullable with null default
        defaultProtocol: Protocol = Protocol.GRPC
    ) {
        if (isInitialized()) {
            throw IllegalStateException("Telemetry has already been initialized!")
        }

        val serviceName = System.getenv("OTEL_SERVICE_NAME") ?: defaultServiceName
        val endpoint = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") ?: defaultEndpoint

        // Only initialize if endpoint is provided
        if (endpoint.isNullOrBlank()) {
            ConnectorLogger.logger.warn("No OpenTelemetry endpoint configured - set OTEL_EXPORTER_OTLP_ENDPOINT to enable tracing")
            return
        }

        val protocol = System.getenv("OTEL_EXPORTER_OTLP_PROTOCOL")?.let {
            when (it.lowercase()) {
                "grpc" -> Protocol.GRPC
                "http/protobuf" -> Protocol.HTTP_PROTOBUF
                else -> defaultProtocol
            }
        } ?: defaultProtocol

        val spanExporter = when (protocol) {
            Protocol.GRPC -> OtlpGrpcSpanExporter.builder()
                .setEndpoint(endpoint)
                .setCompression("gzip")
                .setTimeout(30000, TimeUnit.MILLISECONDS)
                .build()
            Protocol.HTTP_PROTOBUF -> OtlpHttpSpanExporter.builder()
                .setEndpoint("$endpoint/v1/traces")
                .addHeader("Content-Type", "application/x-protobuf")
                .build()
        }

        val metricExporter = when (protocol) {
            Protocol.GRPC -> OtlpGrpcMetricExporter.builder()
                .setEndpoint(endpoint)
                .setCompression("gzip")
                .setTimeout(30000, TimeUnit.MILLISECONDS)
                .build()
            Protocol.HTTP_PROTOBUF -> OtlpHttpMetricExporter.builder()
                .setEndpoint("$endpoint/v1/metrics")
                .addHeader("Content-Type", "application/x-protobuf")
                .build()
        }

        val hostname = try {
            InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            "unknown"
        }

        val resource = Resource.getDefault()
            .merge(Resource.create(Attributes.builder()
                .put(stringKey("service.name"), serviceName)
                .put(stringKey("service.version"), System.getProperty("java.version"))
                .put(stringKey("host.name"), hostname)
                .put(stringKey("os.name"), System.getProperty("os.name"))
                .put(stringKey("os.version"), System.getProperty("os.version"))
                .put(stringKey("deployment.environment"), System.getenv("DEPLOYMENT_ENVIRONMENT") ?: "development")
                .put(stringKey("process.runtime.name"), "JVM")
                .put(stringKey("process.runtime.version"), System.getProperty("java.vm.version"))
                .put(stringKey("process.runtime.description"), System.getProperty("java.runtime.version"))
                .build()
            ))

        val tracerProvider = SdkTracerProvider.builder()
            .setResource(resource)
            .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
            .build()

        val meterProvider = SdkMeterProvider.builder()
            .setResource(resource)
            .registerMetricReader(PeriodicMetricReader.builder(metricExporter).build())
            .build()

        sdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .build()

        tracer = sdk!!.tracerProvider.get("ndc-sdk-kotlin")

        Runtime.getRuntime().addShutdownHook(Thread {
            sdk?.shutdown()
        })
    }

    private fun isInitialized(): Boolean = sdk != null

    suspend fun <T> withActiveSpan(
        name: String,
        block: suspend (Span) -> T
    ): T = withInternalActiveSpan(name, block, USER_VISIBLE_SPAN_ATTRIBUTE)

    suspend fun <T> withActiveSpan(
        name: String,
        attributes: Attributes,
        block: suspend (Span) -> T
    ): T = withInternalActiveSpan(
        name,
        block,
        Attributes.builder()
            .putAll(USER_VISIBLE_SPAN_ATTRIBUTE)
            .putAll(attributes)
            .build()
    )

    suspend fun <T> withActiveSpanContext(
        parentContext: Context,
        name: String,
        block: suspend (Span) -> T
    ): T {
        val scope = parentContext.makeCurrent()
        try {
            return withActiveSpan(name, block)
        } finally {
            scope.close()
        }
    }

    private suspend fun <T> withInternalActiveSpan(
        name: String,
        block: suspend (Span) -> T,
        attributes: Attributes
    ): T {
        val parentContext = Context.current()
        val span = tracer.spanBuilder(name)
            .setParent(parentContext)
            .setAllAttributes(attributes)
            .startSpan()

        return span.makeCurrent().use { _ ->
            try {
                val result = block(span)
                span.setStatus(StatusCode.OK)
                result
            } catch (e: Exception) {
                span.setStatus(StatusCode.ERROR)
                span.recordException(e)
                throw e
            } finally {
                try {
                    span.end()
                } catch (e: Exception) {
                    ConnectorLogger.logger.warn("Failed to export span", e)
                }
            }
        }
    }

    fun recordError(span: Span, throwable: Throwable) {
        span.setStatus(StatusCode.ERROR)
        span.recordException(throwable)
    }

    // Convenience method to record error on the current active span
    fun recordError(throwable: Throwable) {
        Span.current().let { span ->
            recordError(span, throwable)
        }
    }
}
