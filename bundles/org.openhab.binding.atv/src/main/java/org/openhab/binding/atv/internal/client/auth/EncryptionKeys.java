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
package org.openhab.binding.atv.internal.client.auth;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A pair of directional session keys derived after pair-verify (or transient pair-setup).
 *
 * @param outputKey key used to encrypt data sent to the device
 * @param inputKey key used to decrypt data received from the device
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record EncryptionKeys(byte[] outputKey, byte[] inputKey) {

    /** Canonical constructor taking defensive copies. */
    public EncryptionKeys {
        outputKey = outputKey.clone();
        inputKey = inputKey.clone();
    }

    @Override
    public byte[] outputKey() {
        return outputKey.clone();
    }

    @Override
    public byte[] inputKey() {
        return inputKey.clone();
    }
}
