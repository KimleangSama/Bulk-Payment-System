package com.keakimleang.bulkpayment.controllers;

import com.keakimleang.bulkpayment.payloads.requests.AuthRequest;
import com.keakimleang.bulkpayment.repos.UserRepository;
import com.keakimleang.bulkpayment.securities.CustomReactiveUserDetailsService;
import com.keakimleang.bulkpayment.securities.CustomUserDetails;
import com.keakimleang.bulkpayment.securities.User;
import com.keakimleang.bulkpayment.utils.TokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final CustomReactiveUserDetailsService userDetailsService;
    private final ReactiveAuthenticationManager authenticationManager;
    private final TokenUtil tokenUtil;

    @PostMapping("/register")
    public Mono<Void> register(@RequestBody AuthRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user)
                .doOnSuccess(savedUser -> log.info("User registered successfully: {}", savedUser.getUsername()))
                .then();
    }

    @PostMapping("/login")
    public Mono<String> login(@RequestBody AuthRequest request) {
        return userDetailsService.findByUsername(request.getUsername())
                .flatMap(user -> authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
                        .flatMap(authentication -> {
                            if (authentication.isAuthenticated()) {
                                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                                String token = tokenUtil.generateAccessToken(userDetails);
                                log.info("User logged in successfully: {}", userDetails.getUsername());
                                return Mono.just(token);
                            } else {
                                return Mono.error(new RuntimeException("Authentication failed"));
                            }
                        }))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }
}
