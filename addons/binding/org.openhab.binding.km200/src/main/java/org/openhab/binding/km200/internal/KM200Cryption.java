/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.km200.internal;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The KM200Cryption is managing the en- and decription of the communication to the device
 *
 * @author Markus Eckhardt - Initial contribution
 *
 */
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
        int encrypt_padchar = bSize - (bdata.length % bSize);
        byte[] padchars = new String(new char[encrypt_padchar]).getBytes(cSet);
        byte[] padded_data = new byte[bdata.length + padchars.length];
        System.arraycopy(bdata, 0, padded_data, 0, bdata.length);
        System.arraycopy(padchars, 0, padded_data, bdata.length, padchars.length);
        return padded_data;
    }

    /**
     * This function does the decoding for a new message from the device
     *
     */
    public String decodeMessage(byte[] encoded) {
        String retString = null;
        byte[] decodedB64 = null;

        decodedB64 = Base64.decodeBase64(encoded);
        try {
            /* Check whether the length of the decryptData is NOT multiplies of 16 */
            if ((decodedB64.length & 0xF) != 0) {
                /* Return the data */
                retString = new String(decodedB64, remoteDevice.getCharSet());
                return retString;
            }
            // --- create cipher
            final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(remoteDevice.getCryptKeyPriv(), "AES"));
            byte[] decryptedData = cipher.doFinal(decodedB64);
            byte[] decryptedDataWOZP = removeZeroPadding(decryptedData);
            return (new String(decryptedDataWOZP, remoteDevice.getCharSet()));
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            logger.debug("Exception on encoding: {}", e);
            return null;
        }
    }

    /**
     * This function does the encoding for a new message to the device
     *
     */
    public byte[] encodeMessage(String data) {
        try {
            // --- create cipher
            byte[] bdata = data.getBytes(remoteDevice.getCharSet());
            final Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(remoteDevice.getCryptKeyPriv(), "AES"));
            int bsize = cipher.getBlockSize();
            logger.debug("Add Padding, encrypt AES and B64..");
            byte[] encryptedData = cipher.doFinal(addZeroPadding(bdata, bsize, remoteDevice.getCharSet()));
            try {
                return (Base64.encodeBase64(encryptedData));
            } catch (IllegalArgumentException e) {
                logger.info("Base64encoding not possible: {}", e.getMessage());
            }
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            logger.error("Exception on encoding: {}", e);
        }
        return null;
    }

    /**
     * This function creates the private key from the MD5Salt, the device and the private password
     *
     * @author Markus Eckhardt
     */
    public void recreateKeys() {
        if (StringUtils.isNotBlank(remoteDevice.getGatewayPassword())
                && StringUtils.isNotBlank(remoteDevice.getPrivatePassword()) && remoteDevice.getMD5Salt() != null) {
            byte[] MD5_K1 = null;
            byte[] MD5_K2_Init = null;
            byte[] MD5_K2_Private = null;
            byte[] bytesOfGatewayPassword = null;
            byte[] bytesOfPrivatePassword = null;

            /* Needed keys for the communication */
            byte[] cryptKeyInit;
            byte[] cryptKeyPriv;
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                logger.error("No such algorithm, MD5: {}", e.getMessage());
                return;
            }

            /* First half of the key: MD5 of (GatewayPassword . Salt) */
            try {
                bytesOfGatewayPassword = remoteDevice.getGatewayPassword().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("No such encoding, UTF-8: {}", e.getMessage());
                return;
            }
            byte[] CombParts1 = new byte[bytesOfGatewayPassword.length + remoteDevice.getMD5Salt().length];
            System.arraycopy(bytesOfGatewayPassword, 0, CombParts1, 0, bytesOfGatewayPassword.length);
            System.arraycopy(remoteDevice.getMD5Salt(), 0, CombParts1, bytesOfGatewayPassword.length,
                    remoteDevice.getMD5Salt().length);
            MD5_K1 = md.digest(CombParts1);

            /* Second half of the key: - Initial: MD5 of ( Salt) */
            MD5_K2_Init = md.digest(remoteDevice.getMD5Salt());

            /* Second half of the key: - private: MD5 of ( Salt . PrivatePassword) */
            try {
                bytesOfPrivatePassword = remoteDevice.getPrivatePassword().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("No such encoding, UTF-8: {}", e.getMessage());
                return;
            }
            byte[] CombParts2 = new byte[bytesOfPrivatePassword.length + remoteDevice.getMD5Salt().length];
            System.arraycopy(remoteDevice.getMD5Salt(), 0, CombParts2, 0, remoteDevice.getMD5Salt().length);
            System.arraycopy(bytesOfPrivatePassword, 0, CombParts2, remoteDevice.getMD5Salt().length,
                    bytesOfPrivatePassword.length);
            MD5_K2_Private = md.digest(CombParts2);

            /* Create Keys */
            cryptKeyInit = new byte[MD5_K1.length + MD5_K2_Init.length];
            System.arraycopy(MD5_K1, 0, cryptKeyInit, 0, MD5_K1.length);
            System.arraycopy(MD5_K2_Init, 0, cryptKeyInit, MD5_K1.length, MD5_K2_Init.length);
            remoteDevice.setCryptKeyInit(cryptKeyInit);

            cryptKeyPriv = new byte[MD5_K1.length + MD5_K2_Private.length];
            System.arraycopy(MD5_K1, 0, cryptKeyPriv, 0, MD5_K1.length);
            System.arraycopy(MD5_K2_Private, 0, cryptKeyPriv, MD5_K1.length, MD5_K2_Private.length);
            remoteDevice.setCryptKeyPriv(cryptKeyPriv);
        }
    }
}
