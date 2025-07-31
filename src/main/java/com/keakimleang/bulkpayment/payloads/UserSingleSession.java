package com.keakimleang.bulkpayment.payloads;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserSingleSession {
    private String username;
    private String message;
    private String newSessionId;
}
