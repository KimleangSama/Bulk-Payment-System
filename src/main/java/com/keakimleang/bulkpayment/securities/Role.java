package com.keakimleang.bulkpayment.securities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "roles")
public class Role {
    @Id
    private Long id;
    private String name;
}
