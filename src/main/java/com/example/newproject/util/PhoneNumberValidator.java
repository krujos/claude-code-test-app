package com.example.newproject.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class PhoneNumberValidator {

    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        try {
            // Try to parse with multiple region codes
            // First try with US as default
            PhoneNumber number = phoneUtil.parse(phoneNumber, "US");
            if (phoneUtil.isValidNumber(number)) {
                return true;
            }

            // Try parsing without region (international format)
            if (phoneNumber.startsWith("+")) {
                number = phoneUtil.parse(phoneNumber, null);
                return phoneUtil.isValidNumber(number);
            }

            // Try with other common regions
            String[] commonRegions = {"CA", "GB", "DE", "FR", "IT", "ES", "MX", "JP", "AU"};
            for (String region : commonRegions) {
                try {
                    number = phoneUtil.parse(phoneNumber, region);
                    if (phoneUtil.isValidNumber(number)) {
                        return true;
                    }
                } catch (NumberParseException e) {
                    // Continue trying other regions
                }
            }

            return false;
        } catch (NumberParseException e) {
            return false;
        }
    }

    public static String getValidationErrorMessage(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "Phone number is required";
        }

        if (isValidPhoneNumber(phoneNumber)) {
            return null;
        }

        return "Invalid phone number format. Please enter a valid phone number with country code (e.g., +1 555-123-4567)";
    }
}
