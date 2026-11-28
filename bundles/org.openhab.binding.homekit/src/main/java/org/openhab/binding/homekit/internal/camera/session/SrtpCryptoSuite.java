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
package org.openhab.binding.homekit.internal.camera.session;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of the SRTP crypto suites that may be negotiated for HomeKit camera streams.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum SrtpCryptoSuite {
    AES_CM_128_HMAC_SHA1_80(0x00, 16, 14),
    AES_CM_256_HMAC_SHA1_80(0x01, 32, 14),
    NONE(0x02, 0, 0);

    public final int value;
    public final int keyLength;
    public final int saltLength;

    SrtpCryptoSuite(int value, int keyLength, int saltLength) {
        this.value = value;
        this.keyLength = keyLength;
        this.saltLength = saltLength;
    }

    public static SrtpCryptoSuite fromValue(int value) {
        for (SrtpCryptoSuite suite : values()) {
            if (suite.value == value) {
                return suite;
            }
        }
        throw new IllegalArgumentException("Unknown SRTP crypto suite: " + value);
    }
}
