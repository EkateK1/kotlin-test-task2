package app.task2.dao

import app.task2.entities.Subscription
import app.task2.repositories.SubscriptionRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class SubscriptionDAO(
    private val subscriptionRepository: SubscriptionRepository,
) {
    fun save(subscription: Subscription): Subscription = subscriptionRepository.save(subscription)

    fun findById(id: UUID): Subscription? = subscriptionRepository.findById(id).orElse(null)
}
