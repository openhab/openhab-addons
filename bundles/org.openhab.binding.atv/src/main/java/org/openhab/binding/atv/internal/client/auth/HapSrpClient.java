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

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * SRP-6a client for HAP Pair-Setup.
 *
 * <p>
 * Uses the RFC 5054 3072-bit group (generator 5), SHA-512, a fixed username {@code "Pair-Setup"},
 * and session key {@code K = H(S)}. The proofs follow the RFC 2945 form
 * {@code M1 = H((H(N) xor H(g)) || H(I) || salt || A || B || K)} and {@code M2 = H(A || M1 || K)},
 * computed by {@link SrpContext} rather than by BouncyCastle (whose proof formula differs).
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class HapSrpClient extends AbstractSrpClient {

    private static final String USERNAME = "Pair-Setup";

    /** Creates a client with a secure-random private ephemeral. */
    public HapSrpClient() {
        this(secureRandomSupplier());
    }

    /**
     * Creates a client with an injectable private ephemeral, for reproducible handshakes in tests.
     *
     * @param privateSupplier supplies the raw bytes of the client private ephemeral {@code a}
     */
    public HapSrpClient(Supplier<byte[]> privateSupplier) {
        super(SrpContext.hap(), privateSupplier);
    }

    /**
     * First pairing step: begins the session with the given PIN as the SRP password.
     *
     * @param pin the PIN shown on the device
     */
    public void step1(String pin) {
        begin(USERNAME, pin);
    }
}
