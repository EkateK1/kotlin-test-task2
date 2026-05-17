package app.task2.controllers

import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.SubscriptionResponse
import app.task2.services.interfaces.SubscriptionInterface
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SubscriptionController(
    private val subscriptionService: SubscriptionInterface,
) {
    @PostMapping("/subscriptions")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody request: CreateSubscriptionRequest): SubscriptionResponse =
        subscriptionService.create(request)
}
