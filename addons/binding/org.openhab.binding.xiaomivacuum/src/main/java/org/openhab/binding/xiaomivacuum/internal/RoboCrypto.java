/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.xiaomivacuum.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * The {@link RoboCrypto} is responsible for creating Xiaomi messages.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class RoboCrypto {

    public static byte[] md5(byte[] source) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        return m.digest(source);
    }

    public static byte[] iv(byte[] token) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance("MD5");
        byte[] ivbuf = new byte[32];
        System.arraycopy(m.digest(token), 0, ivbuf, 0, 16);
        System.arraycopy(token, 0, ivbuf, 16, 16);
        return m.digest(ivbuf);
    }

    public static byte[] encrypt(byte[] cipherText, byte[] key, byte[] iv) throws Exception {
        IvParameterSpec vector = new IvParameterSpec(iv);
        // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        // Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, vector);
        byte[] encrypted = cipher.doFinal(cipherText);
        return encrypted;
    }

    public static byte[] encrypt(byte[] text, byte[] token) throws Exception {
        return encrypt(text, md5(token), iv(token));
    }

    public static byte[] decrypt(byte[] cipherText, byte[] key, byte[] iv) throws Exception {
        // Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        IvParameterSpec vector = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, vector);
        byte[] encrypted = cipher.doFinal(cipherText);
        return (encrypted);
    }

    public static byte[] decrypt(byte[] cipherText, byte[] token) throws Exception {
        return decrypt(cipherText, md5(token), iv(token));
    }

}
