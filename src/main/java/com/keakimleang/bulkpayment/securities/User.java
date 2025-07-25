package com.keakimleang.bulkpayment.securities;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "users")
@NoArgsConstructor
public class User {
    @Id
    private Long id;
    private String username;
    private String password;
    private List<String> roles;
}
