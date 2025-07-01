package com.keakimleang.bulkpayment.utils;

import java.util.*;
import java.util.function.*;
import lombok.*;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.*;

@Getter
@Setter
public class GenerateExcelParam {
    private Resource templateResource;
    private Map<String, Object> headerData;
    private BiConsumer<Sheet, Map<String, Object>> headerPopulator;
    private List<String> orderedColumnName;
    private Iterable<Map<String, Object>> rows;
    private int startRowDataAt;
    private boolean copyFirstRowCellStyle;
}
