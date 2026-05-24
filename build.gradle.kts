/*
 * SPDX-License-Identifier: AGPL-3.0-only OR SSPL-1.0 OR Elastic-2.0
 * Copyright (c) 2026 Subhrodip Mohanta (whoa.sh). All rights reserved.
 */
plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.spring)
	alias(libs.plugins.kotlin.jpa)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependency.management)
	alias(libs.plugins.ktlint)
	alias(libs.plugins.spotless)
}

group = "sh.whoa"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencyLocking {
	lockAllConfigurations()
}

dependencies {
	implementation(libs.spring.boot.starter.actuator)
	implementation(libs.spring.boot.starter.data.jpa)
	implementation(libs.spring.boot.starter.flyway)
	implementation(libs.spring.boot.starter.webmvc)
	implementation(libs.jackson.module.kotlin)
	implementation(libs.kotlin.reflect)
	implementation(libs.flyway.postgresql)
	developmentOnly(libs.spring.boot.devtools)
	runtimeOnly(libs.postgresql)
	testImplementation(libs.spring.boot.starter.actuator.test)
	testImplementation(libs.spring.boot.starter.data.jpa.test)
	testImplementation(libs.spring.boot.starter.webmvc.test)
	testImplementation(libs.kotlin.test.junit5)
	testImplementation(libs.mockk)
	testImplementation(libs.spring.boot.testcontainers)
	testImplementation(libs.testcontainers.junit.jupiter)
	testImplementation(libs.testcontainers.postgresql)
	testRuntimeOnly(libs.junit.platform.launcher)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

ktlint {
	verbose.set(true)
	filter {
		exclude("**/generated/**")
	}
}

spotless {
	kotlin {
		target("src/**/*.kt")
		licenseHeaderFile(rootProject.file("config/license/HEADER_SLASHSTAR.txt"))
	}
	kotlinGradle {
		target("*.gradle.kts", "gradle/**/*.gradle.kts")
		licenseHeaderFile(rootProject.file("config/license/HEADER_SLASHSTAR.txt"), "(plugins|import|buildscript|pluginManagement|rootProject)")
	}
	format("hashHeader") {
		target(".github/**/*.yml", ".github/**/*.yaml", "scripts/**/*.ps1", "src/**/*.properties")
		targetExclude("src/test/resources/application.properties")
		licenseHeaderFile(rootProject.file("config/license/HEADER_HASH.txt"), "([^#\\r\\n])")
	}
	format("sqlHeader") {
		target("src/**/*.sql")
		licenseHeaderFile(rootProject.file("config/license/HEADER_SQL.txt"), "(?i)(create|alter|insert|update|delete|select)")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.named("check") {
	dependsOn("ktlintCheck")
	dependsOn("spotlessCheck")
}
