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

import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.ID;
import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.NAME;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.dto.UniFiWlan;

/**
 * The {@link UniFiWlanCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiWlan} instances.
 *
 * The cache uses the following prefixes: <code>id</code>, <code>name</code>
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class UniFiWlanCache extends UniFiCache<UniFiWlan> {

    public UniFiWlanCache() {
        super(ID, NAME);
    }

    @Override
    protected @Nullable String getSuffix(final UniFiWlan wlan, final Prefix prefix) {
        switch (prefix) {
            case ID:
                return wlan.getId();
            case NAME:
                return wlan.getName();
            default:
                return null;
        }
    }
}
