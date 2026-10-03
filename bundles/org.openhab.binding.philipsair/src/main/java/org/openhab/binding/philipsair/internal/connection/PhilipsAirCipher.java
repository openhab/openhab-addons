/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.philipsair.internal.connection;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.util.HexUtils;

/**
 * Performs message de- and encyrption
 *
 * @author Michał Boroński - Initial contribution
 * @author Marcel Verpaalen - Fix key exchange and add error handling
 */
@NonNullByDefault
public class PhilipsAirCipher {
    private static final String AA = "AA";

    private static final BigInteger G = new BigInteger(Base64.getDecoder().decode(
            "AKTRy9XD/TQSZ2WkQu+5mQX4EE3SWKxQf9ZAbP8UJm0xJm/qHlxBVkt3fmkPVQTyExYCF7SwG4hqXpFUf54nSfTX+9fTuaku4ZCdDSJj+Ap2pqJMCHoJH1MdvwoBabaiitZipNGOc6+jLXedWRjQi8iFj03O+XwqJIVebusis7Ll"));
    private static final BigInteger P = new BigInteger(Base64.getDecoder().decode(
            "ALELj5aggOAd3pLeXq5dVOxSyZ+8+wajxppqncpS0jthYHPihnWiPRiYOO8eLuZSwBPstK6pBhEjJJdcPNSbg7+sy919kMS9cJhIjpwhmnNyTv/W+uVkRzj6oxpP9VvMwKFRr18NyLS9Rb833zZcGmXmjP2nbU2nCN8fsrwuSkNx"));
    private static final SecureRandom RAND = new SecureRandom();
    private static final int SHARED_SECRET_LENGTH = 128;

    private @Nullable Cipher decipher;
    private @Nullable Cipher cipher;
    private final BigInteger a;
    private final BigInteger aPow;

    public PhilipsAirCipher() throws GeneralSecurityException {
        this(randomForBitsNonZero(256));
    }

    public PhilipsAirCipher(BigInteger randomSeed) throws GeneralSecurityException {
        a = randomSeed;
        aPow = G.modPow(a, P);
    }

    public void initKey(String key) throws GeneralSecurityException {
        this.cipher = null;
        this.decipher = null;
        SecretKeySpec keySpec = new SecretKeySpec(HexUtils.hexToBytes(key), "AES");

        Cipher newDecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        newDecipher.init(Cipher.DECRYPT_MODE, keySpec, new IvParameterSpec(new byte[16]));
        Cipher newCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        newCipher.init(Cipher.ENCRYPT_MODE, keySpec, new IvParameterSpec(new byte[16]));

        this.decipher = newDecipher;
        this.cipher = newCipher;
    }

    public String getApow() {
        return aPow.toString(16);
    }

    private static BigInteger randomForBitsNonZero(int numBits) {
        BigInteger candidate = new BigInteger(numBits, RAND);
        while (candidate.equals(BigInteger.ZERO)) {
            candidate = new BigInteger(numBits, RAND);
        }
        return candidate;
    }

    public String calculateKey(String hellman, String key) throws GeneralSecurityException {
        BigInteger b = new BigInteger(hellman, 16);
        BigInteger s = b.modPow(a, P);
        byte[] sByteArrayTrunc = Arrays.copyOfRange(toFixedLength(s), 0, 16);
        byte[] hexKey = HexUtils.hexToBytes(key);

        Cipher ciph = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ciph.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sByteArrayTrunc, "AES"), new IvParameterSpec(new byte[16]));

        byte[] keyDecoded = ciph.doFinal(hexKey);
        String aesKey = HexUtils.bytesToHex(keyDecoded).substring(0, 32);
        return aesKey;
    }

    /**
     * Encodes the shared secret as fixed length big-endian byte array, as the device does.
     * {@link BigInteger#toByteArray()}
     * returns the minimal two's-complement representation, which may have a leading sign byte or be shorter.
     */
    static byte[] toFixedLength(BigInteger value) {
        byte[] bytes = value.toByteArray();
        byte[] fixed = new byte[SHARED_SECRET_LENGTH];
        int length = Math.min(bytes.length, SHARED_SECRET_LENGTH);
        System.arraycopy(bytes, bytes.length - length, fixed, SHARED_SECRET_LENGTH - length, length);
        return fixed;
    }

    /**
     * @throws IllegalStateException if no key was set with {@link #initKey(String)}
     */
    public String decrypt(String encodedContent) throws IllegalBlockSizeException, BadPaddingException {
        Cipher decipher = this.decipher;
        if (decipher == null) {
            throw new IllegalStateException("Key not initialized");
        }

        byte[] decoded = decipher.doFinal(Base64.getDecoder().decode(encodedContent));
        byte[] unpaded = Arrays.copyOfRange(decoded, 2, decoded.length);
        return new String(unpaded, StandardCharsets.UTF_8);
    }

    /**
     * @throws IllegalStateException if no key was set with {@link #initKey(String)}
     */
    public String encrypt(String data) throws IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = this.cipher;
        if (cipher == null) {
            throw new IllegalStateException("Key not initialized");
        }

        String encodedData = AA + data;
        byte[] encryptedBytes = cipher.doFinal(encodedData.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
}
