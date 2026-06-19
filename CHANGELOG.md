# Changelog

All notable changes to this project are documented here. Format loosely follows
[Keep a Changelog](https://keepacharchangelog.com/); this project uses semantic versioning.

## [1.2.0] — unreleased (Spring Boot 2.x line)

Hardening + resilience + security + observability + zero-config DX. Every new
capability is **opt-in or behaviour-compatible**; the public API
(`TrouveRequestDispatcher.entrance`, the annotations) is unchanged.

### Added
- **Retry with failover** — a failed forward retries against a *different* healthy instance
  (configurable via `@DispatchHttpProperty(retryCount=...)`; default 0).
- **Per-instance circuit breaker** — eject repeatedly-failing instances, half-open recovery
  (`circuitBreakerEnabled`, default off).
- **Entrance concurrency limit** — cap in-flight forwards, shed excess with `503`
  (`maxConcurrentRequests`, default unlimited).
- **Request body-size cap** — reject oversized bodies with `413` (`maxBodyBytes`, default unlimited).
- **Active HTTP health probing** — probe instances with hysteresis on top of passive heartbeats
  (`activeHealthCheckEnabled`, default off).
- **Control-plane authentication** — shared token on register / heartbeat / meta
  (`trouve.server.token` + `trouve.client.token`; default off).
- **Graceful drain** — finish in-flight forwards on shutdown (`trouve.server.shutdown-drain-millis`).
- **Trace passthrough** — forwards `traceparent` / `b3`; adds `X-Request-Id` when absent.
- **Built-in metrics & management endpoints** — `GET /trouve/manager/{metrics,prometheus,dashboard,health/instances}`
  (token-gated). Prometheus text exposition format; lightweight live HTML dashboard.
- **spring-boot-starter auto-configuration** — properties-only setup for client and server
  (`trouve.client.service-name`, `trouve.server.namespace`); optional auto-registered entrance
  (`trouve.server.auto-entrance`); IDE autocomplete via `spring-configuration-metadata`.

### Fixed
- Inverted retry math (`Math.min` → `Math.max`) — retries now actually execute.
- Forward total-failure now returns **502 / 504** instead of a misleading near-empty `200`.
- Exception boundary in `entrance`: unregistered route → **404**, no instance → **503**,
  other Trouve errors → **500** (previously a raw 500 + leaked stack trace).
- `Matcher` is now thread-safe (`ConcurrentHashMap` + immutable snapshots + cached route condition);
  null-guarded read path (no more NPE on miss / concurrent replace).
- `HealthChecker` fail-open fixed: once initialized, an empty health set means *no* healthy instances.
- Three `java.util.Timer` instances replaced with a shared `ScheduledExecutorService`
  (a single task throwing no longer silently kills heartbeats / health checks / matcher flushes).
- Thread-safe lazy init for the forwarding `OkHttpClient` and the load-balance policy.
- Client registry-address round-robin failover (was pinned to one arbitrary address).
- Removed the dead `fastjson` version pin.

### Security
- **Cluster mode RCE fix** — replaced the Redisson JDK `SerializationCodec` (a deserialization
  RCE primitive on untrusted data) with a JSON codec.

### Migration notes
- **Redis cluster mode wire-format change**: the registry codec changed from JDK serialization to
  JSON. Entries written by 1.1.0 nodes are not readable by 1.2.0 nodes and vice-versa — upgrade all
  cluster nodes together (or let the registry repopulate from heartbeats after upgrade).
- Callers that (incorrectly) treated a failed forward as a `200` will now see `502/504`.

## [1.1.0]

Baseline release (embedded service discovery / registration / forwarding for Spring Boot 2.x).
