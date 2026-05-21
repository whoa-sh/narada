test:
	./gradlew --no-daemon test

lint:
	./gradlew --no-daemon ktlintCheck

compose-up:
	docker compose up --build

compose-down-v:
	docker compose down -v
