package com.ubaidsample.controller;

import com.ubaidsample.dto.request.UserRequest;
import com.ubaidsample.dto.response.UserResponse;
import com.ubaidsample.entity.User;
import com.ubaidsample.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest req, UriComponentsBuilder uriBuilder) {
        User saved = service.register(req);
        URI location = uriBuilder.path("/api/users/{id}").buildAndExpand(saved.getId()).toUri();
        UserResponse resp = new UserResponse(saved.getId(), saved.getName(), saved.getEmail());
        return ResponseEntity.created(location).body(resp);
    }
}