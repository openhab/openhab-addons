/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Manages a secure session using ChaCha20 encryption for a HomeKit accessory.
 * This class handles encryption and decryption of messages using session keys.
 * It maintains separate counters for read and write operations to ensure nonce uniqueness.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SecureSession {

    private final AtomicInteger writeCounter = new AtomicInteger(0);
    private final AtomicInteger readCounter = new AtomicInteger(0);
    private final DecryptingInputStream decryptingInputStream;
    private final EncryptingOutputStream encryptingOutputStream;

    public SecureSession(Socket socket, AsymmetricSessionKeys keys) throws IOException {
        decryptingInputStream = new DecryptingInputStream(socket.getInputStream(), readCounter, keys);
        encryptingOutputStream = new EncryptingOutputStream(socket.getOutputStream(), writeCounter, keys);
    }

    /**
     * Returns the InputStream that decrypts data from the underlying socket input stream.
     *
     * @return an {@link InputStream} being an instance of {@link DecryptingInputStream}
     */
    public InputStream getInputStream() {
        return decryptingInputStream;
    }

    /**
     * Returns the OutputStream that encrypts data to the underlying socket output stream.
     *
     * @return an {@link OutputStream} being an instance of {@link EncryptingOutputStream}
     */
    public OutputStream getOutputStream() {
        return encryptingOutputStream;
    }
}
