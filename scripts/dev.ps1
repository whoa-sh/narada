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
