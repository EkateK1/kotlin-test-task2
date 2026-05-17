package app.task2.controllers

import app.task2.dto.requests.ChangeSubscriptionStatusRequest
import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.SubscriptionResponse
import app.task2.entities.Status
import app.task2.services.interfaces.SubscriptionInterface
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
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
class SubscriptionController(
    private val subscriptionService: SubscriptionInterface,
) {
    @PostMapping("/subscriptions")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateSubscriptionRequest): SubscriptionResponse =
        subscriptionService.create(request)

    @GetMapping("/subscriptions/{id}")
    fun getById(@PathVariable id: UUID): SubscriptionResponse =
        subscriptionService.getById(id)

    @GetMapping("/subscriptions")
    fun list(
        @RequestParam(required = false) userId: UUID?,
        @RequestParam(required = false) serviceName: String?,
        @RequestParam(required = false) status: Status?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateFrom: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) dateTo: LocalDate?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: List<String>?,
    ): Page<SubscriptionResponse> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), parseSort(sort))
        return subscriptionService.list(userId, serviceName, status, dateFrom, dateTo, pageable)
    }

    @GetMapping("/users/{userId}/subscriptions/active")
    fun listActiveByUser(
        @PathVariable userId: UUID,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: List<String>?,
    ): Page<SubscriptionResponse> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), parseSort(sort))
        return subscriptionService.listActiveByUser(userId, pageable)
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
    fun pause(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): SubscriptionResponse = subscriptionService.pause(id, request?.reason)

    @PatchMapping("/subscriptions/{id}/unpause")
    fun unpause(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): SubscriptionResponse = subscriptionService.unpause(id, request?.reason)

    @PatchMapping("/subscriptions/{id}/cancel")
    fun cancel(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): SubscriptionResponse = subscriptionService.cancel(id, request?.reason)

    @PatchMapping("/subscriptions/{id}/renew")
    fun renew(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: ChangeSubscriptionStatusRequest?,
    ): SubscriptionResponse = subscriptionService.renew(id, request?.reason)
}
