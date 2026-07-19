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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;
import org.openhab.binding.atv.internal.client.auth.HapPairVerify;
import org.openhab.binding.atv.internal.client.auth.PairVerifyProcedure;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.CryptoPairingMessageOuterClass.CryptoPairingMessage;
import org.openhab.binding.atv.internal.client.protocols.mrp.dto.ProtocolMessageOuterClass.ProtocolMessage;

/**
 * Verifies credentials and derives new encryption keys over MRP by driving
 * {@link HapPairVerify} through {@code CRYPTO_PAIRING} messages. Crypto messages carry no
 * identifier, so correlation uses the synthetic {@code "type_&lt;n&gt;"} key.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class MrpPairVerifyProcedure implements PairVerifyProcedure {

    private final MrpProtocol protocol;
    private final HapPairVerify verifier;

    /**
     * Creates a new verify procedure with a fresh session key pair.
     *
     * @param protocol protocol used for the message exchange
     * @param credentials credentials to verify
     */
    public MrpPairVerifyProcedure(MrpProtocol protocol, HapCredentials credentials) {
        this.protocol = protocol;
        this.verifier = new HapPairVerify(credentials);
    }

    /**
     * Verifies credentials with the device.
     *
     * @return future resolving to {@code true} on success, exceptionally with
     *         {@code AuthenticationError} on failure
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    public CompletableFuture<Boolean> verifyCredentials() {
        CompletableFuture<Boolean> result = new CompletableFuture<>();
        Thread.ofVirtual().name("mrp-pair-verify").start(() -> {
            try {
                ProtocolMessage m2 = protocol
                        .sendAndReceive(MrpMessages.cryptoPairing(verifier.verify1(), false), false).join();
                byte[] m3 = verifier.verify2(pairingData(m2));
                protocol.sendAndReceive(MrpMessages.cryptoPairing(m3, false), false).join();
                // The M4 status code is not checked
                result.complete(true);
            } catch (Throwable t) {
                Throwable cause = t;
                if (t instanceof java.util.concurrent.CompletionException completion) {
                    Throwable inner = completion.getCause();
                    if (inner != null) {
                        cause = inner;
                    }
                }
                result.completeExceptionally(cause);
            }
        });
        return result;
    }

    @Override
    public EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        return verifier.encryptionKeys(salt, outputInfo, inputInfo);
    }

    /** Extracts raw pairing data from a crypto pairing response. */
    static byte[] pairingData(ProtocolMessage message) {
        CryptoPairingMessage inner = (CryptoPairingMessage) MrpExtensions.extractInner(message);
        return inner.getPairingData().toByteArray();
    }
}
