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

import static org.openhab.binding.unifi.internal.api.cache.UniFiCache.Prefix.MAC;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifi.internal.api.dto.UniFiDevice;

/**
 * The {@link UniFiDeviceCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiDevice} instances.
 *
 * The cache uses the following prefixes: <code>mac</code>
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
class UniFiDeviceCache extends UniFiCache<UniFiDevice> {

    public UniFiDeviceCache() {
        super(MAC);
    }

    @Override
    protected @Nullable String getSuffix(final UniFiDevice device, final Prefix prefix) {
        switch (prefix) {
            case MAC:
                return device.getMac();
        }
        return null;
    }
}
