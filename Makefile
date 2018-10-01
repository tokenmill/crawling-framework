unit-test:
	mvn clean test

run-dev-env:
	docker-compose -f docker-compose.dev.yml pull && \
	docker-compose -f docker-compose.dev.yml down && \
	docker-compose -f docker-compose.dev.yml build && \
	docker-compose -f docker-compose.dev.yml up --remove-orphans

build-base-docker:
	docker build -f Dockerfile.base -t registry.gitlab.com/tokenmill/crawling-framework/deps:latest .

publish-base-docker: build-base-docker
	docker push registry.gitlab.com/tokenmill/crawling-framework/deps:latest
