/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal;

import java.io.UnsupportedEncodingException;
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
 * The {@link MiIoCrypto} is responsible for creating Xiaomi messages.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoCrypto {

    public static byte[] md5(byte[] source) throws MiIoCryptoException {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            return m.digest(source);
        } catch (NoSuchAlgorithmException e) {
            throw new MiIoCryptoException(e.getMessage());
        }
    }

    public static byte[] iv(byte[] token) throws MiIoCryptoException {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] ivbuf = new byte[32];
            System.arraycopy(m.digest(token), 0, ivbuf, 0, 16);
            System.arraycopy(token, 0, ivbuf, 16, 16);
            return m.digest(ivbuf);
        } catch (NoSuchAlgorithmException e) {
            throw new MiIoCryptoException(e.getMessage());
        }
    }

    public static byte[] encrypt(byte[] cipherText, byte[] key, byte[] iv) throws MiIoCryptoException {
        try {
            IvParameterSpec vector = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, vector);
            byte[] encrypted = cipher.doFinal(cipherText);
            return encrypted;
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new MiIoCryptoException(e.getMessage());
        }
    }

    public static byte[] encrypt(byte[] text, byte[] token) throws MiIoCryptoException {
        return encrypt(text, md5(token), iv(token));
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) throws MiIoCryptoException {
        try {
            IvParameterSpec vector = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, vector);
            byte[] crypted = cipher.doFinal(cipherText);
            return (crypted);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
            throw new MiIoCryptoException(e.getMessage());
        }
    }

    public static byte[] decrypt(byte[] cipherText, byte[] token) throws MiIoCryptoException {
        return decrypt(cipherText, md5(token), iv(token));
    }

    public static String decryptTolken(byte[] cipherText) throws MiIoCryptoException {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");

            SecretKeySpec keySpec = new SecretKeySpec(new byte[16], "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(cipherText);
            try {
                return new String(decrypted, "UTF-8").trim();
            } catch (UnsupportedEncodingException e) {
                return new String(decrypted).trim();
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
                | BadPaddingException e) {
            throw new MiIoCryptoException(e.getMessage());
        }
    }
}
