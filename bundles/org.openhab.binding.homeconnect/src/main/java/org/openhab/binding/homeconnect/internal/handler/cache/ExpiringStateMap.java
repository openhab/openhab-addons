/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.handler.SupplierWithException;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This is a simple expiring state cache implementation. The state value expires after the
 * specified duration has passed since the item was created.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ExpiringStateMap {
    private final Duration expiry;
    private final ConcurrentMap<ChannelUID, ExpiringStateCache> items;

    /**
     * Expiring state map.
     *
     * @param expiry expiry duration
     */
    public ExpiringStateMap(Duration expiry) {
        this.expiry = expiry;
        this.items = new ConcurrentHashMap<>();
    }

    /**
     * Get cached value or retrieve new state value via supplier.
     *
     * @param channelUID cache key / channel uid
     * @param supplier supplier
     * @return current state
     * @throws CommunicationException API communication exception
     * @throws AuthorizationException oAuth authorization exception
     * @throws ApplianceOfflineException appliance is not connected to the cloud
     */
    public State putIfAbsentAndGet(ChannelUID channelUID, SupplierWithException<State> supplier)
            throws AuthorizationException, ApplianceOfflineException, CommunicationException {
        items.putIfAbsent(channelUID, new ExpiringStateCache(expiry, supplier));

        final ExpiringStateCache item = items.get(channelUID);
        if (item == null) {
            return UnDefType.UNDEF;
        } else {
            return item.getState();
        }
    }
}
