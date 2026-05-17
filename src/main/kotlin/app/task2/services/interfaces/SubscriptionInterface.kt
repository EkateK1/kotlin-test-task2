package app.task2.services.interfaces

import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.SubscriptionResponse
import java.util.UUID

interface SubscriptionInterface {
    fun create(request: CreateSubscriptionRequest): SubscriptionResponse
    fun getById(id: UUID): SubscriptionResponse
    fun pause(id: UUID, reason: String? = null): SubscriptionResponse
    fun unpause(id: UUID, reason: String? = null): SubscriptionResponse
    fun cancel(id: UUID, reason: String? = null): SubscriptionResponse
    fun renew(id: UUID, reason: String? = null): SubscriptionResponse
}
