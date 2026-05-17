package app.task2.dto.responses

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class PlanResponse(
    val id: UUID,
    val serviceName: String,
    val planName: String,
    val description: String?,
    val defaultPrice: BigDecimal,
    val currency: String,
    val durationDays: Int,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
)

