package app.task2.controllers

import app.task2.dto.responses.ApiErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.InvalidDataAccessApiUsageException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException

@RestControllerAdvice
class ExceptionController {
    private val log = LoggerFactory.getLogger(ExceptionController::class.java)

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatus(ex: ResponseStatusException): ResponseEntity<ApiErrorResponse> {
        val status = HttpStatus.valueOf(ex.statusCode.value())
        log.error("Exception", ex)
        return ResponseEntity
            .status(status)
            .body(ApiErrorResponse(status.value(), status.reasonPhrase, ex.reason))
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        MissingServletRequestParameterException::class,
        HttpMessageNotReadableException::class,
        MethodArgumentNotValidException::class,
        IllegalArgumentException::class,
    )
    fun handleBadRequest(ex: Exception): ResponseEntity<ApiErrorResponse> {
        log.warn("Bad request: {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.reasonPhrase, ex.message))
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException::class)
    fun handleInvalidDataAccess(ex: InvalidDataAccessApiUsageException): ResponseEntity<ApiErrorResponse> {
        log.warn("Bad request (data access): {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.reasonPhrase, ex.message))
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrity(ex: DataIntegrityViolationException): ResponseEntity<ApiErrorResponse> {
        log.warn("Conflict (data integrity): {}", ex.message)
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiErrorResponse(HttpStatus.CONFLICT.value(), HttpStatus.CONFLICT.reasonPhrase, ex.rootCause?.message ?: ex.message))
    }

    @ExceptionHandler(
        NoResourceFoundException::class,
        NoHandlerFoundException::class,
    )
    fun handleNotFound(ex: Exception): ResponseEntity<ApiErrorResponse> {
        log.error("Not found exception", ex)
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiErrorResponse(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.reasonPhrase, ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(ex: Exception): ResponseEntity<ApiErrorResponse> {
        log.error("Unhandled exception", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                    "Unexpected error",
                )
            )
    }
}
