/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.session;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.CryptoUtils;

/**
 * Manages a secure session using ChaCha20 encryption for a HomeKit accessory.
 * This class handles encryption and decryption of messages using session keys.
 * It maintains separate counters for read and write operations to ensure nonce uniqueness.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SecureSession {

    private final byte[] writeKey;
    private final byte[] readKey;
    private final AtomicInteger writeCounter = new AtomicInteger(0);
    private final AtomicInteger readCounter = new AtomicInteger(0);

    public SecureSession(SessionKeys keys) {
        this.writeKey = keys.getWriteKey();
        this.readKey = keys.getReadKey();
    }

    /**
     * * Encrypts the given plaintext using the write key and a unique nonce.
     *
     * @param plaintext The plaintext to encrypt.
     * @return The encrypted ciphertext.
     * @throws Exception
     */
    public byte[] encrypt(byte[] plaintext) throws Exception {
        byte[] nonce = generateNonce(writeCounter.getAndIncrement());
        return CryptoUtils.encrypt(writeKey, nonce, plaintext);
    }

    /**
     * Decrypts the given ciphertext using the read key and a unique nonce.
     *
     * @param ciphertext The ciphertext to decrypt.
     * @return The decrypted plaintext.
     * @throws Exception
     */
    public byte[] decrypt(byte[] ciphertext) throws Exception {
        byte[] nonce = generateNonce(readCounter.getAndIncrement());
        return CryptoUtils.decrypt(readKey, nonce, ciphertext);
    }

    /**
     * * Generates a 12-byte nonce using the given counter.
     * The first 4 bytes are zero, and the last 8 bytes are the counter in big-endian format.
     *
     * @param counter The counter value.
     * @return The generated nonce.
     */
    private byte[] generateNonce(int counter) {
        byte[] nonce = new byte[12];
        nonce[4] = (byte) ((counter >> 24) & 0xFF);
        nonce[5] = (byte) ((counter >> 16) & 0xFF);
        nonce[6] = (byte) ((counter >> 8) & 0xFF);
        nonce[7] = (byte) (counter & 0xFF);
        return nonce;
    }
}
