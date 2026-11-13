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
import org.openhab.binding.atv.internal.client.auth.PairVerifyProcedure;

/**
 * Procedure for performing AirPlay Pair-Verify over an HTTP connection.
 *
 * <p>
 * The message exchange is {@link #verifyCredentials()}; key derivation is inherited from
 * {@link PairVerifyProcedure}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface AirPlayPairVerifyProcedure extends PairVerifyProcedure {

    /**
     * Verifies if the stored credentials are valid.
     *
     * @return future completing with {@code true} if encryption keys can be derived
     *         afterwards ({@code encryptionKeys} may be called), {@code false} otherwise
     */
    CompletableFuture<Boolean> verifyCredentials();
}
