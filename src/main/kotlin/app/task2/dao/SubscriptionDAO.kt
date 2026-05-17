package app.task2.dao

import app.task2.entities.Subscription
import app.task2.entities.Status
import app.task2.repositories.SubscriptionRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

@Service
class SubscriptionDAO(
    private val subscriptionRepository: SubscriptionRepository,
) {
    fun save(subscription: Subscription): Subscription = subscriptionRepository.save(subscription)

    fun findById(id: UUID): Subscription? = subscriptionRepository.findById(id).orElse(null)

    fun findAllFiltered(
        userId: UUID?,
        serviceName: String?,
        status: Status?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        pageable: Pageable,
    ): Page<Subscription> = subscriptionRepository.findAllFiltered(userId, serviceName, status, dateFrom, dateTo, pageable)

    fun findToExpire(today: LocalDate): List<Subscription> =
        subscriptionRepository.findByStatusAndEndDateBefore(Status.ACTIVE, today)
}
