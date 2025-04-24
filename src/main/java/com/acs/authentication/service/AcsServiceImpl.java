package com.acs.authentication.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.acs.authentication.entity.User;

import jakarta.annotation.Nullable;
import reactor.core.publisher.Mono;

@Service
public class AcsServiceImpl implements AcsService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private UserService userService;

    public Mono<String> callAcsApi(HttpMethod method, String command, Map<String, String> queryParams,
            @Nullable Object body) {
        // 1. Manually build the query string (to prevent password encoding issues)
        StringBuilder urlBuilder = new StringBuilder("/?command=" + command + "&response=json");
        queryParams.forEach((key, value) -> urlBuilder.append("&").append(key).append("=").append(value));
        String finalUri = urlBuilder.toString();

        System.out.println("Calling ACS with URI: " + finalUri);

        WebClient.RequestHeadersSpec<?> request;
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            request = body != null ? webClient.method(method).uri(finalUri).bodyValue(body)
                    : webClient.method(method).uri(finalUri);
        } else {
            request = webClient.method(method).uri(finalUri);
        }

        return request.retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new RuntimeException("ACS Error: " + error))))
                .bodyToMono(String.class).flatMap(responseBody -> {
                    // ACS login successful, now process user

                    String username = queryParams.get("username");
                    String password = queryParams.get("password");

                    User existingUser = userService.findByUserNameAndPassword(username, password);
                    if (existingUser == null) {
                        User user = new User();
                        user.setUserName(username);
                        user.setPassword(password);
                        user.setIsActive(true);
                        user.setEmail(username);
                        userService.save(user);
                        System.out.println("New user saved: " + username);
                    }

                    return Mono.just(responseBody);
                });
    }

}
