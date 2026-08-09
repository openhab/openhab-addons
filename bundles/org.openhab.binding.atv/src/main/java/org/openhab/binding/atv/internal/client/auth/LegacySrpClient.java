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
 * SRP-6a client for AirPlay legacy device authentication.
 *
 * <p>
 * Uses the RFC 5054 2048-bit group (generator 2), SHA-1, and Apple's non-standard session key
 * {@code K = H(S || 0x00000000) || H(S || 0x00000001)} (two SHA-1 passes concatenated, 40 bytes).
 * The username is the uppercase-hex client identifier and the password is the PIN, both supplied to
 * {@link #step1(String, String)}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class LegacySrpClient extends AbstractSrpClient {

    /** Creates a client with a secure-random private ephemeral. */
    public LegacySrpClient() {
        this(secureRandomSupplier());
    }

    /**
     * Creates a client with an injectable private ephemeral, for reproducible handshakes in tests.
     *
     * @param privateSupplier supplies the raw bytes of the client private ephemeral {@code a}
     */
    public LegacySrpClient(Supplier<byte[]> privateSupplier) {
        super(SrpContext.legacyAirPlay(), privateSupplier);
    }

    /**
     * First authentication step: begins the session with the given username and password.
     *
     * @param username the SRP username (uppercase-hex client identifier)
     * @param password the PIN shown on the device
     */
    public void step1(String username, String password) {
        begin(username, password);
    }
}
