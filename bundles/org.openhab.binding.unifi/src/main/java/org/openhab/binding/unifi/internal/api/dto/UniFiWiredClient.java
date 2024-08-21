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
package org.openhab.binding.unifi.internal.api.dto;

import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;

/**
 * A {@link UniFiWiredClient} represents a wired {@link UniFiClient}.
 *
 * A wired client is physically connected to the network - typically it is connected via an Ethernet cable.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiWiredClient extends UniFiClient {

    private String swMac;

    public UniFiWiredClient(final UniFiControllerCache cache) {
        super(cache);
    }

    @Override
    public boolean isWired() {
        return true;
    }

    @Override
    public String getDeviceMac() {
        return swMac;
    }
}
