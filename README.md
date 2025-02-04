# Hasura NDC SDK for Kotlin

This repository provides a Kotlin library to aid development of [Hasura Native Data
Connectors](https://hasura.github.io/ndc-spec/). Developers can implement an
interface, and create an executable which can be used to run a connector that is
compatible with the specification.

In addition, this library adopts certain conventions which are not covered by
the current specification:

- Connector configuration
- State management
- Trace collection
- Metrics collection

#### Getting Started with the SDK

```sh
./gradlew :sdk:build
```

#### Run the example connector

```sh
./gradlew run :example:run --args="--configuration ./example"
```

Inspect the resulting (example) schema:

```sh
curl http://localhost:8080/schema
```

(The default port, 8080, can be changed using `--port`.)

## Tracing

The serve command emits OTLP trace information. This can be used to see details
of requests across services.

To enable tracing you must:

- use the SDK option `--otlp-endpoint` e.g. `http://localhost:4317`,
- set the SDK environment variable `OTEL_EXPORTER_OTLP_ENDPOINT`, or
- set the `tracing` environment variable `OTEL_EXPORTER_OTLP_TRACES_ENDPOINT`.

The exporter uses gRPC protocol by default. To use HTTP protocol you must set `OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf`.

For additional service information you can:

- Set `OTEL_SERVICE_NAME` e.g. `ndc-sdk-kotlin`
- Set `DEPLOYMENT_ENVIRONMENT` e.g. `development`, `staging`, `production`

To view trace information during local development you can run a Jaeger server via Docker:

```sh
docker run --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one
```
