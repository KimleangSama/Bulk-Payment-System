package com.keakimleang.bulkpayment.batches;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.RowN;
import org.jooq.impl.DSL;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.lang.NonNull;

@Slf4j
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
        log.info("Bulk Payment write completed");

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
