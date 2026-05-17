package app.task2.repositories

import app.task2.entities.Plan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PlanRepository : JpaRepository<Plan, UUID>{
}
