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
package org.openhab.binding.atv.internal.client.protocols.airplay;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.auth.HapCredentials;

/**
 * Procedure for performing AirPlay Pair-Setup over an HTTP connection.
 *
 * <p>
 * The optional display name (only used by the HAP flavor's M5 message) is supplied when the
 * procedure is created, since it is known up front by the pairing handler.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface AirPlayPairSetupProcedure {

    /**
     * Starts the pairing process; makes the device show the expected PIN on screen.
     *
     * @return future completing when the process has started
     */
    CompletableFuture<Void> startPairing();

    /**
     * Finishes the pairing process.
     *
     * @param username unused by the current implementations, kept for interface symmetry
     * @param pinCode the PIN code shown on screen
     * @return future completing with the resulting credentials
     */
    CompletableFuture<HapCredentials> finishPairing(String username, String pinCode);
}
