package app.task2.dao

import app.task2.entities.History
import app.task2.repositories.HistoryRepository
import org.springframework.stereotype.Service

@Service
class HistoryDAO(
    private val historyRepository: HistoryRepository,
) {
    fun save(history: History): History = historyRepository.save(history)
}
