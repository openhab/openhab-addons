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
 * Something that has established a shared secret with the device and can derive
 * directional session encryption keys from it.
 *
 * <p>
 * Implemented by {@link HapPairVerify} (regular HAP verify) and {@link HapPairSetup}
 * (transient mode). The actual message exchange is transport specific and driven by the
 * protocol implementations.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface PairVerifyProcedure {

    /**
     * Returns the derived session encryption keys.
     *
     * @param salt HKDF salt string (e.g. {@code "MediaRemote-Salt"})
     * @param outputInfo HKDF info for the key encrypting data sent to the device
     * @param inputInfo HKDF info for the key decrypting data received from the device
     * @return the derived key pair
     */
    EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo);
}
