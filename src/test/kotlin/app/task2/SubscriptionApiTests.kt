package app.task2

import app.task2.entities.Plan
import app.task2.entities.Status
import app.task2.repositories.HistoryRepository
import app.task2.repositories.PlanRepository
import app.task2.repositories.SubscriptionRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubscriptionApiTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val planRepository: PlanRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val historyRepository: HistoryRepository,
) {
    @Test
    fun `GET subscriptions returns empty page when DB empty`() {
        mockMvc.get("/subscriptions") {
            param("page", "0")
            param("size", "10")
            param("sort", "string")
        }.andExpect {
            status { isOk() }
            jsonPath("$.content", hasSize<Any>(0))
        }
    }

    @Test
    fun `create subscription with existing planId, then get by id`() {
        val planId = UUID.randomUUID()
        val plan = planRepository.save(
            Plan(
                id = planId,
                serviceName = "music",
                planName = "BASIC",
                description = "Basic plan",
                defaultPrice = BigDecimal("199.00"),
                currency = "RUB",
                durationDays = 30,
                isActive = true,
                createdAt = LocalDateTime.now(),
            )
        )

        val userId = UUID.randomUUID()
        val body = mapOf(
            "userId" to userId,
            "planId" to plan.id,
            "startDate" to LocalDate.parse("2026-05-17"),
        )

        val createResult = mockMvc.post("/subscriptions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(body)
        }.andExpect {
            status { isCreated() }
            jsonPath("$.userId", `is`(userId.toString()))
            jsonPath("$.status", `is`("ACTIVE"))
            jsonPath("$.plan.serviceName", `is`("music"))
            jsonPath("$.plan.planName", `is`("BASIC"))
        }.andReturn()

        val createdId = objectMapper.readTree(createResult.response.contentAsString)["id"].asText()

        mockMvc.get("/subscriptions/$createdId").andExpect {
            status { isOk() }
            jsonPath("$.id", `is`(createdId))
            jsonPath("$.status", `is`("ACTIVE"))
        }
    }

    @Test
    fun `pause then unpause shifts endDate by full days paused`() {
        val plan = planRepository.save(
            Plan(
                id = UUID.randomUUID(),
                serviceName = "video",
                planName = "BASIC",
                description = null,
                defaultPrice = BigDecimal("299.00"),
                currency = "RUB",
                durationDays = 30,
                isActive = true,
                createdAt = LocalDateTime.now(),
            )
        )

        val userId = UUID.randomUUID()
        val createBody = mapOf(
            "userId" to userId,
            "planId" to plan.id,
            "startDate" to LocalDate.now(),
        )

        val createResult = mockMvc.post("/subscriptions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createBody)
        }.andExpect { status { isCreated() } }.andReturn()

        val subscriptionId = UUID.fromString(objectMapper.readTree(createResult.response.contentAsString)["id"].asText())

        val beforeUnpauseEndDate = subscriptionRepository.findById(subscriptionId).orElseThrow().endDate!!

        mockMvc.patch("/subscriptions/$subscriptionId/pause") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("reason" to "test pause"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.status", `is`("PAUSED"))
        }

        val pausedHistory = historyRepository
            .findTopBySubscription_IdAndNewStatusOrderByChangedAtDesc(subscriptionId, Status.PAUSED)
            ?: throw IllegalStateException("Paused history not found")
        pausedHistory.changedAt = LocalDateTime.now().minusDays(2).minusMinutes(1)
        historyRepository.save(pausedHistory)

        mockMvc.patch("/subscriptions/$subscriptionId/unpause").andExpect {
            status { isOk() }
            jsonPath("$.status", `is`("ACTIVE"))
        }

        val afterUnpauseEndDate = subscriptionRepository.findById(subscriptionId).orElseThrow().endDate!!
        org.junit.jupiter.api.Assertions.assertEquals(beforeUnpauseEndDate.plusDays(2), afterUnpauseEndDate)
    }

    @Test
    fun `renew allowed only for ACTIVE or EXPIRED`() {
        val plan = planRepository.save(
            Plan(
                id = UUID.randomUUID(),
                serviceName = "music",
                planName = "PREMIUM",
                description = null,
                defaultPrice = BigDecimal("399.00"),
                currency = "RUB",
                durationDays = 30,
                isActive = true,
                createdAt = LocalDateTime.now(),
            )
        )

        val userId = UUID.randomUUID()
        val createBody = mapOf(
            "userId" to userId,
            "planId" to plan.id,
            "startDate" to LocalDate.now().minusDays(40),
        )

        val createResult = mockMvc.post("/subscriptions") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(createBody)
        }.andReturn()
        val subscriptionId = UUID.fromString(objectMapper.readTree(createResult.response.contentAsString)["id"].asText())

        mockMvc.patch("/subscriptions/$subscriptionId/cancel").andExpect {
            status { isOk() }
            jsonPath("$.status", `is`("CANCELLED"))
        }

        mockMvc.patch("/subscriptions/$subscriptionId/renew").andExpect {
            status { isConflict() }
        }
    }
}
