package com.keakimleang.bulkpayment.utils.excelpopulator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.Resource;

public class ExcelPopulatorImpl implements ExcelPopulator {

    private final Resource resource;
    private Workbook workbook;

    public ExcelPopulatorImpl(final Resource resource) {
        this.resource = resource;
    }

    @Override
    public Sheet populate() {
        try {
            workbook = WorkbookFactory.create(resource.getInputStream());
            return workbook.getSheetAt(0);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ByteArrayOutputStream export() {
        try {
            final var output = new ByteArrayOutputStream();
            workbook.write(output);
            workbook.close();
            return output;
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
