package com.keakimleang.bulkpayment.quartz;

import com.keakimleang.bulkpayment.entities.BulkPaymentDataProd;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkPaymentDataProdMessage {
    private Long bulkPaymentInfoId;
    private List<BulkPaymentDataProd> records;
    private Instant timestamp;
}
