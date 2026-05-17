package app.task2.dao

import app.task2.entities.History
import app.task2.entities.Status
import app.task2.repositories.HistoryRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class HistoryDAO(
    private val historyRepository: HistoryRepository,
) {
    fun save(history: History): History = historyRepository.save(history)

    fun findLatestPaused(subscriptionId: UUID): History? =
        historyRepository.findTopBySubscription_IdAndNewStatusOrderByChangedAtDesc(subscriptionId, Status.PAUSED)
}
