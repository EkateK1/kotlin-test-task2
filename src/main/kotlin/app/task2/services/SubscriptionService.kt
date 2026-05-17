package app.task2.services

import app.task2.dao.SubscriptionDAO
import app.task2.dto.requests.CreateSubscriptionRequest
import app.task2.dto.responses.PlanResponse
import app.task2.dto.responses.SubscriptionResponse
import app.task2.entities.History
import app.task2.entities.Plan
import app.task2.entities.Status
import app.task2.entities.Subscription
import app.task2.services.interfaces.HistoryInterface
import app.task2.services.interfaces.PlanInterface
import app.task2.services.interfaces.SubscriptionInterface
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import org.slf4j.LoggerFactory

@Service
class SubscriptionService(
    private val subscriptionDAO: SubscriptionDAO,
    private val planService: PlanInterface,
    private val historyService: HistoryInterface,
) : SubscriptionInterface {
    private val log = LoggerFactory.getLogger(SubscriptionService::class.java)

    @Transactional(readOnly = true)
    override fun getById(id: UUID): SubscriptionResponse {
        val subscription =
            subscriptionDAO.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found: $id")
        return buildSubscriptionResponse(subscription)
    }

    @Transactional(readOnly = true)
    override fun list(
        userId: UUID?,
        serviceName: String?,
        status: Status?,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
        pageable: Pageable,
    ): Page<SubscriptionResponse> {
        return subscriptionDAO
            .findAllFiltered(userId, serviceName, status, dateFrom, dateTo, pageable)
            .map { buildSubscriptionResponse(it) }
    }

    @Transactional(readOnly = true)
    override fun listActiveByUser(userId: UUID, pageable: Pageable): Page<SubscriptionResponse> {
        return list(userId = userId, serviceName = null, status = Status.ACTIVE, dateFrom = null, dateTo = null, pageable = pageable)
    }

    @Transactional
    fun expireEndedSubscriptions(today: LocalDate = LocalDate.now()): Int {
        val toExpire = subscriptionDAO.findToExpire(today)
        if (toExpire.isEmpty()) return 0

        val now = LocalDateTime.now()
        toExpire.forEach { subscription ->
            val old = subscription.status
            subscription.status = Status.EXPIRED
            val saved = subscriptionDAO.save(subscription)
            historyService.save(
                History(
                    id = UUID.randomUUID(),
                    subscription = saved,
                    oldStatus = old,
                    newStatus = Status.EXPIRED,
                    changedAt = now,
                    reason = "expired by scheduler",
                )
            )
        }
        log.info("Expired {} subscriptions (today={})", toExpire.size, today)
        return toExpire.size
    }

    @Transactional
    override fun pause(id: UUID, reason: String?): SubscriptionResponse {
        val subscription =
            subscriptionDAO.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found: $id")

        val current = subscription.status ?: throw IllegalStateException("subscription.status is null")
        if (current == Status.PAUSED) return buildSubscriptionResponse(subscription)
        if (current != Status.ACTIVE) throw ResponseStatusException(HttpStatus.CONFLICT, "Only ACTIVE subscription can be paused")

        return changeStatus(subscription, Status.PAUSED, reason ?: "paused by user")
    }

    @Transactional
    override fun unpause(id: UUID, reason: String?): SubscriptionResponse {
        val subscription =
            subscriptionDAO.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found: $id")

        val current = subscription.status ?: throw IllegalStateException("subscription.status is null")
        if (current != Status.PAUSED) throw ResponseStatusException(HttpStatus.CONFLICT, "Only PAUSED subscription can be unpaused")

        val endDate = subscription.endDate ?: throw IllegalStateException("subscription.endDate is null")

        val pausedHistory = historyService.findLatestPaused(id)
            ?: throw ResponseStatusException(HttpStatus.CONFLICT, "No PAUSED history record found for subscription: $id")

        val pausedAt = pausedHistory.changedAt ?: throw IllegalStateException("history.changedAt is null")
        val now = LocalDateTime.now()
        val duration = Duration.between(pausedAt, now)
        val totalSeconds = duration.seconds.coerceAtLeast(0)
        val fullDays = totalSeconds / 86_400
        val daysToAdd = fullDays.coerceAtLeast(0)

        subscription.endDate = endDate.plusDays(daysToAdd)
        subscription.status = Status.ACTIVE
        val saved = subscriptionDAO.save(subscription)

        historyService.save(
            History(
                id = UUID.randomUUID(),
                subscription = saved,
                oldStatus = Status.PAUSED,
                newStatus = Status.ACTIVE,
                changedAt = now,
                reason = reason ?: "unpaused",
            )
        )

        return buildSubscriptionResponse(saved)
    }

    @Transactional
    override fun cancel(id: UUID, reason: String?): SubscriptionResponse {
        val subscription =
            subscriptionDAO.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found: $id")

        val current = subscription.status ?: throw IllegalStateException("subscription.status is null")
        if (current == Status.CANCELLED) return buildSubscriptionResponse(subscription)
        if (current == Status.EXPIRED) throw ResponseStatusException(HttpStatus.CONFLICT, "EXPIRED subscription cannot be cancelled")

        return changeStatus(subscription, Status.CANCELLED, reason ?: "cancelled by user")
    }

    @Transactional
    override fun renew(id: UUID, reason: String?): SubscriptionResponse {
        val subscription =
            subscriptionDAO.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found: $id")

        val currentStatus = subscription.status ?: throw IllegalStateException("subscription.status is null")
        if (currentStatus != Status.ACTIVE && currentStatus != Status.EXPIRED) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Only ACTIVE or EXPIRED subscription can be renewed")
        }

        val plan = subscription.plan ?: throw IllegalStateException("subscription.plan is null")
        val durationDays = plan.durationDays ?: throw IllegalStateException("plan.durationDays is null")
        if (durationDays <= 0) throw ResponseStatusException(HttpStatus.CONFLICT, "Plan durationDays must be > 0")

        val today = LocalDate.now()
        val currentEndDate = subscription.endDate ?: throw IllegalStateException("subscription.endDate is null")

        val baseDate = if (currentEndDate.isAfter(today)) currentEndDate else today
        subscription.endDate = baseDate.plusDays(durationDays.toLong())

        if (currentStatus == Status.EXPIRED) {
            subscription.startDate = today
            subscription.status = Status.ACTIVE
        }

        val saved = subscriptionDAO.save(subscription)

        historyService.save(
            History(
                id = UUID.randomUUID(),
                subscription = saved,
                oldStatus = currentStatus,
                newStatus = saved.status ?: currentStatus,
                changedAt = LocalDateTime.now(),
                reason = reason ?: "renewed",
            )
        )

        return buildSubscriptionResponse(saved)
    }

    @Transactional
    override fun create(request: CreateSubscriptionRequest): SubscriptionResponse {
        val planId = request.planId
        val planRequest = request.plan

        if ((planId == null && planRequest == null) || (planId != null && planRequest != null)) {
            throw IllegalArgumentException("Provide exactly one: planId OR plan")
        }

        val plan = if (planId != null) {
            planService.getById(planId)
        } else {
            planService.create(planRequest!!)
        }

        if (plan.isActive == null || plan.isActive == false) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan is not active")
        }

        val startDate = request.startDate ?: LocalDate.now()
        val durationDays = plan.durationDays ?: throw IllegalStateException("plan.durationDays is null")
        if (durationDays <= 0) throw ResponseStatusException(HttpStatus.CONFLICT, "Plan durationDays must be > 0")
        val endDate = startDate.plusDays(durationDays.toLong())

        val subscription = Subscription(
            id = UUID.randomUUID(),
            userId = request.userId,
            plan = plan,
            status = Status.ACTIVE,
            startDate = startDate,
            endDate = endDate,
        )
        val saved = subscriptionDAO.save(subscription)

        historyService.save(
            History(
                id = UUID.randomUUID(),
                subscription = saved,
                oldStatus = null,
                newStatus = Status.ACTIVE,
                changedAt = LocalDateTime.now(),
                reason = "created",
            )
        )

        return buildSubscriptionResponse(saved)
    }

    private fun changeStatus(subscription: Subscription, newStatus: Status, reason: String): SubscriptionResponse {
        val oldStatus = subscription.status
        subscription.status = newStatus
        val saved = subscriptionDAO.save(subscription)

        historyService.save(
            History(
                id = UUID.randomUUID(),
                subscription = saved,
                oldStatus = oldStatus,
                newStatus = newStatus,
                changedAt = LocalDateTime.now(),
                reason = reason,
            )
        )

        return buildSubscriptionResponse(saved)
    }

    private fun buildSubscriptionResponse(subscription: Subscription): SubscriptionResponse {
        val id = subscription.id ?: throw IllegalStateException("subscription.id is null")
        val userId = subscription.userId ?: throw IllegalStateException("subscription.userId is null")
        val status = subscription.status ?: throw IllegalStateException("subscription.status is null")
        val startDate = subscription.startDate ?: throw IllegalStateException("subscription.startDate is null")
        val endDate = subscription.endDate ?: throw IllegalStateException("subscription.endDate is null")
        val plan = subscription.plan ?: throw IllegalStateException("subscription.plan is null")

        return SubscriptionResponse(
            id = id,
            userId = userId,
            status = status,
            startDate = startDate,
            endDate = endDate,
            plan = buildPlanResponse(plan),
        )
    }

    private fun buildPlanResponse(plan: Plan): PlanResponse {
        val id = plan.id ?: throw IllegalStateException("plan.id is null")
        val serviceName = plan.serviceName ?: throw IllegalStateException("plan.serviceName is null")
        val planName = plan.planName ?: throw IllegalStateException("plan.planName is null")
        val defaultPrice = plan.defaultPrice ?: throw IllegalStateException("plan.defaultPrice is null")
        val currency = plan.currency ?: throw IllegalStateException("plan.currency is null")
        val durationDays = plan.durationDays ?: throw IllegalStateException("plan.durationDays is null")
        val isActive = plan.isActive ?: throw IllegalStateException("plan.isActive is null")
        val createdAt = plan.createdAt ?: throw IllegalStateException("plan.createdAt is null")

        return PlanResponse(
            id = id,
            serviceName = serviceName,
            planName = planName,
            description = plan.description,
            defaultPrice = defaultPrice,
            currency = currency,
            durationDays = durationDays,
            isActive = isActive,
            createdAt = createdAt,
        )
    }
}
