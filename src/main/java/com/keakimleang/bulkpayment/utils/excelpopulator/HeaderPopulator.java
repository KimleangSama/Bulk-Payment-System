package com.keakimleang.bulkpayment.utils.excelpopulator;

import java.util.*;
import java.util.function.*;
import org.apache.poi.ss.usermodel.*;

public class HeaderPopulator extends ExcelPopulatorDecorator {

    private final Map<String, Object> headerData;
    private final BiConsumer<Sheet, Map<String, Object>> headerSheetPopulator;

    public HeaderPopulator(final ExcelPopulator excelPopulator,
                           final Map<String, Object> headerData,
                           final BiConsumer<Sheet, Map<String, Object>> headerSheetPopulator) {
        super(excelPopulator);
        this.headerData = headerData;
        this.headerSheetPopulator = Objects.requireNonNull(headerSheetPopulator);
    }

    @Override
    public Sheet populate() {
        final var sheet = super.populate();
        headerSheetPopulator.accept(sheet, headerData);
        return sheet;
    }
}
