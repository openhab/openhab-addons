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
 * A {@link UniFiUnknownClient} represents an unknown {@link UniFiClient}.
 *
 * An unknown client is neither a {@link UniFiWiredClient} nor a {@link UniFiWirelessClient}
 * because the <code>is_wired</code> property was missing from the JSON response of the controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiUnknownClient extends UniFiClient {

    public UniFiUnknownClient(final UniFiControllerCache cache) {
        super(cache);
    }

    @Override
    public boolean isWired() {
        return false; // mgb: no is_wired property in the JSON
    }

    @Override
    public String getDeviceMac() {
        return null; // mgb: no device mac in the JSON
    }
}
