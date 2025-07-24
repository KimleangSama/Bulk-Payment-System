package com.keakimleang.bulkpayment.utils;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.core.io.Resource;

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
