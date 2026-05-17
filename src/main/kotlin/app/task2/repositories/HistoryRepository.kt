package app.task2.repositories

import app.task2.entities.History
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HistoryRepository : JpaRepository<History, UUID>{
}