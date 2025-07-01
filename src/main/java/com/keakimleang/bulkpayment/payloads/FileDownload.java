package com.keakimleang.bulkpayment.payloads;

import java.io.*;
import org.springframework.core.io.*;
import org.springframework.http.*;

public record FileDownload(InputStreamResource file, HttpHeaders headers) implements Serializable {
}
