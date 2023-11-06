/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.DESC;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.ID;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;

/**
 * The {@link UniFiSiteCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiSite} instances.
 *
 * The cache uses the following prefixes: <code>id</code>, <code>name</code>, <code>desc</code>
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
class UniFiSiteCache extends UniFiCache<UniFiSite> {

    public UniFiSiteCache() {
        super(ID, NAME, DESC);
    }

    @Override
    protected @Nullable String getSuffix(final UniFiSite site, final Prefix prefix) {
        switch (prefix) {
            case ID:
                return site.getId();
            case NAME:
                return site.getName();
            case DESC:
                return site.getDescription();
            default:
                return null;
        }
    }
}
