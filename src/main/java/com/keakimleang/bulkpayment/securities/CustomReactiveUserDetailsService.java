package com.keakimleang.bulkpayment.securities;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomReactiveUserDetailsService implements ReactiveUserDetailsService {
    private final DatabaseClient databaseClient;

    private static final String QUERY = """
                SELECT u.id, u.username, u.password, r.name as role
                FROM users u
                JOIN users_roles ur ON u.id = ur.user_id
                JOIN roles r ON ur.role_id = r.id
                WHERE u.username = :username
            """;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return databaseClient.sql(QUERY)
                .bind("username", username)
                .map((row, meta) -> Tuples.of(
                        Objects.requireNonNull(row.get("id", Long.class)),
                        Objects.requireNonNull(row.get("username", String.class)),
                        Objects.requireNonNull(row.get("password", String.class)),
                        Objects.requireNonNull(row.get("role", String.class))
                ))
                .all()
                .collectList()
                .filter(rows -> !rows.isEmpty())
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found: " + username)))
                .map(rows -> {
                    var first = rows.getFirst();
                    User user = new User();
                    user.setId(first.getT1());
                    user.setUsername(first.getT2());
                    user.setPassword(first.getT3());
                    user.setRoles(
                            rows.stream()
                                    .map(Tuple4::getT4)
                                    .distinct()
                                    .toList()
                    );
                    return new CustomUserDetails(user);
                });
    }
}
