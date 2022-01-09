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

import org.openhab.binding.unifi.internal.api.model.UniFiDevice;

/**
 * The {@link UniFiDeviceCache} is a specific implementation of {@link UniFiCache} for the purpose of caching
 * {@link UniFiDevice} instances.
 *
 * The cache uses the following prefixes: <code>mac</code>
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiDeviceCache extends UniFiCache<UniFiDevice> {

    public UniFiDeviceCache() {
        super(PREFIX_MAC);
    }

    @Override
    protected String getSuffix(UniFiDevice device, String prefix) {
        switch (prefix) {
            case PREFIX_MAC:
                return device.getMac();
        }
        return null;
    }
}
