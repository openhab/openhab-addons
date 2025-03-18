/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.cache;

import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.ID;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.dto.UniFiNetwork;

/**
 * The {@link UniFiNetworkCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiNetwork} instances.
 *
 * The cache uses the following prefixes: <code>id</code>, <code>name</code>
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
class UniFiNetworkCache extends UniFiCache<UniFiNetwork> {

    public UniFiNetworkCache() {
        super(ID, NAME);
    }

    @Override
    protected @Nullable String getSuffix(final UniFiNetwork network, final Prefix prefix) {
        switch (prefix) {
            case ID:
                return network.getId();
            case NAME:
                return network.getName();
            default:
                return null;
        }
    }
}
