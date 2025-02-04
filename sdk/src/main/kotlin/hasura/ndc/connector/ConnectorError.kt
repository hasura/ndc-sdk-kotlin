package hasura.ndc.connector

sealed class ConnectorError(
    val statusCode: Int,
    message: String,
    val details: Map<String, Any>? = null
) : Exception(message) {
    class BadRequest(message: String, details: Map<String, Any>? = null) : 
        ConnectorError(400, message, details)
    
    class UnprocessableContent(message: String, details: Map<String, Any>? = null) : 
        ConnectorError(422, message, details)

    class InternalServerError(message: String, details: Map<String, Any>? = null) : 
        ConnectorError(500, message, details)

    class NotSupported(message: String, details: Map<String, Any>? = null) : 
        ConnectorError(501, message, details)
}