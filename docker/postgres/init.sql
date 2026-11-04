-- Runs once, the first time the Postgres volume is created.
-- One database per service: no service may read another service's tables.
CREATE DATABASE orders;
CREATE DATABASE inventory;
