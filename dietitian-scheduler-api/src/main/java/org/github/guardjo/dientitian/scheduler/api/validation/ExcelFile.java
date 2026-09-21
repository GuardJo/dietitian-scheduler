package org.github.guardjo.dientitian.scheduler.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 업로드된 파일이 엑셀 파일(.xlsx, .xls)인지 검증한다.
 */
@Documented
@Constraint(validatedBy = ExcelFileValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelFile {
    String message() default "엑셀 파일(.xlsx, .xls)만 업로드할 수 있습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
