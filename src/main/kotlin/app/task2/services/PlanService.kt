package app.task2.services

import app.task2.dao.PlanDAO
import app.task2.dto.requests.CreatePlanRequest
import app.task2.entities.Plan
import app.task2.services.interfaces.PlanInterface
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.UUID

@Service
class PlanService(
    private val planDAO: PlanDAO,
) : PlanInterface {
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
            isActive = request.isActive,
            createdAt = now,
        )
        return planDAO.save(plan)
    }

    override fun getById(id: UUID): Plan {
        return planDAO.findById(id) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found: $id")
    }
}
