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
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.auth.HapPairVerify;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;

/**
 * Verifies credentials and derives new encryption keys over the Companion transport.
 *
 * <p>
 * The M1 TLV (with our session public key) is sent as {@code PV_Start} with the extra
 * {@code _auTy: 4} entry, the M3 TLV as {@code PV_Next}; both TLVs travel in the {@code _pd}
 * OPACK entry. The crypto itself is the shared {@link HapPairVerify} state machine.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
final class CompanionPairVerifyProcedure {

    private final CompanionProtocol protocol;
    private final HapPairVerify verifier;

    CompanionPairVerifyProcedure(CompanionProtocol protocol, HapPairVerify verifier) {
        this.protocol = protocol;
        this.verifier = verifier;
    }

    /**
     * Verifies credentials with the device. Blocking; call from a dedicated thread.
     *
     * @throws AuthenticationError if verification fails
     */
    void verifyCredentials() {
        Map<String, Object> m1Message = new LinkedHashMap<>();
        m1Message.put(CompanionAuth.PAIRING_DATA_KEY, verifier.verify1());
        m1Message.put("_auTy", 4L);
        Map<String, Object> response = CompanionAuth
                .awaitExchange(protocol.exchangeAuth(FrameType.PV_Start, m1Message));

        byte[] m2Tlv = CompanionAuth.getPairingData(response);
        byte[] m3Tlv = verifier.verify2(m2Tlv);

        Map<String, Object> m3Message = new LinkedHashMap<>();
        m3Message.put(CompanionAuth.PAIRING_DATA_KEY, m3Tlv);
        CompanionAuth.awaitExchange(protocol.exchangeAuth(FrameType.PV_Next, m3Message));

        // The status of the final message is not checked further.
    }

    /**
     * Returns the derived channel encryption keys.
     *
     * @param salt HKDF salt
     * @param outputInfo HKDF info for the output (encrypt) key
     * @param inputInfo HKDF info for the input (decrypt) key
     * @return derived keys
     */
    EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        return verifier.encryptionKeys(salt, outputInfo, inputInfo);
    }
}
