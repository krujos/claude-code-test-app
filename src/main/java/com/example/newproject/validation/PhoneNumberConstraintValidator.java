package com.example.newproject.validation;

import com.example.newproject.util.PhoneNumberValidator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberConstraintValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        boolean isValid = PhoneNumberValidator.isValidPhoneNumber(phoneNumber);

        if (!isValid) {
            // Customize the error message with the specific validation error
            String errorMessage = PhoneNumberValidator.getValidationErrorMessage(phoneNumber);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errorMessage)
                   .addConstraintViolation();
        }

        return isValid;
    }
}
