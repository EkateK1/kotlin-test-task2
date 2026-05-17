package app.task2.dto.responses

data class ApiErrorResponse(val status: Int,
                            val error: String,
                            val message: String?)