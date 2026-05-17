package app.task2.services

import app.task2.dao.PlanDAO
import app.task2.dto.requests.CreatePlanRequest
import app.task2.dto.responses.PlanResponse
import app.task2.entities.Plan
import app.task2.services.interfaces.PlanInterface
import org.springframework.stereotype.Service
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

@Service
class PlanService(
    private val planDAO: PlanDAO,
) : PlanInterface {
    private val log = org.slf4j.LoggerFactory.getLogger(PlanService::class.java)

    @Transactional
    override fun create(request: CreatePlanRequest): Plan {
        if (request.serviceName.isBlank()) throw IllegalArgumentException("serviceName is blank")
        if (request.planName.isBlank()) throw IllegalArgumentException("planName is blank")
        if (request.currency.isBlank()) throw IllegalArgumentException("currency is blank")
        if (request.durationDays <= 0) throw IllegalArgumentException("durationDays must be > 0")
        if (request.defaultPrice < java.math.BigDecimal.ZERO) throw IllegalArgumentException("defaultPrice must be >= 0")

        val now = LocalDateTime.now()
        val plan = Plan(
            id = UUID.randomUUID(),
            serviceName = request.serviceName,
            planName = request.planName,
            description = request.description,
            defaultPrice = request.defaultPrice,
            currency = request.currency,
            durationDays = request.durationDays,
            isActive = request.isActive ?: true,
            createdAt = now,
        )
        val saved = planDAO.save(plan)
        log.info("Created plan id={} serviceName={} planName={} active={}", saved.id, saved.serviceName, saved.planName, saved.isActive)
        return saved
    }

    override fun getById(id: UUID): Plan {
        val plan = planDAO.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found: $id")
        log.debug("Get plan id={} serviceName={} planName={}", id, plan.serviceName, plan.planName)
        return plan
    }

    @Transactional(readOnly = true)
    override fun list(pageable: Pageable): Page<PlanResponse> {
        log.debug("List plans page={} size={} sort={}", pageable.pageNumber, pageable.pageSize, pageable.sort)
        return planDAO.findAll(pageable).map { buildPlanResponse(it) }
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
