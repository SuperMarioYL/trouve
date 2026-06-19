# Changelog

All notable changes to this project are documented here. This project uses semantic versioning.

> **Two release lines.** The `3.x` line targets **Spring Boot 3 / Jakarta EE / Java 17+**.
> The `1.x` line (Spring Boot 2.x / javax / Java 11+) is maintained in parallel so existing
> Boot 2 users are not forced to migrate. `3.0.0` contains every feature of `1.2.0` plus the
> platform migration below.

## [3.0.0] — unreleased (Spring Boot 3.x line)

### Changed (breaking)
- **Spring Boot 2.2.13 → 3.2.x**, **Java 11 → 17**, Jakarta EE namespaces.
- `javax.servlet.*` → `jakarta.servlet.*` throughout.
- Spring 6 made `HttpMethod` a class (no longer an enum) — internal dispatch rewritten accordingly.
- `MediaType.APPLICATION_JSON_UTF8_VALUE` → `APPLICATION_JSON_VALUE`.
- Auto-configuration registration moved from `META-INF/spring.factories` to
  `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` with
  `@AutoConfiguration`.
- Build: maven-compiler 3.13.0 / source 3.3.0 / javadoc 3.6.3.

### Included from 1.2.0
All hardening / resilience / security / observability / zero-config-starter work — retry-with-failover,
circuit breaker, concurrency limit, body-size cap, active health probing, control-plane auth,
graceful drain, trace passthrough, built-in metrics + `/trouve/manager/*` endpoints, spring-boot-starter.
See the 1.2.0 entry on the `1.x` line for the full list.

### Migration notes
- Requires **Java 17+** and **Spring Boot 3.x**. Not drop-in for Boot 2 apps.
- Same Redis cluster-mode codec change as 1.2.0 (JDK serialization → JSON): upgrade all nodes together.

## [1.2.0] (Spring Boot 2.x line)

Hardening + resilience + security + observability + zero-config DX; all opt-in / behaviour-compatible.
Tracked on the `1.x` release line — see that line's CHANGELOG for the full entry.

## [1.1.0]

Baseline release (embedded service discovery / registration / forwarding for Spring Boot 2.x).
