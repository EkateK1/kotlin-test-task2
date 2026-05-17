package app.task2.controllers

import app.task2.dto.requests.ChangeSubscriptionStatusRequest
import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.SubscriptionResponse
import app.task2.entities.Status
import app.task2.services.interfaces.SubscriptionInterface
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.util.UUID

@RestController
@Tag(
    name = "Subscriptions",
    description = "Управление подписками: создание, просмотр, список, пауза/возобновление, отмена, продление.",
)
class SubscriptionController(
    private val subscriptionService: SubscriptionInterface,
) {
    private val log = LoggerFactory.getLogger(SubscriptionController::class.java)

    @PostMapping("/subscriptions")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Создать подписку",
        description = "Создать подписку двумя способами: (1) по существующему плану через planId, (2) с созданием нового плана через plan.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Подписка создана"),
            ApiResponse(responseCode = "400", description = "Некорректный запрос", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "404", description = "План/подписка не найдены", content = [Content(schema = Schema(hidden = true))]),
            ApiResponse(responseCode = "409", description = "Конфликт бизнес-логики", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun create(
        @RequestBody request: CreateSubscriptionRequest,
    ): ResponseEntity<SubscriptionResponse> {
        log.info("POST /subscriptions userId={} planId={} hasPlanObject={}", request.userId, request.planId, request.plan != null)
        val response = subscriptionService.create(request)
        log.info("Subscription created id={} userId={} status={}", response.id, response.userId, response.status)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/subscriptions/{id}")
    @Operation(
        summary = "Получить подписку по id",
        description = "Возвращает подписку и связанный план по id подписки.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Подписка найдена"),
            ApiResponse(responseCode = "404", description = "Подписка не найдена", content = [Content(schema = Schema(hidden = true))]),
        ],
    )
    fun getById(@PathVariable id: UUID): ResponseEntity<SubscriptionResponse> {
        log.info("GET /subscriptions/{}", id)
        val response = subscriptionService.getById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/subscriptions")
    @Operation(
        summary = "Получить список подписок",
        description = "Список подписок с фильтрами по пользователю/сервису/статусу/диапазону дат с поддержкой пагинации и сортировки.",
    )
    fun list(
        @Parameter(description = "Фильтр по id пользователя", example = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")
        @RequestParam(required = false) userId: UUID?,
        @Parameter(description = "Фильтр по названию сервиса плана", example = "music")
        @RequestParam(required = false) serviceName: String?,
        @Parameter(description = "Фильтр по статусу подписки", example = "ACTIVE")
        @RequestParam(required = false) status: Status?,
        @Parameter(description = "Начало диапазона дат (ISO-8601 yyyy-MM-dd). Подписка попадает, если endDate >= dateFrom.")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFrom: LocalDate?,
        @Parameter(description = "Конец диапазона дат (ISO-8601 yyyy-MM-dd). Подписка попадает, если startDate <= dateTo.")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate?,
        @Parameter(description = "Номер страницы (0..)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Размер страницы (1..200)", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(
            description = "Сортировка. Можно передать несколько параметров. Формат: field или field,desc. Примеры: endDate,desc; plan.serviceName,asc",
            example = "endDate,desc",
        )
        @RequestParam(required = false) sort: List<String>?,
    ): ResponseEntity<Page<SubscriptionResponse>> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), parseSort(sort))
        log.info(
            "GET /subscriptions userId={} serviceName={} status={} dateFrom={} dateTo={} page={} size={} sort={}",
            userId,
            serviceName,
            status,
            dateFrom,
            dateTo,
            pageable.pageNumber,
            pageable.pageSize,
            pageable.sort,
        )
        val result = subscriptionService.list(userId, serviceName, status, dateFrom, dateTo, pageable)
        log.info(
            "List subscriptions result totalElements={} totalPages={} numberOfElements={}",
            result.totalElements,
            result.totalPages,
            result.numberOfElements,
        )
        return ResponseEntity.ok(result)
    }

    @GetMapping("/users/{userId}/subscriptions/active")
    @Operation(
        summary = "Получить активные подписки пользователя",
        description = "Возвращает только подписки пользователя в статусе ACTIVE. Поддерживает пагинацию и сортировку.",
    )
    fun listActiveByUser(
        @PathVariable userId: UUID,
        @Parameter(description = "Номер страницы (0..)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Размер страницы (1..200)", example = "20")
        @RequestParam(defaultValue = "20") size: Int,
        @Parameter(
            description = "Сортировка. Можно передать несколько параметров. Формат: field или field,desc. Пример: endDate,desc",
            example = "endDate,desc",
        )
        @RequestParam(required = false) sort: List<String>?,
    ): ResponseEntity<Page<SubscriptionResponse>> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), parseSort(sort))
        log.info(
            "GET /users/{}/subscriptions/active page={} size={} sort={}",
            userId,
            pageable.pageNumber,
            pageable.pageSize,
            pageable.sort,
        )
        val result = subscriptionService.listActiveByUser(userId, pageable)
        log.info(
            "List active subscriptions result totalElements={} totalPages={} numberOfElements={}",
            result.totalElements,
            result.totalPages,
            result.numberOfElements,
        )
        return ResponseEntity.ok(result)
    }

    private fun parseSort(sortParams: List<String>?): Sort {
        if (sortParams.isNullOrEmpty()) return Sort.unsorted()

        val allowed = setOf(
            "id",
            "userId",
            "status",
            "startDate",
            "endDate",
            "plan.serviceName",
            "plan.planName",
            "plan.createdAt",
            "plan.isActive",
            "plan.durationDays",
            "plan.defaultPrice",
            "plan.currency",
        )

        val orders = sortParams.mapNotNull { raw ->
            val cleaned = raw.trim().removePrefix("[").removeSuffix("]").trim().trim('"')
            if (cleaned.isBlank() || cleaned == "string" || cleaned.contains('[') || cleaned.contains(']')) return@mapNotNull null

            val parts = cleaned.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            val property = parts.getOrNull(0) ?: return@mapNotNull null
            if (property !in allowed) return@mapNotNull null

            val dir = parts.getOrNull(1)?.lowercase()
            val direction = if (dir == "desc") Sort.Direction.DESC else Sort.Direction.ASC
            Sort.Order(direction, property)
        }

        return if (orders.isEmpty()) Sort.unsorted() else Sort.by(orders)
    }

    @PatchMapping("/subscriptions/{id}/pause")
    @Operation(
        summary = "Приостановить подписку",
        description = "Переводит ACTIVE подписку в PAUSED и записывает событие в историю статусов.",
    )
    fun pause(
        @PathVariable id: UUID,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Опциональная причина изменения статуса",
            content = [Content(schema = Schema(implementation = ChangeSubscriptionStatusRequest::class))],
        )
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): ResponseEntity<SubscriptionResponse> {
        log.info("PATCH /subscriptions/{}/pause reason={}", id, request?.reason)
        val response = subscriptionService.pause(id, request?.reason)
        log.info("Subscription paused id={} status={}", response.id, response.status)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/subscriptions/{id}/unpause")
    @Operation(
        summary = "Возобновить подписку (unpause)",
        description = "Переводит PAUSED подписку в ACTIVE и сдвигает endDate на длительность паузы (по последней записи PAUSED в истории).",
    )
    fun unpause(
        @PathVariable id: UUID,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Опциональная причина изменения статуса",
            content = [Content(schema = Schema(implementation = ChangeSubscriptionStatusRequest::class))],
        )
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): ResponseEntity<SubscriptionResponse> {
        log.info("PATCH /subscriptions/{}/unpause reason={}", id, request?.reason)
        val response = subscriptionService.unpause(id, request?.reason)
        log.info("Subscription unpaused id={} status={}", response.id, response.status)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/subscriptions/{id}/cancel")
    @Operation(
        summary = "Отменить подписку",
        description = "Переводит подписку в CANCELLED и записывает событие в историю статусов.",
    )
    fun cancel(
        @PathVariable id: UUID,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Опциональная причина изменения статуса",
            content = [Content(schema = Schema(implementation = ChangeSubscriptionStatusRequest::class))],
        )
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): ResponseEntity<SubscriptionResponse> {
        log.info("PATCH /subscriptions/{}/cancel reason={}", id, request?.reason)
        val response = subscriptionService.cancel(id, request?.reason)
        log.info("Subscription cancelled id={} status={}", response.id, response.status)
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/subscriptions/{id}/renew")
    @Operation(
        summary = "Продлить подписку",
        description = "Продлевает подписку (только ACTIVE или EXPIRED): увеличивает endDate на durationDays из плана; для EXPIRED также возвращает статус ACTIVE.",
    )
    fun renew(
        @PathVariable id: UUID,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Опциональная причина продления",
            content = [Content(schema = Schema(implementation = ChangeSubscriptionStatusRequest::class))],
        )
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): ResponseEntity<SubscriptionResponse> {
        log.info("PATCH /subscriptions/{}/renew reason={}", id, request?.reason)
        val response = subscriptionService.renew(id, request?.reason)
        log.info("Subscription renewed id={} status={} endDate={}", response.id, response.status, response.endDate)
        return ResponseEntity.ok(response)
    }
}
