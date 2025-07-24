package com.keakimleang.bulkpayment.utils.excelpopulator;

import static com.keakimleang.bulkpayment.utils.ExcelHelper.addCellValue;
import static com.keakimleang.bulkpayment.utils.ExcelHelper.getCellTypeBaseOnData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;

public class RowDataPopulator extends ExcelPopulatorDecorator {

    private final List<String> orderDataColumns;
    private final Iterable<Map<String, Object>> rows;
    private final int startRowDataAt;
    private final boolean copyFirstRowCellStyle;
    private final List<CellStyle> cellStyles;

    public RowDataPopulator(final ExcelPopulator excelPopulator,
                            final List<String> orderDataColumns,
                            final Iterable<Map<String, Object>> rows,
                            final int startRowDataAt) {
        this(excelPopulator, orderDataColumns, rows, startRowDataAt, false);
    }

    public RowDataPopulator(final ExcelPopulator excelPopulator,
                            final List<String> orderDataColumns,
                            final Iterable<Map<String, Object>> rows,
                            final int startRowDataAt,
                            final boolean copyFirstRowCellStyle) {
        super(excelPopulator);
        this.orderDataColumns = orderDataColumns;
        this.rows = rows;
        this.startRowDataAt = startRowDataAt;
        this.copyFirstRowCellStyle = copyFirstRowCellStyle;
        this.cellStyles = new ArrayList<>();
    }

    @Override
    public Sheet populate() {
        final var sheet = super.populate();
        var rowIdx = startRowDataAt;
        if (copyFirstRowCellStyle) {
            copyCellStyle(sheet);
        }
        for (final var record : rows) {
            final var row = sheet.createRow(rowIdx++);
            var cellIdx = -1;
            for (final var column : orderDataColumns) {
                cellIdx = cellIdx + 1;
                final var value = record.get(column);
                final var cell = row.createCell(cellIdx, getCellTypeBaseOnData(value));
                if (!cellStyles.isEmpty()) {
                    cell.setCellStyle(cellStyles.get(cellIdx));
                }
                addCellValue(cell, value);
            }
        }
        return sheet;
    }

    private void copyCellStyle(final Sheet sheet) {
        final var firstDataRow = sheet.getRow(startRowDataAt);
        if (Objects.isNull(firstDataRow)) {
            return;
        }
        for (var idx = 0; idx < orderDataColumns.size(); idx++) {
            final var cell = firstDataRow.getCell(idx);
            if (Objects.isNull(cell)) {
                cellStyles.add(null);
                continue;
            }
            cellStyles.add(cell.getCellStyle());
        }
    }
}
