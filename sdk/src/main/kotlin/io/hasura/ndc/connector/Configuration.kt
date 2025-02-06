package io.hasura.ndc.connector

import kotlinx.cli.*

/*
 * Server Options
 */
data class ServerOptions(
    val configuration: String,
    val host: String = "localhost",
    val port: Int = 8080,
    val serviceTokenSecret: String? = null,
    val logLevel: String = "info",
    val prettyPrintLogs: Boolean = false
) {
    companion object {
        fun fromArgs(args: Array<String>): ServerOptions {
            val parser = ArgParser("ndc-connector")

            // 1. Configuration: Required - must come from either env or args
            val configuration by parser.option(
                ArgType.String,
                fullName = "configuration",
                description = "Configuration directory"
            )

            // 2. Host: Optional - defaults to "::"
            val host by parser.option(
                ArgType.String,
                fullName = "host",
                description = "Host to bind to"
            )

            // 3. Port: Optional - defaults to 8080
            val port by parser.option(
                ArgType.Int,
                fullName = "port",
                description = "Port to listen on"
            )

            // 4. Service Token Secret: Optional - defaults to null
            val serviceTokenSecret by parser.option(
                ArgType.String,
                fullName = "service-token-secret",
                description = "Service token secret"
            )

            // 5. Log Level: Optional - defaults to "info"
            val logLevel by parser.option(
                ArgType.String,
                fullName = "log-level",
                description = "Log level"
            )

            // 6. Pretty Print Logs: Optional - defaults to false
            val prettyPrintLogs by parser.option(
                ArgType.Boolean,
                fullName = "pretty-print-logs",
                description = "Pretty print logs"
            )

            try {
                parser.parse(args)
            } catch (e: IllegalStateException) {
                System.err.println("\nError parsing command line arguments:")
                System.err.println(e.message)
                System.err.println("\nUsage:")
                System.exit(1)
            }

            try {
                val finalConfiguration = System.getenv("HASURA_CONFIGURATION_DIRECTORY")
                    ?: configuration
                    ?: throw ConfigurationException("""
                        |Configuration directory not specified
                        |
                        |Please provide one of:
                        |  - Environment variable: HASURA_CONFIGURATION_DIRECTORY
                        |  - Command line argument: --configuration
                        |
                        |Example:
                        |  Environment: export HASURA_CONFIGURATION_DIRECTORY=/path/to/config
                        |  Command line: --configuration /path/to/config
                        """.trimMargin())

                val finalHost = System.getenv("HASURA_CONNECTOR_HOST")
                    ?: host
                    ?: "localhost"

                val finalPort = System.getenv("HASURA_CONNECTOR_PORT")?.toIntOrNull()
                    ?: port
                    ?: 8080

                val finalServiceTokenSecret = System.getenv("HASURA_SERVICE_TOKEN_SECRET")
                    ?: serviceTokenSecret

                val finalLogLevel = System.getenv("HASURA_LOG_LEVEL")
                    ?: logLevel
                    ?: "info"

                val finalPrettyPrintLogs = System.getenv("HASURA_PRETTY_PRINT_LOGS")?.toBoolean()
                    ?: prettyPrintLogs
                    ?: false

                return ServerOptions(
                    configuration = finalConfiguration,
                    host = finalHost,
                    port = finalPort,
                    serviceTokenSecret = finalServiceTokenSecret?.takeIf { it.isNotEmpty() },
                    logLevel = finalLogLevel,
                    prettyPrintLogs = finalPrettyPrintLogs
                )
            } catch (e: ConfigurationException) {
                System.err.println("\nConfiguration Error:")
                System.err.println(e.message)
                System.exit(1)
                throw e
            }
        }
    }
}

class ConfigurationException(message: String) : Exception(message)
