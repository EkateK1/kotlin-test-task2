package app.task2.services.interfaces

import app.task2.entities.History
import java.util.UUID

interface HistoryInterface {
    fun save(history: History): History
    fun findLatestPaused(subscriptionId: UUID): History?
}
