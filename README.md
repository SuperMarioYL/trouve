<p align="center">
  <img src="doc/image/trouve-banner.svg" width="100%" alt="Trouve — embedded service discovery, registration and forwarding for Spring Boot" />
</p>

<p align="center">
  <b>Simple, convenient, and fast.</b> A built-in, integrated <b>service discovery + registration + forwarding</b> component for Spring projects —
  no separately deployed registry (Zookeeper / Nacos / Eureka) required.
</p>

<p align="center">
  <a href="https://www.apache.org/licenses/LICENSE-2.0.html"><img src="https://img.shields.io/badge/license-Apache%202.0-4EB1BA.svg" alt="License"></a>
  <img src="https://img.shields.io/badge/JDK-11%2B-2DD4BF.svg" alt="JDK 11+">
  <img src="https://img.shields.io/badge/Spring%20Boot-2.x-6DB33F.svg" alt="Spring Boot 2.x">
  <img src="https://img.shields.io/badge/build-Maven-C71A36.svg" alt="Maven">
  <img src="https://img.shields.io/badge/version-1.1.0-informational.svg" alt="version">
</p>

<p align="center"><a href="doc/README_zh.md">中文版</a></p>

--------

## Why Trouve

Trouve embeds the registry **inside your own Spring Boot apps**. Providers expose APIs with a single annotation,
and a Trouve server (also just a Spring app) discovers them and forwards traffic — so you get service discovery
and an HTTP gateway without standing up and operating a separate cluster.

| | **Trouve** | Nacos | Eureka | Zookeeper |
| --- | :---: | :---: | :---: | :---: |
| Separate registry to deploy | **Not required** (embedded) | Required | Required | Required |
| Service registration | Annotation (`@EnableTrouveRegistry`) | SDK / config | SDK | Client recipes |
| Expose an API | `@ExposeApi` | n/a | n/a | n/a |
| Built-in request forwarding | **Yes** (gateway) | No | No | No |
| Cluster mode | Redis (optional) | Raft | Peer replication | ZAB |

## Architecture

<p align="center">
  <img src="doc/image/trouve-architecture.svg" width="92%" alt="Trouve architecture diagram" />
</p>

- **Providers** register themselves (heartbeat + exposed-API metadata) to the Trouve server on a schedule.
- The **Trouve server** keeps a `url → instances` routing table, health-checks instances, and on each request
  **matches** the route, **load-balances** across healthy instances, and **forwards** via OkHttp.
- **Single-machine mode** by default; enable **cluster mode** with Redis to share state across server nodes.

--------

## Zero-config (Spring Boot starter)

Trouve auto-configures from properties alone — **no `@Enable...` annotations required**. The annotation-based usage below still works and takes priority when both are present.

**Provider** — set a service name and the server address, then annotate the APIs you expose with `@ExposeApi`:

```properties
trouve.client.service-name=my-service
trouve.server.address=http://127.0.0.1:8279
```

**Server** — set a namespace; optionally auto-register the forwarding entrance instead of writing an `EntranceController`:

```properties
trouve.server.namespace=openapi
# optional: auto-register the catch-all forwarding entrance (default false)
trouve.server.auto-entrance=true
```

--------

## Quick start — Client side

### 1. Add the dependency

```xml
<dependency>
    <groupId>com.lei6393.trouve</groupId>
    <artifactId>trouve-client</artifactId>
    <version>1.1.0</version>
</dependency>
```

### 2. Annotate your Spring Boot application class

```java
@EnableTrouveRegistry(
        value = "test_service_name",  // service name — each accessing service needs its own name
        serverAddresses = @ServerAddress(schema = "http", host = "127.0.0.1", port = 8279) // Trouve server address; configuration takes priority, annotation is the fallback
)
@SpringBootApplication
public class ClientTestApp {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(ClientTestApp.class);
    application.setDefaultProperties(Collections.<String, Object>singletonMap("server.port", "8278"));
    application.run(args);
  }
}
```

### 3. Expose a RestController or API with `@ExposeApi`

- On a class: exposes all APIs within the class.
- On a method: exposes only that API.

