<img src="doc/image/trouve_pic.png" width="70%" syt height="30%" />

## Trouve : Simple, convenient, and fast! A built-in integrated service discovery, service registration, and service forwarding general component for Spring projects, compared to the need for independently deployed services like Zookeeper, Nacos, etc., it is easier and more convenient to use and deploy.


[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

--------

## Introduction

Latest version：

```xml
<dependency>
    <groupId>com.lei6393.trouve</groupId>
    <artifactId>trouve-client</artifactId>
    <version>1.1.0</version>
</dependency>
```

```xml
<dependency>
    <groupId>com.lei6393.trouve</groupId>
    <artifactId>trouve-server</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Client-side usage

### 1. Introduce the dependency package
```xml
<dependency>
    <groupId>com.lei6393.trouve</groupId>
    <artifactId>trouve-client</artifactId>
    <version>LATEST</version>
</dependency>
```

### 2. To add an annotation to the Spring Boot startup class

```java
@EnableTrouveRegistry(
        value = "test_service_name",  // servie name. Each accessing service needs to be given a separate name
        serverAddresses = @ServerAddress(schema = "http", host = "127.0.0.1", port = 8279) // Obtain the server address of the trouve service, prioritize getting it from the configuration, and use annotation if the configuration is empty
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

### 3. To expose a RestController or API, simply add @ExposeApi on it

- Adding it to the class will expose all APIs within the class
- Adding it to the API method will only expose that API

example ：
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

### 4. Supported configuration properties:
```properties
# trouve Supports automatic IP acquisition. If the automatically acquired IP is not usable, this property can be used to specify an IP
trouve.client.ip=168.0.0.1

# trouve Supports automatic port acquisition (dependent on Spring's default configuration server.port). If the automatically acquired port is not usable, this property can be used to specify a port                    
trouve.client.port=8888

# spring Defaults to exposing the port, will prioritize acquiring this port
server.port= 9999

# The preferred trouve service server address, supports passing multiple values, separated by ','          
trouve.server.address=http://127.0.0.1:8888
```

## Server-side usage

### 1. Introduce the dependency package

```xml
<dependency>
  <groupId>com.lei6393.trouve</groupId>
  <artifactId>trouve-server</artifactId>
  <version>LATEST</version>
</dependency>
```

### 2. Add annotation `@EnableTrouveDiscover("openapi")` to the startup class 

example：
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
Mandatory item is namespace, each server service must be set with a unique value




### 3. Configure the entry point for service forwarding：`TrouveRequestDispatcher.entrance(request, response);`

example：

```java
@RestController
public class EntranceController {


    @RequestMapping("**")
    public void entrance(HttpServletRequest request,
                         HttpServletResponse response) throws Throwable {
        TrouveRequestDispatcher.entrance(request, response);
    }
}
```



### 4. Usage in cluster mode:

- The server side of trouve defaults to single-machine mode
- "To enable cluster mode (implemented through Redis), the following parameters need to be configured:
```properties
# Enable flag
trouve.server.redis.enable=true
# redis address                      
trouve.server.redis.singleServer=127.0.0.1:6379
# redis password, if none, can be left blank
trouve.server.redis.password=123456
```          
