package com.keakimleang.bulkpayment.utils.excelpopulator;

import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Sheet;

public interface ExcelPopulator {

    Sheet populate();

    ByteArrayOutputStream export();

}
