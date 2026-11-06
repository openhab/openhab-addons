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
package org.openhab.binding.atv.internal.client.protocols.companion;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.exceptions.ProtocolError;
import org.openhab.binding.atv.internal.client.support.Tlv8;

/**
 * Shared helpers for the Companion pairing procedures.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class CompanionAuth {

    /** OPACK key carrying the pairing TLV. */
    static final String PAIRING_DATA_KEY = "_pd";

    private CompanionAuth() {
    }

    /**
     * Extracts and validates the pairing TLV from an auth response message.
     *
     * @param message received OPACK dictionary
     * @return the raw pairing TLV bytes
     * @throws AuthenticationError if the message has no pairing data or the TLV contains an
     *             error entry
     * @throws ProtocolError if the pairing data has an unexpected type
     */
    static byte[] getPairingData(Map<String, Object> message) {
        Object pairingData = message.get(PAIRING_DATA_KEY);
        if (pairingData == null) {
            throw new AuthenticationError("no pairing data in message");
        }
        if (!(pairingData instanceof byte[] tlvBytes)) {
            throw new ProtocolError("Pairing data has unexpected type: " + pairingData.getClass());
        }
        Map<Integer, byte[]> tlv = Tlv8.read(tlvBytes);
        if (tlv.containsKey(Tlv8.TlvValue.Error.value())) {
            throw new AuthenticationError(Tlv8.stringify(tlv));
        }
        return tlvBytes;
    }

    /**
     * Waits for an auth exchange to complete, unwrapping completion exceptions.
     *
     * @param future the exchange future
     * @return the response dictionary
     */
    static Map<String, Object> awaitExchange(java.util.concurrent.CompletableFuture<Map<String, Object>> future) {
        try {
            return future.join();
        } catch (java.util.concurrent.CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeCause) {
                throw runtimeCause;
            }
            throw new ProtocolError("auth exchange failed", cause == null ? e : cause);
        }
    }
}
