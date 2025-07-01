package com.keakimleang.bulkpayment.payloads;

public enum ProcessingStatus {
    FAILED,
    AWAIT_CONFIRM,
    PROCESSING,
    COMPLETED,
    SCHEDULED,
    PARTIAL_FAIL,
}
