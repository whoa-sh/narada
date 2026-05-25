/*
 * SPDX-License-Identifier: AGPL-3.0-only OR SSPL-1.0 OR Elastic-2.0
 * Copyright (c) 2026 Subhrodip Mohanta (whoa.sh). All rights reserved.
 */
package sh.whoa.narada.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BaseEntityTest {
	private class TestEntity : BaseEntity()

	@Test
	fun `new entity should have UUIDv7 ID and be marked as new`() {
		val entity = TestEntity()
		assertNull(entity.createdAt)
		assertNull(entity.updatedAt)
		assertTrue(entity.isNew)
		assertEquals(7, entity.getId().version())
	}

	@Test
	fun `markNotNew should transition isNew to false`() {
		val entity = TestEntity()
		assertTrue(entity.isNew)
		entity.markNotNew()
		assertFalse(entity.isNew)
	}

	@Test
	fun `entities with same ID should be equal`() {
		val entity1 = TestEntity()
		val id = entity1.getId()

		// Using reflection to create another entity with the same ID
		val entity2 =
			object : BaseEntity() {
				init {
					val idField = BaseEntity::class.java.getDeclaredField("id")
					idField.isAccessible = true
					idField.set(this, id)
				}
			}

		assertEquals(entity1, entity2)
		assertEquals(entity1.hashCode(), entity2.hashCode())
		assertTrue(entity1.equals(entity2))
		assertTrue(entity1.equals(entity1))
	}

	@Test
	fun `entities with different IDs should not be equal`() {
		val entity1 = TestEntity()
		val entity2 = TestEntity()

		assertNotEquals(entity1, entity2)
		assertNotEquals(entity1.hashCode(), entity2.hashCode())
		assertFalse(entity1.equals(entity2))
		assertFalse(entity1.equals(null))
		assertFalse(entity1.equals("not an entity"))
	}

	@Test
	fun `isNew should be accessible via getId and isNew`() {
		val entity = TestEntity()
		assertEquals(entity.getId(), entity.id)
		assertEquals(true, entity.isNew())
	}

	@Test
	fun `toString should contain class name and ID`() {
		val entity = TestEntity()
		val toString = entity.toString()
		assertTrue(toString.contains("TestEntity"))
		assertTrue(toString.contains(entity.getId().toString()))
	}
}
