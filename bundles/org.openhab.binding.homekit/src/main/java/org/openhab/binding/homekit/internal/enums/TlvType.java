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
package org.openhab.binding.homekit.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of TLV (Type-Length-Value) types used in HomeKit communication.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum TlvType {
    METHOD(0x00),
    IDENTIFIER(0x01),
    SALT(0x02),
    PUBLIC_KEY(0x03),
    PROOF(0x04),
    ENCRYPTED_DATA(0x05),
    STATE(0x06),
    ERROR(0x07),
    RETRY_DELAY(0x08),
    CERTIFICATE(0x09),
    SIGNATURE(0x0A),
    PERMISSIONS(0x0B),
    FRAGMENT_DATA(0x0C),
    FRAGMENT_LAST(0x0D),
    FLAGS(0x13),
    SEPARATOR((byte) 0xFF);

    public final int value;

    TlvType(int value) {
        this.value = value;
    }
}
