package com.main.datn_SD113.validator;

import com.main.datn_SD113.validator.impl.DateRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {
    String message() default "Ngày bắt đầu không được lớn hơn ngày kết thúc";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}