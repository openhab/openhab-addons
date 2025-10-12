/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homekit.internal.hap_services;

import static org.openhab.binding.homekit.internal.crypto.CryptoUtils.toHex;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homekit.internal.crypto.Tlv8Codec;
import org.openhab.binding.homekit.internal.enums.ErrorCode;
import org.openhab.binding.homekit.internal.enums.PairingMethod;
import org.openhab.binding.homekit.internal.enums.PairingState;
import org.openhab.binding.homekit.internal.enums.TlvType;
import org.openhab.binding.homekit.internal.transport.IpTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service to remove an existing pairing with a HomeKit accessory.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class PairRemoveClient {

    private static final String ENDPOINT_PAIR_REMOVE = "/pairings";
    private static final String CONTENT_TYPE = "application/pairing+tlv8";

    private final Logger logger = LoggerFactory.getLogger(PairRemoveClient.class);

    private final IpTransport ipTransport;
    private final byte[] controllerId;

    public PairRemoveClient(IpTransport ipTransport, byte[] controllerId) throws Exception {
        logger.debug("Created..");
        this.ipTransport = ipTransport;
        this.controllerId = controllerId;
    }

    public void remove() throws Exception {
        logger.debug("Pair-Remove: starting removal");
        Map<Integer, byte[]> tlv = new LinkedHashMap<>();
        tlv.put(TlvType.STATE.value, new byte[] { PairingState.M1.value });
        tlv.put(TlvType.METHOD.value, new byte[] { PairingMethod.REMOVE.value });
        tlv.put(TlvType.IDENTIFIER.value, controllerId);
        loggerTraceTlv(tlv);
        Validator.validate(PairingMethod.REMOVE, tlv);

        byte[] response = ipTransport.post(ENDPOINT_PAIR_REMOVE, CONTENT_TYPE, Tlv8Codec.encode(tlv));
        logger.debug("Pair-Remove: processing response");
        Map<Integer, byte[]> tlv2 = Tlv8Codec.decode(response);
        loggerTraceTlv(tlv2);
        Validator.validate(PairingMethod.REMOVE, tlv2);
    }

    private void loggerTraceTlv(Map<Integer, byte[]> tlv) {
        if (logger.isTraceEnabled()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<Integer, byte[]> entry : tlv.entrySet()) {
                sb.append(String.format("\n - 0x%02x: %s", entry.getKey(), toHex(entry.getValue())));
            }
            logger.trace("{}", sb.toString());
        }
    }

    /**
     * Helper class that validates the TLV map for the specification required pairing state.
     */
    protected static class Validator {

        private static final Map<PairingState, Set<Integer>> SPECIFICATION_REQUIRED_KEYS = Map.of( //
                PairingState.M1, Set.of(TlvType.STATE.value, TlvType.METHOD.value, TlvType.IDENTIFIER.value), //
                PairingState.M2, Set.of(TlvType.STATE.value));

        /**
         * Validates the TLV map for the specification required pairing state.
         *
         * @throws SecurityException if required keys are missing or state is invalid
         */
        public static void validate(PairingMethod method, Map<Integer, byte[]> tlv) throws SecurityException {
            if (tlv.containsKey(TlvType.ERROR.value)) {
                byte[] err = tlv.get(TlvType.ERROR.value);
                ErrorCode code = err != null && err.length > 0 ? ErrorCode.from(err[0]) : ErrorCode.UNKNOWN;
                throw new SecurityException(
                        "Pairing method '%s' action failed with error '%s'".formatted(method.name(), code.name()));
            }

            byte[] state = tlv.get(TlvType.STATE.value);
            if (state == null || state.length != 1) {
                throw new SecurityException("Missing or invalid 'STATE' TLV (0x06)");
            }

            PairingState pairingState = PairingState.from(state[0]);
            Set<Integer> expectedKeys = SPECIFICATION_REQUIRED_KEYS.get(pairingState);

            if (expectedKeys == null) {
                throw new SecurityException(
                        "Pairing method '%s' unexpected state '%s'".formatted(method.name(), pairingState.name()));
            }

            for (Integer key : expectedKeys) {
                if (!tlv.containsKey(key)) {
                    throw new SecurityException("Pairing method '%s' state '%s' required TLV '0x%02x' missing."
                            .formatted(method.name(), pairingState.name(), key));
                }
            }
        }
    }
}
