package com.keakimleang.bulkpayment.utils.excelpopulator;

import java.io.*;
import org.apache.poi.ss.usermodel.*;

public interface ExcelPopulator {

    Sheet populate();

    ByteArrayOutputStream export();

}
