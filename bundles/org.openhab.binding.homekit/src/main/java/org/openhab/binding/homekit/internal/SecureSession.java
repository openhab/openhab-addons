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
package org.openhab.binding.homekit.internal;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class SecureSession {

    private final byte[] writeKey;
    private final byte[] readKey;
    private final AtomicInteger writeCounter = new AtomicInteger(0);
    private final AtomicInteger readCounter = new AtomicInteger(0);

    public SecureSession(SessionKeys keys) {
        this.writeKey = keys.writeKey;
        this.readKey = keys.readKey;
    }

    public byte[] encrypt(byte[] plaintext) {
        byte[] nonce = generateNonce(writeCounter.getAndIncrement());
        return ChaCha20.encrypt(writeKey, nonce, plaintext);
    }

    public byte[] decrypt(byte[] ciphertext) {
        byte[] nonce = generateNonce(readCounter.getAndIncrement());
        return ChaCha20.decrypt(readKey, nonce, ciphertext);
    }

    private byte[] generateNonce(int counter) {
        byte[] nonce = new byte[12];
        nonce[4] = (byte) ((counter >> 24) & 0xFF);
        nonce[5] = (byte) ((counter >> 16) & 0xFF);
        nonce[6] = (byte) ((counter >> 8) & 0xFF);
        nonce[7] = (byte) (counter & 0xFF);
        return nonce;
    }
}