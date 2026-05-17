package app.task2.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "subscription_status_history")
class SubscriptionStatusHistory(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    var subscription: Subscription? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 50)
    var oldStatus: Status? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 50)
    var newStatus: Status? = null,

    @Column(name = "changed_at", nullable = false)
    var changedAt: LocalDateTime? = null,

    @Column(name = "reason", columnDefinition = "TEXT")
    var reason: String? = null,
)
