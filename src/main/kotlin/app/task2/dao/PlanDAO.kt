package app.task2.dao

import app.task2.entities.Plan
import app.task2.repositories.PlanRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PlanDAO(
    private val planRepository: PlanRepository,
) {
    fun save(plan: Plan): Plan = planRepository.save(plan)

    fun findById(id: UUID): Plan? = planRepository.findById(id).orElse(null)
}
