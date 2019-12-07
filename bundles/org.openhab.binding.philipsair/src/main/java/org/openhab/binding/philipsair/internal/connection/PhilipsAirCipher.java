/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs message de- and encyrption
 *
 * @author Michał Boroński - Initial contribution
 *
 */
@NonNullByDefault
public class PhilipsAirCipher {
    private final Logger logger = LoggerFactory.getLogger(PhilipsAirCipher.class);

    private static final BigInteger G = new BigInteger(
            "A4D1CBD5C3FD34126765A442EFB99905F8104DD258AC507FD6406CFF14266D31266FEA1E5C41564B777E690F5504F213160217B4B01B886A5E91547F9E2749F4D7FBD7D3B9A92EE1909D0D2263F80A76A6A24C087A091F531DBF0A0169B6A28AD662A4D18E73AFA32D779D5918D08BC8858F4DCEF97C2A24855E6EEB22B3B2E5",
            16);
    private static final BigInteger P = new BigInteger(
            "B10B8F96A080E01DDE92DE5EAE5D54EC52C99FBCFB06A3C69A6A9DCA52D23B616073E28675A23D189838EF1E2EE652C013ECB4AEA906112324975C3CD49B83BFACCBDD7D90C4BD7098488E9C219A73724EFFD6FAE5644738FAA31A4FF55BCCC0A151AF5F0DC8B4BD45BF37DF365C1A65E68CFDA76D4DA708DF1FB2BC2E4A4371",
            16);
    private static final Random RAND = new Random();

    @Nullable    
    private Cipher decipher;
    @Nullable
    private Cipher cipher;
    private final BigInteger a;
    private final BigInteger aPow;

    public PhilipsAirCipher() throws GeneralSecurityException {
        // prepare numbers for key
        a = randomForBitsNonZero(256, RAND);
        aPow = G.modPow(a, P);
    }

    public void initKey(String key) throws GeneralSecurityException {
        try {
            decipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            decipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(HexUtils.hexToBytes(key), "AES"),
                    new IvParameterSpec(new byte[16]));

            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(HexUtils.hexToBytes(key), "AES"),
                    new IvParameterSpec(new byte[16]));
        } catch (GeneralSecurityException e) {
            logger.error("An exception occured", e);
            cipher = null;
            decipher = null;
            throw e;
        }
    }

    public String getApow() {
        return aPow.toString(16);
    }

    private static BigInteger randomForBitsNonZero(int numBits, Random r) {
        BigInteger candidate = new BigInteger(numBits, r);
        while (candidate.equals(BigInteger.ZERO)) {
            candidate = new BigInteger(numBits, r);
        }
        return candidate;
    }

    public String calculateKey(String hellman, String key) throws GeneralSecurityException, InterruptedException,
            TimeoutException, ExecutionException, InvalidAlgorithmParameterException {
        BigInteger b = new BigInteger(hellman, 16);
        BigInteger s = b.modPow(a, P);
        byte[] sByteArray = s.toByteArray();
        // remove trailing 0
        if (sByteArray.length > 128 && sByteArray[0] == 0) {
            sByteArray = Arrays.copyOfRange(sByteArray, 1, 128);
        }

        byte[] sByteArrayTrunc = Arrays.copyOfRange(sByteArray, 0, 16);
        byte[] hexKey = HexUtils.hexToBytes(key);

        Cipher ciph = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ciph.init(Cipher.DECRYPT_MODE, new SecretKeySpec(sByteArrayTrunc, "AES"), new IvParameterSpec(new byte[16]));

        byte[] keyDecoded = ciph.doFinal(hexKey);
        String aesKey = HexUtils.bytesToHex(keyDecoded).substring(0, 32);
        return aesKey;
    }

    public String decrypt(String encodedContent) throws IllegalBlockSizeException, BadPaddingException {
        byte[] decoded = decipher.doFinal(Base64.getDecoder().decode(encodedContent));
        byte[] unpaded = Arrays.copyOfRange(decoded, 2, decoded.length);
        return new String(unpaded);
    }

    public String encrypt(String data)
            throws IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException,
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        String encodedData = "AA" + data;
        byte[] encryptedBytes = cipher.doFinal(encodedData.getBytes("ascii"));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

}