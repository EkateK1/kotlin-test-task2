package app.task2.schedulers

import app.task2.services.SubscriptionService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SubscriptionExpirationScheduler(
    private val subscriptionService: SubscriptionService,
) {
    private val log = LoggerFactory.getLogger(SubscriptionExpirationScheduler::class.java)

    @Scheduled(cron = "0 5 0 * * *")
    fun expireSubscriptions() {
        log.debug("Scheduler run: expireSubscriptions")
        val expired = subscriptionService.expireEndedSubscriptions()
        if (expired > 0) log.info("Scheduler expired {} subscriptions", expired)
    }
}
