package com.example.newproject.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PhoneNumberValidatorTest {

    @Test
    void testValidUSPhoneNumbers() {
        // Various US phone number formats (using real area codes, not 555 which is reserved)
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("(212) 456-7890"));
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("415-789-0123"));
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("6505551234"));
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("+1 212 456 7890"));
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("+14157890123"));
    }

    @Test
    void testValidInternationalPhoneNumbers() {
        // UK
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("+44 20 7946 0958"));

        // Germany
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("+49 30 123456"));

        // France
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("+33 1 42 86 82 00"));

        // Canada (same as US)
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("+1 416 789 0123"));

        // Australia
        assertTrue(PhoneNumberValidator.isValidPhoneNumber("+61 2 1234 5678"));
    }

    @Test
    void testInvalidPhoneNumbers() {
        // Completely invalid
        assertFalse(PhoneNumberValidator.isValidPhoneNumber("invalid"));
        assertFalse(PhoneNumberValidator.isValidPhoneNumber("abc123"));
        assertFalse(PhoneNumberValidator.isValidPhoneNumber("123"));

        // Too short
        assertFalse(PhoneNumberValidator.isValidPhoneNumber("123-4567"));

        // Invalid format
        assertFalse(PhoneNumberValidator.isValidPhoneNumber("000-000-0000"));
    }

    @Test
    void testNullAndEmptyPhoneNumbers() {
        assertFalse(PhoneNumberValidator.isValidPhoneNumber(null));
        assertFalse(PhoneNumberValidator.isValidPhoneNumber(""));
        assertFalse(PhoneNumberValidator.isValidPhoneNumber("   "));
    }

    @Test
    void testGetValidationErrorMessageForInvalidNumbers() {
        String errorMessage = PhoneNumberValidator.getValidationErrorMessage("invalid");
        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("Invalid phone number format"));
    }

    @Test
    void testGetValidationErrorMessageForNull() {
        String errorMessage = PhoneNumberValidator.getValidationErrorMessage(null);
        assertNotNull(errorMessage);
        assertEquals("Phone number is required", errorMessage);
    }

    @Test
    void testGetValidationErrorMessageForEmpty() {
        String errorMessage = PhoneNumberValidator.getValidationErrorMessage("");
        assertNotNull(errorMessage);
        assertEquals("Phone number is required", errorMessage);
    }

    @Test
    void testGetValidationErrorMessageForValidNumber() {
        String errorMessage = PhoneNumberValidator.getValidationErrorMessage("(212) 456-7890");
        assertNull(errorMessage);
    }
}
