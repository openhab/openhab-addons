/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.rscp.util;

import java.util.Arrays;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BouncyAES256Helper} is responsible for the encryption support.
 *
 * @author Brendon Votteler - Initial Contribution
 */
public class BouncyAES256Helper implements AES256Helper {
    private static final Logger logger = LoggerFactory.getLogger(BouncyAES256Helper.class);
    private final int messageBlockSize = 256;
    private byte[] key;
    private byte[] ivEnc;
    private byte[] ivDec;

    public static BouncyAES256Helper createBouncyAES256Helper(String key) {
        BouncyAES256Helper bouncyAES256Helper = new BouncyAES256Helper();
        bouncyAES256Helper.initializeFromKey(key);
        return bouncyAES256Helper;
    }

    private void initializeFromKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        byte[] aesKey = new byte[32];
        byte[] tmp = key.getBytes();
        // copy password into key
        logger.debug("Setting up encryption password...");
        for (int i = 0; i < aesKey.length; i++) {
            if (i < tmp.length) { // got a byte from password, copy it
                aesKey[i] = tmp[i];
            } else { // password bytes used up, fill with 0xFF
                aesKey[i] = (byte) 0xFF;
            }
        }

        logger.debug("Setting up initialization vectors... ");
        // initialize IV with 0xFF for first contact
        byte[] initializationVectorEncrypt = new byte[32];
        byte[] initializationVectorDecrypt = new byte[32];
        Arrays.fill(initializationVectorEncrypt, (byte) 0xFF);
        Arrays.fill(initializationVectorDecrypt, (byte) 0xFF);

        this.init(aesKey, initializationVectorEncrypt, initializationVectorDecrypt);
    }

    @Override
    public void init(byte[] key, byte[] ivEnc, byte[] ivDec) {
        this.key = null;
        this.ivEnc = null;
        this.ivDec = null;
        if (key.length != 32) {
            throw new IllegalArgumentException("Key has to be 32 bytes long.");
        }

        if (ivEnc.length != 32) {
            throw new IllegalArgumentException("IV has to be 32 bytes long.");
        }

        if (ivDec.length != 32) {
            throw new IllegalArgumentException("IV has to be 32 bytes long.");
        }

        this.key = new byte[32];
        System.arraycopy(key, 0, this.key, 0, key.length);

        this.ivEnc = new byte[32];
        System.arraycopy(ivEnc, 0, this.ivEnc, 0, ivEnc.length);

        this.ivDec = new byte[32];
        System.arraycopy(ivEnc, 0, this.ivDec, 0, ivDec.length);
    }

    @Override
    public byte[] encrypt(byte[] message) {
        if (this.key == null || this.ivEnc == null) {
            throw new IllegalStateException("Both key and IV have to be defined prior to encryption.");
        }

        try {
            byte[] sessionKey = this.key;
            byte[] iv = this.ivEnc;

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                    new CBCBlockCipher(new RijndaelEngine(messageBlockSize)), new ZeroBytePadding());

            int keySize = messageBlockSize / 8;

            CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(sessionKey, 0, keySize), iv, 0, keySize);

            cipher.init(true, ivAndKey);
            byte[] encrypted = new byte[cipher.getOutputSize(message.length)];
            int oLen = cipher.processBytes(message, 0, message.length, encrypted, 0);

            cipher.doFinal(encrypted, oLen);

            // update IV
            System.arraycopy(encrypted, encrypted.length - this.ivEnc.length, this.ivEnc, 0, this.ivEnc.length);

            return encrypted;
        } catch (InvalidCipherTextException e) {
            logger.error("Exception encountered during encryption.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedMessage) {
        if (this.key == null || this.ivDec == null) {
            throw new IllegalStateException("Both key and IV have to be defined prior to decryption.");
        }

        if (encryptedMessage == null) {
            return null;
        }

        try {
            byte[] sessionKey = this.key;
            byte[] iv = this.ivDec;

            PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(
                    new CBCBlockCipher(new RijndaelEngine(messageBlockSize)), new ZeroBytePadding());

            int keySize = messageBlockSize / 8;

            CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(sessionKey, 0, keySize), iv, 0, keySize);

            cipher.init(false, ivAndKey);
            byte[] decrypted = new byte[cipher.getOutputSize(encryptedMessage.length)];
            int oLen = cipher.processBytes(encryptedMessage, 0, encryptedMessage.length, decrypted, 0);
            cipher.doFinal(decrypted, oLen);

            // Strip zeroes from decrypted message
            int lastZeroIdx = decrypted.length - 1;
            while (lastZeroIdx >= 0 && decrypted[lastZeroIdx] == 0) {
                --lastZeroIdx;
            }
            decrypted = Arrays.copyOf(decrypted, lastZeroIdx + 1);

            // update IV with the last bytes from the encrypted message
            System.arraycopy(encryptedMessage, encryptedMessage.length - this.ivDec.length, this.ivDec, 0,
                    this.ivDec.length);

            return decrypted;
        } catch (InvalidCipherTextException e) {
            logger.error("Exception encountered during decryption.", e);
            throw new RuntimeException(e);
        }
    }
}