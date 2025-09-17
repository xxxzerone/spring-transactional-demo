package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.demo.controller.request.RequestJoin;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Slf4j
@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<?> join(@RequestBody RequestJoin requestJoin) {
        log.info("UserController.join requestJoin: {}", requestJoin);
        userService.join(requestJoin);
        return ResponseEntity.ok().build();
    }
    
}
