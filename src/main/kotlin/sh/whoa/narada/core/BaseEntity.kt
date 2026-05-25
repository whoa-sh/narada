/*
 * SPDX-License-Identifier: AGPL-3.0-only OR SSPL-1.0 OR Elastic-2.0
 * Copyright (c) 2026 Subhrodip Mohanta (whoa.sh). All rights reserved.
 */
package sh.whoa.narada.core

import jakarta.persistence.Column
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.domain.Persistable
import sh.whoa.narada.util.UUIDv7
import java.time.Instant
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity : Persistable<UUID> {
	@Id
	private val id: UUID = UUIDv7.randomUUID()

	@CreationTimestamp
	@Column(updatable = false, nullable = false)
	var createdAt: Instant? = null
		protected set

	@UpdateTimestamp
	@Column(nullable = false)
	var updatedAt: Instant? = null
		protected set

	@Transient
	private var isNewEntity = true

	override fun getId(): UUID = id

	override fun isNew(): Boolean = isNewEntity

	@PostPersist
	@PostLoad
	fun markNotNew() {
		isNewEntity = false
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is BaseEntity) return false
		return id == other.id
	}

	override fun hashCode(): Int = id.hashCode()

	override fun toString(): String = "${this::class.simpleName}(id=$id)"
}
