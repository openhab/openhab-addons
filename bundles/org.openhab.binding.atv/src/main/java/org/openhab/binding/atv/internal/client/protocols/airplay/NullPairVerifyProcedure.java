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
import org.openhab.binding.atv.internal.client.auth.EncryptionKeys;
import org.openhab.binding.atv.internal.client.exceptions.NotSupportedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Null implementation for Pair-Verify when no verification is needed.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public final class NullPairVerifyProcedure implements AirPlayPairVerifyProcedure {

    private static final Logger LOGGER = LoggerFactory.getLogger(NullPairVerifyProcedure.class);

    @Override
    public CompletableFuture<Boolean> verifyCredentials() {
        LOGGER.debug("Performing null Pair-Verify");
        return CompletableFuture.completedFuture(false);
    }

    /**
     * Not supported by the null implementation.
     *
     * @throws NotSupportedError always
     */
    @Override
    public EncryptionKeys encryptionKeys(String salt, String outputInfo, String inputInfo) {
        throw new NotSupportedError("encryption keys not supported by null implementation");
    }
}
