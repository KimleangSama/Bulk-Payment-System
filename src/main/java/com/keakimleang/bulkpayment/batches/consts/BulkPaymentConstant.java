package com.keakimleang.bulkpayment.batches.consts;

import java.util.List;

public class BulkPaymentConstant {
    public static final String[] BATCH_UPLOAD_FIELD_NAMES = {
            "beneficiaryAccount",
            "amount",
    };

    public static final List<String> TEMPLATE_HEADER = List.of(
            "beneficiary account",
            "amount"
    );

    public static final String BULK_PAYMENT_INFO = "bulk_payment_info";
    public static final String BULK_PAYMENT_DATA_STAGING = "bulk_payment_data_staging";
    public static final String BULK_PAYMENT_DATA_PROD = "bulk_payment_data_prod";

    public static final String UPLOAD_FILE = "uploadFile";
    public static final String UPLOADED_BULK_PAYMENT_ID = "uploadedBulkPaymentInfoId";
    public static final String UPLOADED_BULK_PAYMENT_SOURCE_ACCOUNT = "uploadedBulkPaymentInfoSourceAccount";
    public static final String UPLOADED_BULK_PAYMENT_CURRENCY = "uploadedBulkPaymentInfoCurrency";
    public static final String RUN_ASYNC_FLOW = "runAsyncFlow";


    public static final String BENEFICIARY_ACCOUNT = "beneficiary account";
    public static final String AMOUNT = "amount";
}
