package com.kayode.ratelimiter.traffic;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class Controller {

    @GetMapping("users/{userId}")
    public String allowRequest(@PathVariable("userId") Long id) {
        String message = String.format("Allowed Request with user id %s at timestamp UTC: %s", id, Instant.now().toString());
        System.out.println(message);
        return message;
    }
}
