package org.github.guardjo.dientitian.scheduler.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

public class ExcelFileValidator implements ConstraintValidator<ExcelFile, MultipartFile> {
    private static final List<String> EXCEL_EXTENSIONS = List.of(".xlsx", ".xls");

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        // null 여부는 @NotNull 에서 검증한다.
        if (file == null) {
            return true;
        }

        String filename = file.getOriginalFilename();

        if (filename == null) {
            return false;
        }

        String lowerCaseFilename = filename.toLowerCase(Locale.ROOT);

        return EXCEL_EXTENSIONS.stream().anyMatch(lowerCaseFilename::endsWith);
    }
}
