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
package org.openhab.binding.homeconnectdirect.internal.service.websocket;

import java.net.Socket;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLEngine;

import org.conscrypt.PSKKeyManager;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * PSK key manager implementation using Conscrypt.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("deprecation")
public class ConscryptPskKeyManager implements PSKKeyManager {

    private static final String PSK_ALGORITHM = "PSK";

    private final String identityHint;
    private final byte[] key;

    public ConscryptPskKeyManager(String identityHint, byte[] key) {
        this.identityHint = identityHint;
        this.key = key;
    }

    @Override
    public @Nullable String chooseServerKeyIdentityHint(@Nullable Socket socket) {
        return identityHint;
    }

    @Override
    public @Nullable String chooseServerKeyIdentityHint(@Nullable SSLEngine sslEngine) {
        return identityHint;
    }

    @Override
    public @Nullable String chooseClientKeyIdentity(@Nullable String s, @Nullable Socket socket) {
        return identityHint;
    }

    @Override
    public @Nullable String chooseClientKeyIdentity(@Nullable String s, @Nullable SSLEngine sslEngine) {
        return identityHint;
    }

    @Override
    public @Nullable SecretKey getKey(@Nullable String s, @Nullable String s1, @Nullable Socket socket) {
        return new SecretKeySpec(key, PSK_ALGORITHM);
    }

    @Override
    public @Nullable SecretKey getKey(@Nullable String s, @Nullable String s1, @Nullable SSLEngine sslEngine) {
        return new SecretKeySpec(key, PSK_ALGORITHM);
    }
}
