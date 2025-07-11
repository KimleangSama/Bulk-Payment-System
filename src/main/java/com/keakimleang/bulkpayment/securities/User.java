package com.keakimleang.bulkpayment.securities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String username;
    private String password;

    // TODO: Add roles

    public User() {
    }

    public User(String username, String encode) {
    }
}