```java
// expose all
@RestController
@RequestMapping("/expose/all")
@ExposeApi
public class ExposeAllMethodController {

    @RequestMapping(value = "/{path}/one", produces = "application/json")
    @ResponseBody
    public String testMethod1() {
        return "{\"message\":\"success call client service\"}";
    }

    @RequestMapping(value = "/{path}/two", produces = "application/json")
    @ResponseBody
    public String testMethod2() {
        return "{\"message\":\"success call client service\"}";
    }
}


// expose alone
@RestController
@RequestMapping("/expose/alone")
public class ExposeAloneMethodController {

  @RequestMapping(value = "/{path}/true", produces = "application/json")
  @ResponseBody
  @ExposeApi
  public String testMethodOne() {
    return "{\"message\":\"success call client service\"}";
  }

  @RequestMapping(value = "/{path}/false", produces = "application/json")
  @ResponseBody
  public String testMethodTwo() {
    return "{\"message\":\"success call client service\"}";
  }
}
```

### 4. Supported configuration properties

```properties
# Trouve supports automatic IP acquisition. If the auto-acquired IP is unusable, specify one here
trouve.client.ip=168.0.0.1

# Trouve supports automatic port acquisition (based on Spring's server.port). If unusable, specify one here
trouve.client.port=8888

# Spring's default exposed port — acquired with priority
server.port= 9999

# Preferred Trouve server address(es); multiple values supported, separated by ','
trouve.server.address=http://127.0.0.1:8888
```

--------

## Quick start — Server side

### 1. Add the dependency

```xml
<dependency>
  <groupId>com.lei6393.trouve</groupId>
  <artifactId>trouve-server</artifactId>
  <version>1.1.0</version>
</dependency>
```

### 2. Add `@EnableTrouveDiscover("openapi")` to the application class

```java
@SpringBootApplication
@EnableTrouveDiscover("openapi")
public class ServerSingletonTestApp {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(ServerSingletonTestApp.class);
    application.setDefaultProperties(Collections.<String, Object>singletonMap("server.port", "8279"));
    application.run(args);
  }
}
```

The mandatory item is the namespace — each server service must be given a unique value.

### 3. Configure the forwarding entry point: `TrouveRequestDispatcher.entrance(request, response)`

```java
@RestController
public class EntranceController {

    @RequestMapping("/**")
    public void entrance(HttpServletRequest request,
                         HttpServletResponse response) throws Throwable {
        TrouveRequestDispatcher.entrance(request, response);
    }
}
```

### 4. Cluster mode

- The Trouve server runs in single-machine mode by default.
- To enable cluster mode (backed by Redis), configure:

```properties
# enable flag
trouve.server.redis.enable=true
# redis address
trouve.server.redis.singleServer=127.0.0.1:6379
# redis password — leave blank if none
trouve.server.redis.password=123456
```

--------

## Observability & operations

Built-in management endpoints (gated by `trouve.server.token` when auth is enabled):

| Endpoint | Description |
| --- | --- |
| `GET /trouve/manager/dashboard` | Lightweight live dashboard (HTML, auto-refreshes every 5s) |
| `GET /trouve/manager/metrics` | Forward metrics as JSON |
| `GET /trouve/manager/prometheus` | Forward metrics in Prometheus text exposition format |
| `GET /trouve/manager/health/instances` | Healthy instance ids |

Optional, **default-off** resilience / security / ops capabilities (configured via
`@EnableTrouveDiscover(dispatchHttpProperty = @DispatchHttpProperty(...))`, or properties where noted):

- **Retry with failover** — retry a failed forward against a *different* healthy instance.
- **Per-instance circuit breaker** — eject instances that fail repeatedly, half-open recovery.
- **Concurrency limit** — cap in-flight forwards; shed load with `503` past the limit.
- **Request body-size cap** — reject oversized bodies with `413`.
- **Active HTTP health probing** — probe instances with hysteresis on top of passive heartbeats.
- **Control-plane auth** — shared token on register / heartbeat / meta (`trouve.server.token` + `trouve.client.token`).
- **Graceful drain** — finish in-flight forwards on shutdown (`trouve.server.shutdown-drain-millis`).
- **Trace passthrough** — forwards `traceparent` / `b3`; adds an `X-Request-Id` when absent.

--------

## Modules

| Module | Responsibility |
| --- | --- |
| `trouve-core` | Shared data models (Instance / Meta / ServiceInfo), utilities, exceptions, events |
| `trouve-client` | `@EnableTrouveRegistry` + `@ExposeApi`; scheduled heartbeat & metadata reporting |
| `trouve-server` | `@EnableTrouveDiscover`; routing table, health check, load balancing, request forwarding |
| `trouve-examples` | Runnable client / singleton-server / cluster-server examples |

## License

Licensed under the [Apache License 2.0](LICENSE).
