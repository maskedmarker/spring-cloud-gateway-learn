
```text
Spring Cloud Gateway 的高性能源于其底层的技术栈：

Spring WebFlux：基于 Reactor 库实现响应式编程模型。
Netty：作为高性能的异步非阻塞网络通信框架。
```

## 三个核心概念

```text
Route, Predicate, Filter

这是理解 Spring Cloud Gateway 的基石，它们共同定义了一条路由规则：

Route（路由）：
网关中最基本的组件。它就像一个规则配置单，包含一个唯一的 ID、一个目标 URI、一组用于匹配的 Predicate 和一组用于处理的 Filter。

Predicate（谓词）：
匹配条件。这是 Java 8 中的一个函数式接口，它接收一个请求，然后返回一个布尔值，判断这个请求是否符合规则。Spring Cloud Gateway 内置了非常丰富的 Predicate 工厂，例如：
    Path：基于请求路径匹配（如 /user/**）。
    Method：基于 HTTP 方法匹配（如 GET, POST）。
    Header：基于请求头匹配。
    Cookie、Query、Host、时间（Before/After/Between）等。
    
Filter（过滤器）：
处理逻辑。过滤器可以在请求被转发到后端服务之前（pre）和之后（post）对请求/响应进行加工。和 Predicate 类似，它也分为两种：
    GatewayFilter： 路由过滤器。需要通过配置绑定到某个具体的路由规则上，作用于该规则下的请求。
    GlobalFilter： 全局过滤器。作用于所有路由规则，通常用于实现一些通用的功能，如权限校验、请求日志等。
```


```text
工作流程深度解析



当一个请求抵达 Spring Cloud Gateway 时，它会经历以下关键步骤：

1. 请求匹配 (HandlerMapping)：请求首先会被 RoutePredicateHandlerMapping 接管。
    它会遍历所有配置好的路由，依次执行每个路由的 Predicate 逻辑。一旦找到第一个所有 Predicate 都满足的路由，匹配过程就会停止，并将这个路由信息（如目标地址、过滤器列表等）绑定到请求上下文（ServerWebExchange）中。

2. 过滤器链执行 (WebHandler)：匹配成功后，请求会被交给 FilteringWebHandler。这个处理器会创建一个 过滤器链，它是整个网关处理逻辑的核心。这个链由三部分组成：
与该路由绑定的 GatewayFilter。
全局生效的 GlobalFilter。
默认的 defaultFilter。

3. 过滤器排序与执行：所有过滤器会被合并到一个列表里，并根据 Ordered 接口或 @Order 注解指定的优先级进行排序。order 值越小，优先级越高，越先执行。
Pre 阶段：按照顺序执行所有过滤器的“前置”逻辑，例如参数校验、权限验证、日志记录等。
请求转发：这是过滤器链中的一个关键步骤，通常由 NettyRoutingFilter 这样的过滤器负责。它会基于第一步匹配到的目标 URI，使用非阻塞的 HTTP 客户端（基于 Netty）将请求转发给下游的微服务。
Post 阶段：当下游服务返回响应后，过滤器链会以 逆序 执行所有过滤器的“后置”逻辑，用于修改响应体、添加响应头等。

4. 响应返回：经过完整过滤器链处理后的最终响应，会被返回给客户端。
```

```text
过滤器链的执行顺序


在自定义过滤器时，了解其执行顺序至关重要。三者合并后的排序规则如下：
默认过滤器（defaultFilter）
路由过滤器（GatewayFilter）
全局过滤器（GlobalFilter）
```