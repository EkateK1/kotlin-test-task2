package app.task2

import app.task2.entities.Plan
import app.task2.entities.Status
import app.task2.entities.Subscription
import app.task2.repositories.PlanRepository
import app.task2.repositories.SubscriptionRepository
import app.task2.services.SubscriptionService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@Transactional
class SubscriptionExpirationSchedulerTests @Autowired constructor(
    private val subscriptionService: SubscriptionService,
    private val planRepository: PlanRepository,
    private val subscriptionRepository: SubscriptionRepository,
) {
    @Test
    fun `expireEndedSubscriptions marks ACTIVE subscriptions as EXPIRED`() {
        val plan = planRepository.save(
            Plan(
                id = UUID.randomUUID(),
                serviceName = "music",
                planName = "BASIC",
                description = null,
                defaultPrice = BigDecimal("199.00"),
                currency = "RUB",
                durationDays = 30,
                isActive = true,
                createdAt = LocalDateTime.now(),
            )
        )

        val sub = subscriptionRepository.save(
            Subscription(
                id = UUID.randomUUID(),
                userId = UUID.randomUUID(),
                plan = plan,
                status = Status.ACTIVE,
                startDate = LocalDate.now().minusDays(10),
                endDate = LocalDate.now().minusDays(1),
            )
        )

        val expiredCount = subscriptionService.expireEndedSubscriptions(LocalDate.now())
        assertEquals(1, expiredCount)

        val updated = subscriptionRepository.findById(sub.id!!).orElseThrow()
        assertEquals(Status.EXPIRED, updated.status)
    }
}
