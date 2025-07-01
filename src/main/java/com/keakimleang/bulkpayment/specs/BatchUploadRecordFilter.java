package com.keakimleang.bulkpayment.specs;

import com.keakimleang.bulkpayment.utils.*;
import java.util.*;
import lombok.experimental.*;
import org.springframework.data.relational.core.query.*;

@Accessors(chain = true)
public class BatchUploadRecordFilter {
    private final List<Criteria> filters = new ArrayList<>();

    public Criteria getCriteria() {
        return SpecificationCombiner.and(filters);
    }

    public void setBatchUploadId(final Long batchUploadId) {
        if (Objects.isNull(batchUploadId)) {
            return;
        }
        filters.add(buildEqualsSpec("batch_upload_id", batchUploadId));
    }

    public void setBatchOwnerName(final String batchOwnerName) {
        if (StringWrapperUtils.isBlank(batchOwnerName)) {
            return;
        }
        filters.add(buildEqualsSpec("batch_owner_name", batchOwnerName));
    }

    private Criteria buildEqualsSpec(final String columnName,
                                     final Object value) {
        return Criteria.where(columnName).is(value);
    }
}
