package com.keakimleang.bulkpayment.payloads;

import com.keakimleang.bulkpayment.payloads.requests.BulkPaymentUploadInfoRequest;
import org.springframework.http.codec.multipart.*;

public record BulkPaymentUploadRequest(FilePart file,
                                       BulkPaymentUploadInfoRequest request,
                                       boolean runJobAsync) {
}
