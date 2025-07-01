package com.keakimleang.bulkpayment.utils.excelpopulator;

import java.io.*;
import org.apache.poi.ss.usermodel.*;

public abstract class ExcelPopulatorDecorator implements ExcelPopulator {

    protected final ExcelPopulator excelPopulator;

    public ExcelPopulatorDecorator(final ExcelPopulator excelPopulator) {
        this.excelPopulator = excelPopulator;
    }

    @Override
    public Sheet populate() {
        return excelPopulator.populate();
    }

    @Override
    public ByteArrayOutputStream export() {
        return excelPopulator.export();
    }
}
