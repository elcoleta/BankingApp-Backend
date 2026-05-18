package com.example.bankingapp.util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class IbanGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        // 9-digit account number (100000000–999999999)
        int accountNum = 100_000_000 + RANDOM.nextInt(900_000_000);
        String digits = String.valueOf(accountNum);

        // BBAN: INHO0 + 9 digits  →  NLxxINHO0xxxxxxxxx
        String bban = "INHO0" + digits;

        // MOD-97 check-digit calculation: rearrange as BBAN + country + "00"
        String rearranged = bban + "NL00";
        StringBuilder numericStr = new StringBuilder();
        for (char c : rearranged.toCharArray()) {
            if (Character.isLetter(c)) {
                numericStr.append(c - 'A' + 10);
            } else {
                numericStr.append(c);
            }
        }

        int checkDigits = 98 - new BigInteger(numericStr.toString()).mod(BigInteger.valueOf(97)).intValue();
        return String.format("NL%02dINHO0%s", checkDigits, digits);
    }
}
