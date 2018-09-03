unit-test:
	mvn clean test

run-dev-env:
	docker-compose -f docker-compose.dev.yml pull && \
	docker-compose -f docker-compose.dev.yml down && \
	docker-compose -f docker-compose.dev.yml build && \
	docker-compose -f docker-compose.dev.yml up --remove-orphans
