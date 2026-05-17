package app.task2.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "subscriptions")
class Subscription(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: Plan? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    var status: Status? = null,

    @Column(name = "start_date", nullable = false)
    var startDate: LocalDate? = null,

    @Column(name = "end_date", nullable = false)
    var endDate: LocalDate? = null,

    ) {
    @OneToMany(mappedBy = "subscription")
    var statusHistory: MutableList<History> = mutableListOf()
}
