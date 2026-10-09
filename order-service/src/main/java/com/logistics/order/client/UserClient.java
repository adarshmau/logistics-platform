package com.logistics.order.client;

import com.logistics.order.dto.UserResponse;
import com.logistics.order.exception.ServiceUnavailableException;
import com.logistics.order.exception.UserNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://USER-SERVICE/api/v1/users/";

    // Method 1: the REAL call. This is what runs normally when the circuit is CLOSED.
    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponse getUserById(Long userId) {
        String url = USER_SERVICE_URL + userId;

        try {
            log.info("Calling user-service: GET {}", url);
            return restTemplate.getForObject(url, UserResponse.class);

        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("user-service reported no user with id: {}", userId);
            throw new UserNotFoundException("User not found with id: " + userId);

        } catch (ResourceAccessException ex) {
            log.error("user-service is unreachable: {}", ex.getMessage());
            throw new ServiceUnavailableException("user-service is currently unavailable. Please try again later.");
        }
    }

    // Method 2: the FALLBACK. No @CircuitBreaker annotation here.
    // No network call here. This only runs when the circuit is OPEN,
    // or when getUserById throws something the circuit breaker treats as a failure.
    private UserResponse getUserByIdFallback(Long userId, Throwable throwable) {
        log.error(
                "Fallback triggered | userId={} | exceptionType={} | message={}",
                userId,
                throwable.getClass().getSimpleName(),
                throwable.getMessage()
        );
        throw new ServiceUnavailableException("user-service is currently unavailable. Please try again later.");
    }
}