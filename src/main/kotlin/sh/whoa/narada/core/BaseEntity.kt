package sh.whoa.narada.core

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import sh.whoa.narada.util.UUIDv7
import java.time.Instant
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity : Persistable<UUID> {
	@Id
	private val id: UUID = UUIDv7.randomUUID()

	@CreatedDate
	val createdAt: Instant = Instant.now()

	@UpdateTimestamp
	var updatedAt: Instant? = null

	@Transient
	@jakarta.persistence.Transient
	private var isNewEntity = true

	override fun getId(): UUID = id

	override fun isNew(): Boolean = isNewEntity

	@PostPersist
	@PostLoad
	fun markNotNew() {
		isNewEntity = false
	}
}
