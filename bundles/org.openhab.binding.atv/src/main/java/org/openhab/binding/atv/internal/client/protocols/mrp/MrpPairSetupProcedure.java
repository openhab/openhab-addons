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
package org.openhab.binding.atv.internal.client.protocols.mrp;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairSetup;
import org.openhab.binding.atv.internal.client.exceptions.AuthenticationError;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;
import org.openhab.binding.atv.internal.client.support.Tlv8;

/**
 * Performs pairing and returns new credentials over MRP by driving
 * {@link HapPairSetup} (M1-M6) through {@code CRYPTO_PAIRING} messages.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpPairSetupProcedure {

    private final MrpProtocol protocol;
    private final HapPairSetup srp;

    private byte @Nullable [] m2Tlv;

    /**
     * Creates a new pair-setup procedure.
     *
     * @param protocol protocol used for the message exchange
     * @param srp pair-setup state machine (shared with the protocol's pairing id)
     */
    public MrpPairSetupProcedure(MrpProtocol protocol, HapPairSetup srp) {
        this.protocol = protocol;
        this.srp = srp;
    }

    /**
     * Starts the pairing procedure: connects the protocol (skipping the initial
     * messages), sends M1 and stores the device's M2 (salt and public key).
     *
     * @return future completing when the device has displayed its PIN
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Void> startPairing() {
        CompletableFuture<Void> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-pair-setup").start(() -> {
            try {
                protocol.start(true).join();
                ProtocolMessage response = protocol.sendAndReceive(MrpMessages.cryptoPairing(srp.step1(), true), false)
                        .join();
                byte[] tlv = MrpPairVerifyProcedure.pairingData(response);
                checkForError(tlv);
                this.m2Tlv = tlv;
                MrpFutures.completeVoid(result);
            } catch (Throwable t) {
                result.completeExceptionally(unwrap(t));
            }
        });
        return result;
    }

    /**
     * Finishes the pairing process with the PIN shown on screen.
     *
     * @param pin the PIN code
     * @return future resolving to the new credentials
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<HapCredentials> finishPairing(String pin) {
        CompletableFuture<HapCredentials> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-pair-finish").start(() -> {
            try {
                byte[] savedM2Tlv = m2Tlv;
                if (savedM2Tlv == null) {
                    throw new IllegalStateException("startPairing must complete before finishPairing");
                }
                byte[] m3 = srp.step2(savedM2Tlv, pin);
                ProtocolMessage m4Response = protocol.sendAndReceive(MrpMessages.cryptoPairing(m3, false), false)
                        .join();

                byte[] m5 = srp.step3(MrpPairVerifyProcedure.pairingData(m4Response));
                ProtocolMessage m6Response = protocol.sendAndReceive(MrpMessages.cryptoPairing(m5, false), false)
                        .join();

                result.complete(srp.step4(MrpPairVerifyProcedure.pairingData(m6Response)));
            } catch (Throwable t) {
                result.completeExceptionally(unwrap(t));
            }
        });
        return result;
    }

    /** Raises {@link AuthenticationError} when the TLV carries an error entry. */
    private static void checkForError(byte[] tlvBytes) {
        Map<Integer, byte[]> tlv = Tlv8.read(tlvBytes);
        byte[] error = tlv.get(Tlv8.TlvValue.Error.value());
        if (error != null) {
            throw new AuthenticationError(
                    "pairing failed with error code " + (error.length > 0 ? error[0] & 0xFF : -1));
        }
    }

    private static Throwable unwrap(Throwable t) {
        if (t instanceof java.util.concurrent.CompletionException completion) {
            Throwable cause = completion.getCause();
            if (cause != null) {
                return cause;
            }
        }
        return t;
    }
}
