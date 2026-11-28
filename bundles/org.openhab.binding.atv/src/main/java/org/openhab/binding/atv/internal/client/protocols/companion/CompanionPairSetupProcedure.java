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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairSetup;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;

/**
 * Performs Companion pair-setup and returns new credentials.
 *
 * <p>
 * The pairing TLVs of the shared {@link HapPairSetup} state machine are wrapped in the
 * {@code _pd} OPACK entry, with {@code _pwTy: 1} added to every outgoing setup message. The
 * first message uses frame type {@code PS_Start}, all following ones {@code PS_Next}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class CompanionPairSetupProcedure {

    private final CompanionProtocol protocol;
    private final HapPairSetup pairSetup;

    private byte @Nullable [] m2Tlv;

    CompanionPairSetupProcedure(CompanionProtocol protocol, HapPairSetup pairSetup) {
        this.protocol = protocol;
        this.pairSetup = pairSetup;
    }

    /**
     * Starts the pairing procedure: connects (running pair-verify if credentials already
     * exist) and exchanges M1/M2. Blocking; call from a dedicated thread.
     *
     * @throws AuthenticationError if the device rejects the request
     */
    void startPairing() {
        protocol.start();

        Map<String, Object> m1Message = new LinkedHashMap<>();
        m1Message.put(CompanionAuth.PAIRING_DATA_KEY, pairSetup.step1());
        m1Message.put("_pwTy", 1L);
        Map<String, Object> response = CompanionAuth
                .awaitExchange(protocol.exchangeAuth(FrameType.PS_Start, m1Message));

        m2Tlv = CompanionAuth.getPairingData(response);
    }

    /**
     * Finishes the pairing process (M3-M6). Blocking; call from a dedicated thread.
     *
     * @param pin the PIN shown on the device
     * @return the new credentials
     * @throws AuthenticationError if pairing fails (e.g. wrong PIN)
     */
    HapCredentials finishPairing(String pin) {
        byte[] currentM2Tlv = m2Tlv;
        if (currentM2Tlv == null) {
            throw new IllegalStateException("startPairing must be called before finishPairing");
        }
        byte[] m3Tlv = pairSetup.step2(currentM2Tlv, pin);

        Map<String, Object> m3Message = new LinkedHashMap<>();
        m3Message.put(CompanionAuth.PAIRING_DATA_KEY, m3Tlv);
        m3Message.put("_pwTy", 1L);
        Map<String, Object> response = CompanionAuth.awaitExchange(protocol.exchangeAuth(FrameType.PS_Next, m3Message));

        byte[] m4Tlv = CompanionAuth.getPairingData(response);
        byte[] m5Tlv = pairSetup.step3(m4Tlv);

        Map<String, Object> m5Message = new LinkedHashMap<>();
        m5Message.put(CompanionAuth.PAIRING_DATA_KEY, m5Tlv);
        m5Message.put("_pwTy", 1L);
        response = CompanionAuth.awaitExchange(protocol.exchangeAuth(FrameType.PS_Next, m5Message));

        byte[] m6Tlv = CompanionAuth.getPairingData(response);
        return pairSetup.step4(m6Tlv);
    }
}
