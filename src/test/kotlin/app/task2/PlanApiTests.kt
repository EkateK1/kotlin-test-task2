package app.task2

import app.task2.entities.Plan
import app.task2.repositories.PlanRepository
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PlanApiTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val planRepository: PlanRepository,
) {
    @Test
    fun `GET plans returns paged list`() {
        planRepository.save(
            Plan(
                id = UUID.randomUUID(),
                serviceName = "music",
                planName = "BASIC",
                description = null,
                defaultPrice = BigDecimal("199.00"),
                currency = "RUB",
                durationDays = 30,
                isActive = true,
                createdAt = LocalDateTime.now(),
            )
        )

        mockMvc.get("/plans") {
            param("page", "0")
            param("size", "10")
            param("sort", "createdAt,desc")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content", hasSize<Any>(1))
        }
    }
}
