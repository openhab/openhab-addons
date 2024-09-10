/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.hdpowerview.internal.shade;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Encoder/decoder for data sent to an HD PowerView Generation 3 Shade.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeDataWriter {

    // real position values 0% to 100% scale to internal values 0 to 10000
    private static final double SCALE = 100;

    // byte array for a blank 'no-op' write command
    private static final byte[] BLANK_WRITE_COMMAND_FRAME = HexFormat.ofDelimiter(":")
            .parseHex("f7:01:00:09:00:80:00:80:00:80:00:80:00");

    // index to data field positions in the outgoing bytes
    private static final int INDEX_SEQUENCE = 2;
    private static final int INDEX_PRIMARY = 4;
    private static final int INDEX_SECONDARY = 6;
    private static final int INDEX_TILT = 10;

    private final byte[] bytes;

    public ShadeDataWriter() {
        bytes = BLANK_WRITE_COMMAND_FRAME.clone();
    }

    public ShadeDataWriter(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    public byte[] getBytes() {
        return bytes;
    }

    /**
     * Decrypt the bytes using the given hexadecimal key. No-Op if key is blank or null.
     *
     * @param keyHex decryption key
     * @return decrypted bytes
     * @throws IllegalArgumentException (the key hex value could not be parsed)
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] getDecrypted(@Nullable String keyHex)
            throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (keyHex != null && !keyHex.isBlank()) {
            byte[] keyBytes = HexFormat.of().parseHex(keyHex);
            SecretKey keySecret = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySecret, new IvParameterSpec(new byte[16]));
            return cipher.doFinal(bytes);
        }
        return bytes;
    }

    /**
     * Encrypt the bytes using the given hexadecimal key. No-Op if key is blank or null.
     *
     * @param keyHex decryption key
     * @return encrypted bytes
     * @throws IllegalArgumentException (the key hex value could not be parsed)
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public byte[] getEncrypted(@Nullable String keyHex)
            throws IllegalArgumentException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        if (keyHex != null && !keyHex.isBlank()) {
            byte[] keyBytes = HexFormat.of().parseHex(keyHex);
            SecretKey keySecret = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySecret, new IvParameterSpec(new byte[16]));
            return cipher.doFinal(bytes);
        }
        return bytes;
    }

    /**
     * Encode the bytes in little endian format.
     */
    public byte[] encodeLE(double percent) throws IllegalArgumentException {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(String.format("Number '%0.1f' out of range (0% to 100%)", percent));
        }
        int position = ((int) Math.round(percent * SCALE));
        return new byte[] { (byte) (position & 0xff), (byte) ((position & 0xff00) >> 8) };
    }

    public ShadeDataWriter withPrimary(double percent) {
        byte[] bytes = encodeLE(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_PRIMARY, bytes.length);
        return this;
    }

    public ShadeDataWriter withSecondary(double percent) {
        byte[] bytes = encodeLE(percent);
        System.arraycopy(bytes, 0, this.bytes, INDEX_SECONDARY, bytes.length);
        return this;
    }

    public ShadeDataWriter withSequence(byte sequence) {
        this.bytes[INDEX_SEQUENCE] = sequence;
        return this;
    }

    public ShadeDataWriter withTilt(double percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException(String.format("Number '%0.1f' out of range (0% to 100%)", percent));
        }
        byte[] bytes = new byte[] { (byte) (percent), 0 };
        System.arraycopy(bytes, 0, this.bytes, INDEX_TILT, bytes.length);
        return this;
    }
}
