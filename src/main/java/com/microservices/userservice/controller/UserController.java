package com.microservices.userservice.controller;

import com.microservices.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<String> getUser(
            @PathVariable String id,
            @RequestParam(required = false) String failure
    ) {
        return ResponseEntity.ok(userService.getUser(id, failure));
    }
}