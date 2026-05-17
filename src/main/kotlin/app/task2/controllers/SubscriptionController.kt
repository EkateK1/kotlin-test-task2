package app.task2.controllers

import app.task2.dto.requests.ChangeSubscriptionStatusRequest
import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.SubscriptionResponse
import app.task2.services.interfaces.SubscriptionInterface
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
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
