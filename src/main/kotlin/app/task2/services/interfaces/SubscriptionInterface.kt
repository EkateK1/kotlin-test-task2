package app.task2.services.interfaces

import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.SubscriptionResponse

interface SubscriptionInterface {
    fun create(request: CreateSubscriptionRequest): SubscriptionResponse
}
