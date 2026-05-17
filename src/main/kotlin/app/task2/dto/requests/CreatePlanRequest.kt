package app.task2.dto.requests

import java.math.BigDecimal

data class CreatePlanRequest(
    val serviceName: String,
    val planName: String,
    val description: String? = null,
    val defaultPrice: BigDecimal,
    val currency: String,
    val durationDays: Int,
    val isActive: Boolean? = null,
)
