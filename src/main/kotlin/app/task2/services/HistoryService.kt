package app.task2.services

import app.task2.dao.HistoryDAO
import app.task2.entities.History
import app.task2.services.interfaces.HistoryInterface
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class HistoryService(
    private val historyDAO: HistoryDAO,
) : HistoryInterface {
    @Transactional
    override fun save(history: History): History = historyDAO.save(history)

    @Transactional(readOnly = true)
    override fun findLatestPaused(subscriptionId: UUID): History? = historyDAO.findLatestPaused(subscriptionId)
}
