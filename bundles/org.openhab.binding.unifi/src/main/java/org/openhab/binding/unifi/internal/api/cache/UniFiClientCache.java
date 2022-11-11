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
package org.openhab.binding.unifi.internal.api.cache;

import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.ALIAS;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.HOSTNAME;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.ID;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.IP;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.MAC;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;

/**
 * The {@link UniFiClientCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiClient} instances.
 *
 * The cache uses the following prefixes: <code>mac</code>, <code>ip</code>, <code>hostname</code>, and
 * <code>alias</code>
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
class UniFiClientCache extends UniFiCache<UniFiClient> {

    public UniFiClientCache() {
        super(ID, MAC, IP, HOSTNAME, ALIAS);
    }

    @Override
    protected @Nullable String getSuffix(final UniFiClient client, final Prefix prefix) {
        switch (prefix) {
            case ID:
                return client.getId();
            case MAC:
                return client.getMac();
            case IP:
                return client.getIp();
            case HOSTNAME:
                return client.getHostname();
            case ALIAS:
                return client.getAlias();
            default:
                return null;
        }
    }
}
