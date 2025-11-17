.SILENT:
.PHONY: build

# Containers
all:

ifeq ($(dev),up)
all: dev-up
else ifeq ($(dev),stop)
all: dev-stop
else ifeq ($(dev),down)
all: dev-down
else ifeq ($(prod),up)
all: prod-up
else ifeq ($(prod),down)
all: prod-down
else ifeq ($(sonar),up)
all: sonar-up
else ifeq ($(sonar),down)
all: sonar-down
endif

prod-up:
	docker compose -f deployment/prod/compose.yml up -d --build
	docker image prune -a -f

prod-down:
	docker compose -f deployment/prod/compose.yml down

# Development
dev-up:
	docker compose -f deployment/dev/compose.yml up -d

dev-stop:
	docker compose -f deployment/dev/compose.yml stop

dev-down:
	docker compose -f deployment/dev/compose.yml down
	docker volume prune -a -f

check-install:
	@if [ ! -d "./web/node_modules" ]; then \
		echo "Installing nodejs dependencies..."; \
		cd ./web && bun install; \
	fi

ui-build: check-install
	cd ./web && bun run build

ui-dev: check-install
	cd ./web && bun run dev

build: ui-build
	./gradlew build copyDeps

api:
	./scripts/watch.sh ./app/src ./server/src 

dev: 
	make -j2 ui-dev api

run:
	java -jar app/build/libs/app.jar
	
test:
	./gradlew test

clean: 
	./gradlew clean --no-problems-report
	rm -rf ./web/node_modules
	rm -rf ./app/src/main/resources/static/ui
	rm -rf .gradle build


