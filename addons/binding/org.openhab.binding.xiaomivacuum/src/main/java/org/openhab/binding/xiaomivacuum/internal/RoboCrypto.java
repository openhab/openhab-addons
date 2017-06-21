/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The {@link RoboCrypto} is responsible for creating Xiaomi messages.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class RoboCrypto {

    public static byte[] md5(byte[] source) throws RoboCryptoException {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            return m.digest(source);
        } catch (NoSuchAlgorithmException e) {
            throw new RoboCryptoException(e.getMessage());
        }
    }

    public static byte[] iv(byte[] token) throws RoboCryptoException {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] ivbuf = new byte[32];
            System.arraycopy(m.digest(token), 0, ivbuf, 0, 16);
            System.arraycopy(token, 0, ivbuf, 16, 16);
            return m.digest(ivbuf);
        } catch (NoSuchAlgorithmException e) {
            throw new RoboCryptoException(e.getMessage());
        }
    }

    public static byte[] encrypt(byte[] cipherText, byte[] key, byte[] iv) throws RoboCryptoException {
        try {
            IvParameterSpec vector = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, vector);
            byte[] encrypted = cipher.doFinal(cipherText);
            return encrypted;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RoboCryptoException(e.getMessage());
        }

    }

    public static byte[] encrypt(byte[] text, byte[] token) throws RoboCryptoException {
        return encrypt(text, md5(token), iv(token));
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) throws RoboCryptoException {
        try {
            IvParameterSpec vector = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, vector);
            byte[] encrypted = cipher.doFinal(cipherText);
            return (encrypted);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new RoboCryptoException(e.getMessage());
        }
    }

    public static byte[] decrypt(byte[] cipherText, byte[] token) throws RoboCryptoException {
        return decrypt(cipherText, md5(token), iv(token));
    }
}
