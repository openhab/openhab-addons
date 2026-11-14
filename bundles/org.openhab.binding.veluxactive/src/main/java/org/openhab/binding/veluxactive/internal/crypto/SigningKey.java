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
package org.openhab.binding.veluxactive.internal.crypto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Gateway-issued signing credentials. Array access is defensive and diagnostics never include secrets.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public final class SigningKey {
    private final byte[] id;
    private final byte[] key;

    public SigningKey(byte[] id, byte[] key) {
        if (id.length != 16 || key.length != 32) {
            throw new IllegalArgumentException("Invalid signing credential lengths.");
        }
        this.id = id.clone();
        this.key = key.clone();
    }

    public byte[] id() {
        return id.clone();
    }

    public byte[] key() {
        return key.clone();
    }

    @Override
    public String toString() {
        return "SigningKey[redacted]";
    }
}
