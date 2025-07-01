package com.keakimleang.bulkpayment.utils;

import com.keakimleang.bulkpayment.payloads.*;
import java.io.*;
import java.math.*;
import java.nio.file.*;
import java.text.*;
import java.time.*;
import java.util.*;
import org.apache.commons.lang3.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DateUtil;

public final class ExcelHelper {

    private ExcelHelper() {
    }

    public static void addCellValue(final Cell cell,
                                    final Object value) {
        if (value instanceof String valueStr) {
            cell.setCellValue(valueStr);
        } else if (value instanceof LocalDate valueDate) {
            cell.setCellValue(valueDate);
        } else if (value instanceof LocalDateTime valueDt) {
            cell.setCellValue(valueDt);
        } else if (value instanceof Number valueNm) {
            cell.setCellValue(valueNm.doubleValue());
        } else if (value instanceof Boolean valueBoolean) {
            cell.setCellValue(valueBoolean);
        } else if (value instanceof RichTextString valueRich) {
            cell.setCellValue(valueRich);
        }
    }

    public static CellType getCellTypeBaseOnData(final Object value) {
        if (Objects.isNull(value)) {
            return CellType.BLANK;
        }
        switch (value) {
            case Double v -> {
                return CellType.NUMERIC;
            }
            case Boolean b -> {
                return CellType.BOOLEAN;
            }
            default -> {
                return CellType.STRING;
            }
        }
    }

    public static List<String> readRow(final Path excelFile,
                                       final int rowIndex) {
        try (final var wb = WorkbookFactory.create(excelFile.toFile())) {
            final var activeIndex = wb.getActiveSheetIndex();
            final var sheet = wb.getSheetAt(activeIndex);
            return readRow(sheet, rowIndex);
        } catch (final IOException e) {
            throw new BulkPaymentServiceException("Failed to read excel file.");
        }
    }

    public static List<String> readRow(final Sheet sheet,
                                       final int rowIndex) {
        final var row = sheet.getRow(rowIndex);
        if (Objects.isNull(row)) {
            return List.of();
        }
        final var result = new ArrayList<String>();
        for (var idx = 0; idx < row.getLastCellNum(); idx++) {
            result.add(convertExcelCell(row.getCell(idx)));
        }
        return result;
    }

    public static String convertExcelCell(final Cell cell) {
        if (Objects.isNull(cell)) {
            return null;
        }
        if (CellType.NUMERIC == cell.getCellType()) {
            if (isDateType(cell)) {
                /*
                 * Date System in Excel:
                 * - Excel represents dates as numbers, where 1 corresponds to January 1, 1900.
                 * - Each subsequent day increments this value by 1. For example:
                 *  + 2 = January 2, 1900.
                 *  + 43831 = January 1, 2020.
                 */
                if (cell.getNumericCellValue() == 0.0) {
                    // Invalid date input such as 0/1/1900
                    return null;
                }
                final var date = cell.getDateCellValue();
                final var dft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                return dft.format(date);
            }
            return BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
        } else if (CellType.BOOLEAN == cell.getCellType()) {
            return String.valueOf(cell.getBooleanCellValue());
        } else if (EnumSet.of(CellType._NONE, CellType.BLANK, CellType.ERROR).contains(cell.getCellType())) {
            return null;
        } else if (CellType.FORMULA == cell.getCellType()) {
            return evaluateFormulaValue(cell);
        }
        else {
            final var value = cell.getStringCellValue();
            if (StringUtils.isEmpty(value)) {
                return null;
            }
            return value;
        }
    }

    private static String evaluateFormulaValue(final Cell cell) {
        try {
            final var wb = cell.getSheet().getWorkbook();
            final var evaluator = wb.getCreationHelper().createFormulaEvaluator();
            final var cellValue = evaluator.evaluate(cell);
            return switch (cellValue.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue()).toPlainString();
                case BOOLEAN -> String.valueOf(cellValue.getBooleanValue());
                case STRING -> cellValue.getStringValue();
                default -> null;
            };
        } catch (final IllegalStateException ignore) {
            // error formula case
            return null;
        }
    }

    private static boolean isDateType(final Cell cell) {
        if (CellType.NUMERIC != cell.getCellType()) {
            return false;
        }
        final var style = cell.getCellStyle();
        if (Objects.isNull(style)) {
            return false;
        }
        final var formatIdx = style.getDataFormat();
        final var fmt = style.getDataFormatString();
        return DateUtil.isADateFormat(formatIdx, fmt);
    }
}
