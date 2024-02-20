<img src="doc/image/trouve_pic.png" width="70%" syt height="30%" />

## trouve : 基于 Spring 的一款集成服务发现、服务注册、服务转发的通用 SDK


[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

-------

## 介绍

最新版本：

```xml
<dependency>
    <groupId>com.lei6393.trouve</groupId>
    <artifactId>trouve-client</artifactId>
    <version>1.0.5</version>
</dependency>
```

```xml
<dependency>
    <groupId>com.lei6393.trouve</groupId>
    <artifactId>trouve-server</artifactId>
    <version>1.0.5</version>
</dependency>
```

## Client 端使用方式

### 1. 在 pom.xml 中引入依赖：
```xml
<dependency>
    <groupId>com.lei6393.trouve</groupId>
    <artifactId>trouve-client</artifactId>
    <version>LATEST</version>
</dependency>
```

### 2. 在 spring 启动类上加入注解

```java
@EnableTrouveRegistry(
        value = "test_service_name",  // servie name, 每个接入服务需要单独起名字
        serverAddresses = @ServerAddress(schema = "http", host = "127.0.0.1", port = 8279) // trouve 服务端地址，从配置优先获取，如果配置为空则使用注解
)
@SpringBootApplication
public class ClientTestApp {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(ClientTestApp.class);
    // 设置默认端口
    application.setDefaultProperties(Collections.<String, Object>singletonMap("server.port", "8278"));
    application.run(args);
  }
}
```

### 3. 在需要暴露的 RestController 或者 api 上加上 @ExposeApi 即可

- 加在 class 上会将 class 里所有 api 暴露出去
- 加在 API 方法上则只将该API暴露出去

例子：
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

### 4. 支持配置的属性：
```properties
# trouve 支持自动获取IP，如果自动获取的IP无法使用，可以通过该属性指定IP
trouve.client.ip=168.0.0.1

# trouve 支持自动获取 port（依赖 spring 默认配置 server.port ），如果自动获取的 port 无法使用，可以通过该属性指定port                    
trouve.client.port=8888

# spring 默认暴露端口，会优先获取该端口 
server.port= 9999

# 优先获取的 trouve 服务端地址，支持传多个值，用","分割                               
trouve.server.address=http://127.0.0.1:8888
```

## Server 端使用方式

### 1. 引入依赖包：

```xml
<dependency>
  <groupId>com.lei6393.trouve</groupId>
  <artifactId>trouve-server</artifactId>
  <version>LATEST</version>
</dependency>
```

### 2. 在启动类加入注解 `@EnableTrouveDiscover("openapi")`

举例：
```java
@SpringBootApplication
@EnableTrouveDiscover("openapi")
public class ServerSingletonTestApp {

  public static void main(String[] args) {
    SpringApplication application = new SpringApplication(ServerSingletonTestApp.class);
    // 设置默认端口
    application.setDefaultProperties(Collections.<String, Object>singletonMap("server.port", "8279"));
    application.run(args);
  }
}
```
必填项为 namespace 每一个 server 服务要设置一个唯一值




### 3. 配置服务转发的入口：`TrouveRequestDispatcher.entrance(request, response);`

举例：

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



### 4. 集群模式使用方式：

- trouve  的 server 端默认开启单机模式
- 如果要开启集群模式（通过Redis实现）需要配置如下参数：
```properties
# 开启标识
trouve.server.redis.enable=true
# redis 地址                      
trouve.server.redis.singleServer=127.0.0.1:6379
# redis 密码，如果没有，可不填     
trouve.server.redis.password=123456
```          
