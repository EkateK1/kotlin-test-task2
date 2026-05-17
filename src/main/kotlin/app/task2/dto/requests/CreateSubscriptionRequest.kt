package app.task2.dto.requests

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class CreateSubscriptionRequest(
    val userId: UUID,
    val planId: UUID? = null,
    val plan: CreatePlanRequest? = null,
    val startDate: LocalDate? = null,
)
