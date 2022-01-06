/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.handler.cache;

import java.lang.ref.SoftReference;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.handler.SupplierWithException;
import org.openhab.core.types.State;

/**
 *
 * Expiring state model. Holds a state and the corresponding expiration time.
 *
 * @author Jonas Brüstel - Initial Contribution
 */
@NonNullByDefault
public class ExpiringStateCache {

    private final SupplierWithException<State> stateSupplier;
    private final long expiry;

    private SoftReference<@Nullable State> state = new SoftReference<>(null);
    private long expiresAt;

    /**
     * Create a new instance.
     *
     * @param expiry the duration for how long the state should be cached
     * @param stateSupplier supplier to get current state
     */
    public ExpiringStateCache(Duration expiry, SupplierWithException<State> stateSupplier) {
        this.stateSupplier = stateSupplier;
        this.expiry = expiry.toNanos();
    }

    /**
     * Returns the cached or newly fetched state.
     *
     * @return State
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public synchronized State getState()
            throws AuthorizationException, ApplianceOfflineException, CommunicationException {
        State cachedValue = state.get();
        if (cachedValue == null || isExpired()) {
            return refreshState();
        }
        return cachedValue;
    }

    private State refreshState() throws AuthorizationException, ApplianceOfflineException, CommunicationException {
        State freshValue = stateSupplier.get();
        state = new SoftReference<>(freshValue);
        expiresAt = calcExpiresAt();
        return freshValue;
    }

    private boolean isExpired() {
        return expiresAt < System.nanoTime();
    }

    private long calcExpiresAt() {
        return System.nanoTime() + expiry;
    }
}
