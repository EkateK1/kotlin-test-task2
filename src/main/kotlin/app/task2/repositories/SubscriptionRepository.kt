package app.task2.repositories

import app.task2.entities.Status
import app.task2.entities.Subscription
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.UUID

@Repository
interface SubscriptionRepository : JpaRepository<Subscription, UUID>{
    @Query(
        """
        select s from Subscription s
        join s.plan p
        where (:userId is null or s.userId = :userId)
          and (:serviceName is null or p.serviceName = :serviceName)
          and (:status is null or s.status = :status)
          and (:dateFrom is null or s.endDate >= :dateFrom)
          and (:dateTo is null or s.startDate <= :dateTo)
        """,
    )
    fun findAllFiltered(
        @Param("userId") userId: UUID?,
        @Param("serviceName") serviceName: String?,
        @Param("status") status: Status?,
        @Param("dateFrom") dateFrom: LocalDate?,
        @Param("dateTo") dateTo: LocalDate?,
        pageable: Pageable,
    ): Page<Subscription>

    fun findByStatusAndEndDateBefore(status: Status, date: LocalDate): List<Subscription>
}
