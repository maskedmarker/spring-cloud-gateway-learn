package org.example.learn.spring.cloud.gateway.hello.config;


import org.example.learn.spring.cloud.gateway.hello.filter.GrayReleaseFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 灰度路由配置类
 * 配置基于染色标记的动态路由规则
 */
@Configuration
public class GrayRouteConfig {

    /**
     * 配置灰度路由规则
     * 根据请求头中的染色标记，将流量路由到不同的服务版本
     */
    @Bean
    public RouteLocator grayRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 灰度版本路由 - 匹配带有灰度标记的请求
                .route("service_gray", r -> r
                        .path("/service/**")
                        .and().header(GrayReleaseFilter.GRAY_HEADER, GrayReleaseFilter.GRAY_VALUE)
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Route-Version", "gray")
                                .addResponseHeader("X-Version", "gray-v1.0"))
                        // 灰度服务地址（实际项目中替换为灰度环境地址）
                        .uri("http://localhost:8081"))

                // 稳定版本路由 - 匹配稳定版本标记的请求
                .route("service_stable", r -> r
                        .path("/service/**")
                        .and().header(GrayReleaseFilter.GRAY_HEADER, GrayReleaseFilter.STABLE_VALUE)
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Route-Version", "stable")
                                .addResponseHeader("X-Version", "stable-v1.0"))
                        // 稳定服务地址
                        .uri("http://localhost:8082"))

                // 默认路由 - 兜底路由（当染色标记不匹配时）
                .route("service_default", r -> r
                        .path("/service/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .addRequestHeader("X-Route-Version", "default")
                                .addResponseHeader("X-Version", "default-v1.0"))
                        .uri("http://localhost:8082"))
                .build();
    }
}
