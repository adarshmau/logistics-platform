package com.logistics.order.client;

import com.logistics.order.dto.UserResponse;
import com.logistics.order.exception.ServiceUnavailableException;
import com.logistics.order.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {

    private final RestTemplate restTemplate;

//    @Value("${user-service.base-url}")
//    private String userServiceBaseUrl;


    private static final String USER_SERVICE_URL = "http://USER-SERVICE/api/v1/users/";
    public UserResponse getUserById(Long userId) {

       String url = USER_SERVICE_URL+userId;

        try {
            log.info("Calling user-service: GET {}", url);
            return restTemplate.getForObject(url, UserResponse.class);//Send GET request to this URL and convert the response into UserResponse

        } catch (HttpClientErrorException.NotFound ex) {
            log.warn("user-service reported no user with id: {}", userId);
            throw new UserNotFoundException("User not found with id: " + userId);

        } catch (ResourceAccessException ex) {
            log.error("user-service is unreachable: {}", ex.getMessage());
            throw new ServiceUnavailableException("user-service is currently unavailable. " +
                    "Please try again later.");
        }
    }
}