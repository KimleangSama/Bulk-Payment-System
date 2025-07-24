package com.keakimleang.bulkpayment.payloads;

import java.io.Serializable;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;

public record FileDownload(InputStreamResource file, HttpHeaders headers) implements Serializable {
}
