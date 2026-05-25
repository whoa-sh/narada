/*
 * SPDX-License-Identifier: AGPL-3.0-only OR SSPL-1.0 OR Elastic-2.0
 * Copyright (c) 2026 Subhrodip Mohanta (whoa.sh). All rights reserved.
 */
package sh.whoa.narada

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class NaradaApplication

fun main(args: Array<String>) {
	runApplication<NaradaApplication>(*args)
}
