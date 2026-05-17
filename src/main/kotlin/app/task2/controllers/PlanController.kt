package app.task2.controllers

import app.task2.dto.responses.PlanResponse
import app.task2.services.interfaces.PlanInterface
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PlanController(
    private val planService: PlanInterface,
) {
    @GetMapping("/plans")
    fun list(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) sort: List<String>?,
    ): Page<PlanResponse> {
        val pageable = PageRequest.of(page.coerceAtLeast(0), size.coerceIn(1, 200), parseSort(sort))
        return planService.list(pageable)
    }

    private fun parseSort(sortParams: List<String>?): Sort {
        if (sortParams.isNullOrEmpty()) return Sort.unsorted()

        val allowed = setOf(
            "id",
            "serviceName",
            "planName",
            "defaultPrice",
            "currency",
            "durationDays",
            "isActive",
            "createdAt",
        )

        val orders = sortParams.mapNotNull { raw ->
            val cleaned = raw.trim().removePrefix("[").removeSuffix("]").trim().trim('"')
            if (cleaned.isBlank() || cleaned == "string" || cleaned.contains('[') || cleaned.contains(']')) return@mapNotNull null

            val parts = cleaned.split(',').map { it.trim() }.filter { it.isNotEmpty() }
            val property = parts.getOrNull(0) ?: return@mapNotNull null
            if (property !in allowed) return@mapNotNull null

            val dir = parts.getOrNull(1)?.lowercase()
            val direction = if (dir == "desc") Sort.Direction.DESC else Sort.Direction.ASC
            Sort.Order(direction, property)
        }

        return if (orders.isEmpty()) Sort.unsorted() else Sort.by(orders)
    }
}
