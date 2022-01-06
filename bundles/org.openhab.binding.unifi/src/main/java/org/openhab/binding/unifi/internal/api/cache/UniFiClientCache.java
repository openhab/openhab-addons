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

import org.openhab.binding.unifi.internal.api.model.UniFiClient;

/**
 * The {@link UniFiClientCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiClient} instances.
 *
 * The cache uses the following prefixes: <code>mac</code>, <code>ip</code>, <code>hostname</code>, and
 * <code>alias</code>
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiClientCache extends UniFiCache<UniFiClient> {

    public UniFiClientCache() {
        super(PREFIX_ID, PREFIX_MAC, PREFIX_IP, PREFIX_HOSTNAME, PREFIX_ALIAS);
    }

    @Override
    protected String getSuffix(UniFiClient client, String prefix) {
        switch (prefix) {
            case PREFIX_ID:
                return client.getId();
            case PREFIX_MAC:
                return client.getMac();
            case PREFIX_IP:
                return client.getIp();
            case PREFIX_HOSTNAME:
                return client.getHostname();
            case PREFIX_ALIAS:
                return client.getAlias();
        }
        return null;
    }
}
