package org.example.learn.spring.cloud.gateway.hello.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Random;

/**
 * 灰度发布请求染色过滤器
 * 支持多种染色策略：
 * 1. 基于请求头（Header）染色 - 特定标识的用户
 * 2. 基于请求参数（Query Parameter）染色 - 测试参数
 * 3. 基于IP地址染色 - 特定IP段
 * 4. 基于百分比染色 - 随机流量分配
 */
@Component
public class GrayReleaseFilter implements GlobalFilter, Ordered {

    // 灰度版本请求头标识
    public static final String GRAY_HEADER = "X-Gray-Version";
    public static final String GRAY_VALUE = "gray";
    public static final String STABLE_VALUE = "stable";

    // 灰度配置参数
    private static final String GRAY_PARAM = "gray";
    private static final String GRAY_IP_PREFIX = "192.168.";
    private static final int GRAY_PERCENTAGE = 30; // 30%流量进入灰度版本

    private final Random random = new Random();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 判断是否为灰度请求
        boolean isGray = checkGrayRelease(request);

        // 添加染色标记到请求头
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(GRAY_HEADER, isGray ? GRAY_VALUE : STABLE_VALUE)
                .header("X-Gray-Route", isGray ? "gray-service" : "stable-service")
                .build();

        // 将染色信息传递给后续过滤器和服务
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // 记录染色日志
        logGrayInfo(mutatedExchange, isGray);

        return chain.filter(mutatedExchange);
    }

    /**
     * 检查是否为灰度请求
     */
    private boolean checkGrayRelease(ServerHttpRequest request) {
        // 1. 优先检查请求头（最高优先级）
        String grayHeader = request.getHeaders().getFirst(GRAY_HEADER);
        if (GRAY_VALUE.equalsIgnoreCase(grayHeader)) {
            return true;
        }

        // 2. 检查请求参数
        String grayParam = request.getQueryParams().getFirst(GRAY_PARAM);
        if ("true".equalsIgnoreCase(grayParam) || "1".equals(grayParam)) {
            return true;
        }

        // 3. 检查特定用户标识（如测试账号）
        String userId = request.getHeaders().getFirst("X-User-Id");
        if (isGrayUser(userId)) {
            return true;
        }

        // 4. 检查IP地址
        String clientIp = getClientIp(request);
        if (isGrayIp(clientIp)) {
            return true;
        }

        // 5. 基于百分比随机分配
        return random.nextInt(100) < GRAY_PERCENTAGE;
    }

    /**
     * 判断是否为灰度用户
     */
    private boolean isGrayUser(String userId) {
        if (userId == null || userId.isEmpty()) {
            return false;
        }
        // 灰度用户列表（实际项目中可从配置中心或数据库获取）
        List<String> grayUsers = List.of("user001", "user002", "test001", "beta001");
        return grayUsers.contains(userId);
    }

    /**
     * 判断是否为灰度IP
     */
    private boolean isGrayIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        // 内网IP段作为灰度测试环境
        return ip.startsWith(GRAY_IP_PREFIX) || ip.startsWith("10.0.") || ip.equals("127.0.0.1");
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从X-Forwarded-For获取（经过代理的情况）
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 从X-Real-IP获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        // 直接连接IP
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "";
    }

    /**
     * 记录灰度路由日志
     */
    private void logGrayInfo(ServerWebExchange exchange, boolean isGray) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String clientIp = getClientIp(request);
        String userId = request.getHeaders().getFirst("X-User-Id");

        System.out.printf("[GrayRelease] Path=%s, IP=%s, User=%s, Gray=%s%n",
                path, clientIp, userId != null ? userId : "anonymous", isGray);
    }

    @Override
    public int getOrder() {
        // 确保在其他过滤器之前执行，优先级最高
        return Ordered.HIGHEST_PRECEDENCE;
    }
}