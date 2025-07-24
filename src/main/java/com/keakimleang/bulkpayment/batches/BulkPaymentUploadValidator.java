package com.keakimleang.bulkpayment.batches;

import static com.keakimleang.bulkpayment.batches.consts.BulkPaymentConstant.TEMPLATE_HEADER;
import com.keakimleang.bulkpayment.payloads.ApiError;
import com.keakimleang.bulkpayment.payloads.ApiValidationException;
import com.keakimleang.bulkpayment.payloads.BulkPaymentServiceException;
import com.keakimleang.bulkpayment.payloads.BulkPaymentUploadRequest;
import com.keakimleang.bulkpayment.utils.CsvHelper;
import com.keakimleang.bulkpayment.utils.ExcelHelper;
import com.keakimleang.bulkpayment.utils.MediaTypeConstant;
import com.keakimleang.bulkpayment.utils.StringWrapperUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BulkPaymentUploadValidator {
    private final Set<String> supportedFiles = Set.of(MediaTypeConstant.EXCEL, MediaTypeConstant.CSV);
    private final Validator validator;

    public BulkPaymentUploadValidator(final Validator validator) {
        this.validator = validator;
    }

    public Mono<BulkPaymentUploadRequest> validateFile(final BulkPaymentUploadRequest request) {
        return Mono.just(request)
                .flatMap(r -> {
                    if (!supportedFiles.contains(getMedialType(r.file()))) {
                        final var msg = "Wrong Format! The file should be in .csv or .xlsx format.";
                        return Mono.error(new ApiValidationException(ApiError.validateInput("file", msg)));
                    }

                    final var fileName = r.file().filename();
                    if (fileName.endsWith(".csv")) {
                        return validateCsvHeader(r);
                    } else { // excel
                        return validateExcelHeader(r);
                    }
                });
    }

    private Mono<BulkPaymentUploadRequest> validateCsvHeader(final BulkPaymentUploadRequest request) {
        return DataBufferUtils.join(request.file().content())
                .flatMap(dataBuffer -> {
                    final var bufferedReader = new BufferedReader(new InputStreamReader(dataBuffer.asInputStream()));
                    final var headers = CsvHelper.readRow(bufferedReader, 0);
                    return validate(request, headers);
                });
    }

    private Mono<BulkPaymentUploadRequest> validateExcelHeader(final BulkPaymentUploadRequest request) {
        return DataBufferUtils.join(request.file().content())
                .flatMap(dataBuffer -> {
                    try {
                        final var wb = WorkbookFactory.create(dataBuffer.asInputStream());
                        final var headers = ExcelHelper.readRow(wb.getSheetAt(0), 0);
                        return validate(request, headers);
                    } catch (final IOException e) {
                        return Mono.error(new BulkPaymentServiceException("Failed to read file"));
                    }
                });
    }

    private Mono<? extends BulkPaymentUploadRequest> validate(BulkPaymentUploadRequest request, List<String> headers) {
        final String msg = "Invalid file template for batcher upload";
        if (headers.size() != TEMPLATE_HEADER.size()) {
            return Mono.error(new ApiValidationException(ApiError.validateInput("file", msg)));
        }
        for (var idx = 0; idx < TEMPLATE_HEADER.size(); idx++) {
            if (!StringWrapperUtils.equalsIgnoreCase(TEMPLATE_HEADER.get(idx), headers.get(idx).strip())) {
                return Mono.error(new ApiValidationException(ApiError.validateInput("file", msg)));
            }
        }
        return Mono.just(request);
    }

    private String getMedialType(final FilePart filePart) {
        return URLConnection.guessContentTypeFromName(filePart.filename());
    }

    public String validateItem(final BulkPaymentDataItem bulkPaymentDataItem) {
        final var errors = validator.validate(bulkPaymentDataItem);
        return String.join("\n", errors.stream()
                .map(ConstraintViolation::getMessage)
                .toList());
    }
}
