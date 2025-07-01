package com.keakimleang.bulkpayment.utils;

import com.keakimleang.bulkpayment.utils.excelpopulator.*;
import java.io.*;
import static java.util.Objects.*;


public final class ExportExcelHelper {

    private ExportExcelHelper() {
    }

    public static ByteArrayOutputStream generateExcelWithTemplate(final GenerateExcelParam param) {
        final var excelPopulator = new ExcelPopulatorImpl(param.getTemplateResource());
        final var dataPopulator = new RowDataPopulator(
                excelPopulator,
                param.getOrderedColumnName(),
                param.getRows(),
                param.getStartRowDataAt(),
                param.isCopyFirstRowCellStyle());
        dataPopulator.populate();
        return dataPopulator.export();
    }

    public static ByteArrayOutputStream generateExcelWithTemplateHeader(final GenerateExcelParam param) {
        final var excelPopulator = new ExcelPopulatorImpl(requireNonNull(param.getTemplateResource()));
        final var headerDecorator = new HeaderPopulator(excelPopulator, requireNonNull(param.getHeaderData()), requireNonNull(param.getHeaderPopulator()));
        final var dataDecorator = new RowDataPopulator(
                headerDecorator,
                requireNonNull(param.getOrderedColumnName()),
                requireNonNull(param.getRows()),
                param.getStartRowDataAt(),
                param.isCopyFirstRowCellStyle());
        dataDecorator.populate();
        return dataDecorator.export();
    }
}
