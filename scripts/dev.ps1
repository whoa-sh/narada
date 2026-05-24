# SPDX-License-Identifier: AGPL-3.0-only OR SSPL-1.0 OR Elastic-2.0
# Copyright (c) 2026 Subhrodip Mohanta (whoa.sh). All rights reserved.
param(
	[Parameter(Mandatory = $true)]
	[ValidateSet("test", "lint", "compose-up", "compose-down-v")]
	[string]$Task
)

switch ($Task) {
	"test" { .\gradlew.bat --no-daemon test }
	"lint" { .\gradlew.bat --no-daemon ktlintCheck }
	"compose-up" { docker compose up --build }
	"compose-down-v" { docker compose down -v }
}
