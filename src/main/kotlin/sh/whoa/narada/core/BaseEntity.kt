package sh.whoa.narada.core

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PreUpdate
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import sh.whoa.narada.util.UUIDv7
import java.time.LocalDateTime
import java.util.UUID

@MappedSuperclass
class BaseEntity {
	@Id
	val id: UUID = UUIDv7.randomUUID()

	@CreatedDate
	val createdAt: LocalDateTime = LocalDateTime.now()

	@UpdateTimestamp
	var updatedAt: LocalDateTime? = null

	@PreUpdate
	fun updateTimestamp() {
		updatedAt = LocalDateTime.now()
	}
}
