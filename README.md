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
