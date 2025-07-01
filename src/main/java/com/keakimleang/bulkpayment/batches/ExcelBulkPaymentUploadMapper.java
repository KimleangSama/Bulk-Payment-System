package com.keakimleang.bulkpayment.batches;

import com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.extensions.excel.RowMapper;
import org.springframework.batch.extensions.excel.support.rowset.RowSet;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class ExcelBulkPaymentUploadMapper implements RowMapper<BulkPaymentDataItem> {
    // We want to robust handling if header contains leading space or trailing space
    private Map<String, String> headerMaps;

    @Override
    public BulkPaymentDataItem mapRow(final RowSet rowSet) {
        if (Objects.isNull(headerMaps)) {
            headerMaps = rowSet.getProperties().stringPropertyNames()
                    .stream()
                    .collect(Collectors.toMap(
                            // We already validate header of file, and it is better convert it to lower case for
                            // consistent without worry about Amount or amount or AMount
                            km -> km.strip().toLowerCase(),
                            kv -> kv
                    ));
        }
        final var props = rowSet.getProperties();
        final var item = new BulkPaymentDataItem();
        item.setBeneficiaryAccount(getStringValue(BulkPaymentConstant.BENEFICIARY_ACCOUNT, props));
        item.setAmount(getStringValue(BulkPaymentConstant.AMOUNT, props));
        return item;
    }

    private String getStringValue(final String headerName,
                                  final Properties props) {
        final var rawHeader = headerMaps.get(headerName);
        return Objects.nonNull(rawHeader) ? props.getProperty(rawHeader) : null;
    }
}
