package app.task2.services.interfaces

import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.SubscriptionResponse
import app.task2.entities.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDate
import java.util.UUID

interface SubscriptionInterface {
    fun create(request: CreateSubscriptionRequest): SubscriptionResponse
    fun getById(id: UUID): SubscriptionResponse
    fun list(
        userId: UUID? = null,
        serviceName: String? = null,
        status: Status? = null,
        dateFrom: LocalDate? = null,
        dateTo: LocalDate? = null,
        pageable: Pageable,
    ): Page<SubscriptionResponse>
    fun listActiveByUser(userId: UUID, pageable: Pageable): Page<SubscriptionResponse>
    fun pause(id: UUID, reason: String? = null): SubscriptionResponse
    fun unpause(id: UUID, reason: String? = null): SubscriptionResponse
    fun cancel(id: UUID, reason: String? = null): SubscriptionResponse
    fun renew(id: UUID, reason: String? = null): SubscriptionResponse
}
