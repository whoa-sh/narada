/*
 * SPDX-License-Identifier: AGPL-3.0-only OR SSPL-1.0 OR Elastic-2.0
 * Copyright (c) 2026 Subhrodip Mohanta (whoa.sh). All rights reserved.
 */
package sh.whoa.narada

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest
@Testcontainers
class NaradaApplicationTests {
	companion object {
		@Container
		@ServiceConnection
		@JvmStatic
		val postgres = PostgreSQLContainer("postgres:17-alpine")
	}

	@Test
	fun contextLoads() {
	}
}
