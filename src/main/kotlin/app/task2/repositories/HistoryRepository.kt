package app.task2.repositories

import app.task2.entities.History
import app.task2.entities.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HistoryRepository : JpaRepository<History, UUID>{
    fun findTopBySubscription_IdAndNewStatusOrderByChangedAtDesc(subscriptionId: UUID, newStatus: Status): History?
}
