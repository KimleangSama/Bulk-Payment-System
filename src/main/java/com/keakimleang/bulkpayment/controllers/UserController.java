package com.keakimleang.bulkpayment.controllers;

import com.keakimleang.bulkpayment.config.handlers.ReactiveWebSocketHandler;
import com.keakimleang.bulkpayment.payloads.requests.AuthRequest;
import com.keakimleang.bulkpayment.repos.UserRepository;
import com.keakimleang.bulkpayment.securities.CustomReactiveUserDetailsService;
import com.keakimleang.bulkpayment.securities.CustomUserDetails;
import com.keakimleang.bulkpayment.securities.User;
import com.keakimleang.bulkpayment.utils.TokenUtil;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
    private final StringRedisTemplate redisTemplate;

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
    public Mono<String> login(ServerHttpRequest req, @RequestBody AuthRequest request) {
        String ip = req.getRemoteAddress() != null ? req.getRemoteAddress().getAddress().getHostAddress() : "unknown";
        String host = req.getHeaders().getHost() != null ? req.getHeaders().getHost().toString() : "unknown";
        String userAgent = req.getHeaders().getFirst("User-Agent");
        String message = String.format("User %s logged in from IP: %s, Host: %s, User-Agent: %s",
                request.getUsername(), ip, host, userAgent);
        return userDetailsService.findByUsername(request.getUsername())
                .flatMap(user -> authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()))
                        .flatMap(authentication -> {
                            if (authentication.isAuthenticated()) {
                                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                                String token = tokenUtil.generateAccessToken(userDetails);
                                log.info("User logged in successfully: {}", userDetails.getUsername());
                                handleNewLogin(message, user.getUsername(), token);
                                return Mono.just(token);
                            } else {
                                return Mono.error(new RuntimeException("Authentication failed"));
                            }
                        }))
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")));
    }

    // WebSocket to remove old session
    public void handleNewLogin(String message, String username, String newSessionId) {
        String key = "USER_SESSION:" + username;
        String oldSessionId = redisTemplate.opsForValue().get(key);

        log.info("Old session id: {}", oldSessionId);
        log.info("New session id: {}", newSessionId);

        if (oldSessionId != null && !oldSessionId.equalsIgnoreCase(newSessionId)) {
            // Notify old session via WebSocket
            log.info("Kicking out old session: {}", oldSessionId);
            ReactiveWebSocketHandler.sendLogoutMessage(message, oldSessionId);
        }

        // Save new session
        redisTemplate.opsForValue().set(key, newSessionId, Duration.ofMinutes(60));
    }
}
