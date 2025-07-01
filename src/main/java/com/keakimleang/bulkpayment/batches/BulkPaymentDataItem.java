package com.keakimleang.bulkpayment.batches;

import com.keakimleang.bulkpayment.annotations.AtLeastOneField;
import com.keakimleang.bulkpayment.annotations.ValidMonetary;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AtLeastOneField(fields = {"beneficiaryAccount", "amount"}, message = "Provide at least one of the fields: beneficiaryAccount or amount")
public class BulkPaymentDataItem {
    private String beneficiaryAccount;
    @ValidMonetary(message = "Amount must be positive number")
    private String amount;
}
