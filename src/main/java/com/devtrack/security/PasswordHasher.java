package com.devtrack.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordHasher {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String newSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    public static String hash(String password, String encodedSalt) throws GeneralSecurityException {
        byte[] salt = Base64.getDecoder().decode(encodedSalt);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            byte[] result = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            return Base64.getEncoder().encodeToString(result);
        } finally {
            spec.clearPassword();
        }
    }

    public static boolean matches(String password, String expectedHash, String salt)
            throws GeneralSecurityException {
        byte[] expected = Base64.getDecoder().decode(expectedHash);
        byte[] actual = Base64.getDecoder().decode(hash(password, salt));
        return MessageDigest.isEqual(expected, actual);
    }
}
