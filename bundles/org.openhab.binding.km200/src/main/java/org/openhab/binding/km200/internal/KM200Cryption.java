/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.km200.internal;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KM200Cryption is managing the en- and decription of the communication to the device
 *
 * @author Markus Eckhardt - Initial contribution
 */
@NonNullByDefault
public class KM200Cryption {

    private final Logger logger = LoggerFactory.getLogger(KM200Cryption.class);

    private final KM200Device remoteDevice;

    public KM200Cryption(KM200Device remoteDevice) {
        this.remoteDevice = remoteDevice;
    }

    /**
     * This function removes zero padding from a byte array.
     *
     */
    private byte[] removeZeroPadding(byte[] bytes) {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0) {
            --i;
        }
        return Arrays.copyOf(bytes, i + 1);
    }

    /**
     * This function adds zero padding to a byte array.
     *
     */
    private byte[] addZeroPadding(byte[] bdata, int bSize, String cSet) throws UnsupportedEncodingException {
        int encryptPadchar = bSize - (bdata.length % bSize);
        byte[] padchars = new String(new char[encryptPadchar]).getBytes(cSet);
        byte[] paddedData = new byte[bdata.length + padchars.length];
        System.arraycopy(bdata, 0, paddedData, 0, bdata.length);
        System.arraycopy(padchars, 0, paddedData, bdata.length, padchars.length);
        return paddedData;
    }

    /**
     * This function does the decoding for a new message from the device
     *
     */
    public @Nullable String decodeMessage(byte[] encoded) {
        String retString = null;
        byte[] decodedB64 = null;

        // MimeDecoder was the only working decoder.
        decodedB64 = Base64.getMimeDecoder().decode(encoded);

        try {
            /* Check whether the length of the decryptData is NOT multiplies of 16 */
            if ((decodedB64.length & 0xF) != 0) {
                logger.debug("Length of message is {}.", decodedB64.length);
                /* Return the data */
                retString = new String(decodedB64, remoteDevice.getCharSet());
                logger.debug("Did NOT decrypt message, returning {}.", retString);
                return retString;
            }
            // --- create cipher
            final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(remoteDevice.getCryptKeyPriv(), "AES"));
            byte[] decryptedData = cipher.doFinal(decodedB64);
            byte[] decryptedDataWOZP = removeZeroPadding(decryptedData);
            return (new String(decryptedDataWOZP, remoteDevice.getCharSet()));
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            logger.warn("Exception on encoding ({})", e.getMessage());
            return null;
        }
    }

    /**
     * This function does the encoding for a new message to the device
     *
     */
    public byte @Nullable [] encodeMessage(String data) {
        try {
            // --- create cipher
            byte[] bdata = data.getBytes(remoteDevice.getCharSet());
            final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(remoteDevice.getCryptKeyPriv(), "AES"));
            int bsize = cipher.getBlockSize();
            /* Add Padding, encrypt AES and B64 */
            byte[] encryptedData = cipher.doFinal(addZeroPadding(bdata, bsize, remoteDevice.getCharSet()));
            try {
                return (Base64.getMimeEncoder().encode(encryptedData));
            } catch (IllegalArgumentException e) {
                logger.debug("Base64encoding not possible: {}", e.getMessage());
            }
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            logger.warn("Exception on encoding ({})", e.getMessage());
        }
        return null;
    }

    /**
     * This function creates the private key from the MD5Salt, the device and the private password
     *
     * @author Markus Eckhardt
     */
    public void recreateKeys() {
        if (!remoteDevice.getGatewayPassword().isBlank() && !remoteDevice.getPrivatePassword().isBlank()
                && remoteDevice.getMD5Salt().length > 0) {
            byte[] md5K1 = null;
            byte[] md5K2Init = null;
            byte[] md5K2Private = null;
            byte[] bytesOfGatewayPassword = null;
            byte[] bytesOfPrivatePassword = null;

            /* Needed keys for the communication */
            byte[] cryptKeyInit;
            byte[] cryptKeyPriv;
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                logger.warn("No such algorithm, MD5: {}", e.getMessage());
                return;
            }

            /* First half of the key: MD5 of (GatewayPassword . Salt) */
            bytesOfGatewayPassword = remoteDevice.getGatewayPassword().getBytes(StandardCharsets.UTF_8);
            byte[] combParts1 = new byte[bytesOfGatewayPassword.length + remoteDevice.getMD5Salt().length];
            System.arraycopy(bytesOfGatewayPassword, 0, combParts1, 0, bytesOfGatewayPassword.length);
            System.arraycopy(remoteDevice.getMD5Salt(), 0, combParts1, bytesOfGatewayPassword.length,
                    remoteDevice.getMD5Salt().length);
            md5K1 = md.digest(combParts1);

            /* Second half of the key: - Initial: MD5 of ( Salt) */
            md5K2Init = md.digest(remoteDevice.getMD5Salt());

            /* Second half of the key: - private: MD5 of ( Salt . PrivatePassword) */
            bytesOfPrivatePassword = remoteDevice.getPrivatePassword().getBytes(StandardCharsets.UTF_8);
            byte[] combParts2 = new byte[bytesOfPrivatePassword.length + remoteDevice.getMD5Salt().length];
            System.arraycopy(remoteDevice.getMD5Salt(), 0, combParts2, 0, remoteDevice.getMD5Salt().length);
            System.arraycopy(bytesOfPrivatePassword, 0, combParts2, remoteDevice.getMD5Salt().length,
                    bytesOfPrivatePassword.length);
            md5K2Private = md.digest(combParts2);

            /* Create Keys */
            cryptKeyInit = new byte[md5K1.length + md5K2Init.length];
            System.arraycopy(md5K1, 0, cryptKeyInit, 0, md5K1.length);
            System.arraycopy(md5K2Init, 0, cryptKeyInit, md5K1.length, md5K2Init.length);
            remoteDevice.setCryptKeyInit(cryptKeyInit);

            cryptKeyPriv = new byte[md5K1.length + md5K2Private.length];
            System.arraycopy(md5K1, 0, cryptKeyPriv, 0, md5K1.length);
            System.arraycopy(md5K2Private, 0, cryptKeyPriv, md5K1.length, md5K2Private.length);
            remoteDevice.setCryptKeyPriv(cryptKeyPriv);
        }
    }
}
