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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.InvalidResponseError;
import org.openhab.binding.atv.internal.client.support.Tlv8;
import org.openhab.binding.atv.internal.client.support.Tlv8.TlvValue;

/**
 * Small helpers shared by the HAP pairing state machines for consuming TLV8 responses: a
 * response containing an {@code Error} entry fails authentication with the stringified TLV
 * as message.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class HapTlv {

    private HapTlv() {
    }

    /**
     * Parses a TLV8 response and fails if it contains an error entry.
     *
     * @param tlvBytes raw TLV8 bytes
     * @return the parsed entries
     * @throws AuthenticationError if the TLV contains an {@code Error} entry
     */
    static Map<Integer, byte[]> readChecked(byte[] tlvBytes) {
        Map<Integer, byte[]> tlv = Tlv8.read(tlvBytes);
        if (tlv.containsKey(TlvValue.Error.value())) {
            throw new AuthenticationError(Tlv8.stringify(tlv));
        }
        return tlv;
    }

    /**
     * Returns a required entry from a parsed TLV.
     *
     * @param tlv parsed TLV entries
     * @param key the required tag
     * @return the entry value
     * @throws InvalidResponseError if the tag is missing
     */
    static byte[] required(Map<Integer, byte[]> tlv, TlvValue key) {
        byte[] value = tlv.get(key.value());
        if (value == null) {
            throw new InvalidResponseError("missing " + key.name() + " in TLV");
        }
        return value;
    }
}
