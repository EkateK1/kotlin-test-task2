package app.task2.services.interfaces

import app.task2.dto.requests.CreatePlanRequest
import app.task2.dto.responses.PlanResponse
import app.task2.entities.Plan
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface PlanInterface {
    fun create(request: CreatePlanRequest): Plan
    fun getById(id: UUID): Plan
    fun list(pageable: Pageable): Page<PlanResponse>
}
