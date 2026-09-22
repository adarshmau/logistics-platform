package com.logistics.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.LoadBalancerFilterFunctions.lb;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.web.servlet.function.RequestPredicates.path;

@Configuration
@Slf4j
public class GatewayConfig {

    @Value("${spring.application.name}")
    private String selfServiceName;

    @Bean
    public RouterFunction<ServerResponse> dynamicServiceRoutes(DiscoveryClient discoveryClient) {
        List<String> services = discoveryClient.getServices();
        log.info("Building gateway routes for registered services: {}", services);

        return services.stream()
                .filter(serviceId -> !serviceId.equalsIgnoreCase(selfServiceName))
                .map(this::buildRouteForService)
                .reduce(RouterFunction::and)
                .orElseGet(() -> RouterFunctions.route().build());
    }

    private RouterFunction<ServerResponse> buildRouteForService(String serviceId) {
        String pathPrefix = serviceId.toLowerCase();
        return route(pathPrefix)
                .route(path("/" + pathPrefix + "/**"), http())
                .before(stripPrefix(1))
                .filter(lb(serviceId))
                .build();
    }
}