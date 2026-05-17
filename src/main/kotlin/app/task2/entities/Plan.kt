package app.task2.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "subscription_plans")
class Plan(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID? = null,

    @Column(name = "service_name", nullable = false, length = 100)
    var serviceName: String? = null,

    @Column(name = "plan_name", nullable = false, length = 100)
    var planName: String? = null,

    @Column(name = "description", columnDefinition = "TEXT")
    var description: String? = null,

    @Column(name = "default_price", nullable = false, precision = 10, scale = 2)
    var defaultPrice: BigDecimal? = null,

    @Column(name = "currency", nullable = false, length = 10)
    var currency: String? = null,

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int? = null,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime? = null,
) {
    @OneToMany(mappedBy = "plan")
    var subscriptions: MutableList<Subscription> = mutableListOf()
}

