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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class SubscriptionService(
    private val subscriptionDAO: SubscriptionDAO,
    private val planService: PlanInterface,
    private val historyService: HistoryInterface,
) : SubscriptionInterface {
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
