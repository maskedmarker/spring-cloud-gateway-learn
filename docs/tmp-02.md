
```text
HttpWebHandlerAdapter
WebFilter
DispatcherHandler
    HandlerMapping
        RoutePredicateHandlerMapping
    FilteringWebHandler
```


```text
WebFilter
GlobalFilter
GatewayFilter


WebFilter 是 Spring WebFlux 的底层通用接口，作用于所有 Web 请求，范围最广。
GlobalFilter 是 Spring Cloud Gateway 定义的路由专用接口，作用于所有匹配的路由，但无法直接修改请求路径。
GatewayFilter 是 Spring Cloud Gateway 的路由特定接口，只作用于配置了它的那条路由。




特性	                    WebFilter	                        GlobalFilter	                        GatewayFilter
所属框架	                Spring WebFlux	                    Spring Cloud Gateway	                Spring Cloud Gateway
作用范围	                全局，处理进入Gateway的每一个请求	    全局，作用于通过Gateway路由的每一个请求	    局部，只作用于配置了它的特定路由
是否可配置路由ID	        否	                                否	                                    是
能否修改请求路径	        ✅ 可以	                            ❌ 不可以	                            ✅ 可以
主要用途	                通用Web任务，如日志、CORS、安全	        路由相关任务，如负载均衡、熔断、核心转发	    特定路由任务，如路径重写、添加请求头
如何启用	                实现接口并注册为@Component	            实现接口并注册为@Component	                通过 application.yml 或代码配置在具体路由下


GlobalFilter：与 WebFilter 类似，定义后也作用于所有路由。但它与 Gateway 的路由机制紧密集成，可以访问 Route 对象。一个重要的限制是，GlobalFilter 不能修改请求的路径，否则会导致路由匹配失败。这也是为什么路径重写等功能必须用 GatewayFilter 来实现。
GatewayFilter：它必须通过配置文件或代码显式地绑定到某条路由上。你可以理解为，它是专为某条路由定制的处理逻辑。
```