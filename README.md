# shop-events

Three Spring Boot services that place an order through an event-driven saga, built one step at a time.
Each step is a Git commit and a tag (`step-1`, `step-2`, ...). See what a step added with:

```
git log --oneline
git diff step-1 step-2 --stat
git checkout step-1        # look at an older step; `git checkout main` to come back
```

Commands below work in **cmd, PowerShell and Git Bash** unless marked otherwise.

## Step 1: Skeleton and infrastructure

What's here:
- `pom.xml`: a Maven **multi-module** parent. Services are added as modules in later steps.
- `events/`: the shared event contract (records like `OrderPlaced`, topic names, header names).
  Every service depends on it, so producers and consumers can't disagree about field names.
- `docker-compose.yml`: Postgres (two databases, `orders` and `inventory`), Redis and Kafka.

Why one database per service: if inventory-service could read the `orders` table, the two teams
could never change their schemas independently. Services share **events**, never tables.

Run it (stop infra-lab first with `docker compose down` in that folder, since the ports clash):

```
docker compose up -d
docker compose ps
docker exec -it shop-postgres psql -U shop -c "\l"
```

You should see the `orders` and `inventory` databases. Kafka UI is at http://localhost:8090.

## Step 2: order-service with the transactional outbox

What's new (`order-service/`, port 8081):
- `POST /orders` saves the order **and** an `OrderPlaced` row in the `outbox` table, in one transaction
  (`OrderService.placeOrder`). It returns **202 Accepted** with status `PENDING`.
- `OutboxRelay` polls the outbox every 500 ms, publishes unsent rows to the `order-events` topic
  (key = orderId, headers `eventType` and `eventId`), then marks them published.
- `FOR UPDATE SKIP LOCKED` in `OutboxRepository` lets several instances run the relay without
  publishing the same row twice at the same time.
- Flyway (`db/migration/V1__...sql`) creates the schema; Hibernate only validates it.

Run it: start the infrastructure (Step 1), then run `OrderServiceApplication` in IntelliJ,
or from the project root:

```
mvn -pl order-service -am spring-boot:run
```

(`-pl order-service -am` = build this module plus the modules it depends on, i.e. `events`.)

Place an order. **cmd:**

```
curl -X POST http://localhost:8081/orders -H "Content-Type: application/json" -d "{\"customerId\":\"c1\",\"sku\":\"BOOK-1\",\"quantity\":2}"
```

**PowerShell:**

```
Invoke-RestMethod -Method Post http://localhost:8081/orders -ContentType "application/json" -Body '{"customerId":"c1","sku":"BOOK-1","quantity":2}'
```

Then look at each layer:

```
docker exec -it shop-postgres psql -U shop -d orders -c "select id, status from orders"
docker exec -it shop-postgres psql -U shop -d orders -c "select event_type, aggregate_id, published_at from outbox"
docker exec -it shop-kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic order-events --from-beginning --property print.key=true --property print.headers=true
```

The status stays `PENDING` for now: nobody consumes `OrderPlaced` until Step 3.

**Experiment: the outbox protects you from Kafka outages.**

1. `docker stop shop-kafka`
2. Place two orders. They succeed (202), because placing an order never touches Kafka.
3. Check the outbox: both rows have `published_at` empty. The app log shows the relay failing and retrying.
4. `docker start shop-kafka`. Within seconds the relay publishes both, and `published_at` fills in.

Compare with the naive version (save, then `kafkaTemplate.send()` in the same method): with Kafka down
you'd have to either fail the order or save it and lose the event.

**Think about:** the relay publishes, then marks the row. If the app dies between those two steps, the
event is sent again on restart. That's why every consumer in the next steps checks `eventId`.
