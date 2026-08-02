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
package org.openhab.binding.loqed.internal;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.loqed.internal.api.BoltState;
import org.openhab.binding.loqed.internal.api.LoqedApiException;
import org.openhab.binding.loqed.internal.api.LoqedLockData;

/**
 * Common operations provided by cloud and local LOQED bridges.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public interface LoqedBridge {
    /**
     * Refreshes all locks and returns the latest snapshot.
     *
     * @return immutable snapshot of the locks available through this bridge
     */
    List<LoqedLockData> refreshAndGetLocks();

    /** Refreshes all locks and updates their handlers. */
    void refresh();

    /**
     * Changes the bolt state of a lock.
     *
     * @param lockId LOQED lock identifier
     * @param keySecret local API key secret, ignored by cloud bridges
     * @param localKeyId local API key identifier, ignored by cloud bridges
     * @param boltState requested bolt state
     * @throws LoqedApiException if the command cannot be sent
     */
    void setBoltState(String lockId, String keySecret, int localKeyId, BoltState boltState) throws LoqedApiException;

    /**
     * Returns whether state updates normally arrive asynchronously.
     *
     * @return {@code true} if the bridge uses push updates
     */
    default boolean usesPushUpdates() {
        return false;
    }

    /**
     * Returns the sequence number of the latest matching state update received asynchronously.
     *
     * @param boltState state whose updates are tracked
     * @return matching state update sequence number
     */
    default long getStateUpdateSequence(BoltState boltState) {
        return 0;
    }

    /**
     * Returns whether the child lock needs local API credentials.
     *
     * @return {@code true} if local credentials are required
     */
    default boolean requiresLocalCredentials() {
        return false;
    }
}
