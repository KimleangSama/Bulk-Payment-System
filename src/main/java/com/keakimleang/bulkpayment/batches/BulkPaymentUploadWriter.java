package com.keakimleang.bulkpayment.batches;

import java.util.*;
import org.jooq.*;
import org.jooq.impl.*;
import org.springframework.batch.item.*;
import org.springframework.lang.*;

public class BulkPaymentUploadWriter implements ItemWriter<Map<String, Object>> {
    private final DSLContext create;
    private final String tableName;

    private List<Field<Object>> fields;

    public BulkPaymentUploadWriter(final DSLContext create,
                                   final String tableName) {
        this.create = create;
        this.tableName = tableName;
    }

    @Override
    public void write(@NonNull final Chunk<? extends Map<String, Object>> chunk) {
        if (Objects.isNull(fields)) {
            setFields(chunk.getItems().getFirst());
        }
        final var table = DSL.table(tableName);
        final var rows = chunk.getItems()
                .stream()
                .map(row -> createRow(fields, row))
                .toList();
        create.insertInto(table)
                .columns(fields)
                .valuesOfRows(rows)
                .execute();
    }

    private void setFields(final Map<String, Object> item) {
        fields = item.keySet()
                .stream()
                .map(DSL::field)
                .toList();
    }

    private RowN createRow(final List<Field<Object>> fields,
                           final Map<String, Object> item) {
        final var values = fields.stream()
                .map(f -> item.get(f.getName()))
                .toList();
        return DSL.row(values);
    }
}
