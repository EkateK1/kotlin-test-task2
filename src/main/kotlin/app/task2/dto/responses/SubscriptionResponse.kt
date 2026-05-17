package app.task2.dto.responses

import app.task2.entities.Status
import java.time.LocalDate
import java.util.UUID

data class SubscriptionResponse(
    val id: UUID,
    val userId: UUID,
    val status: Status,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val plan: PlanResponse,
)
