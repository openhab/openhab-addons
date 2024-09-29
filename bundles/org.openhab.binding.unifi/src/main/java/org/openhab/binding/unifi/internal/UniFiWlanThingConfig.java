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
package org.openhab.binding.unifi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifi.internal.handler.UniFiWlanThingHandler;

/**
 * The {@link UniFiWlanThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiWlanThingHandler}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class UniFiWlanThingConfig {

    private String wid = "";

    public String getWlanId() {
        return wid;
    }

    private void setWlanId(final String wid) {
        // method to avoid auto format mark the field as final
        this.wid = wid;
    }

    public boolean isValid() {
        return !wid.isBlank();
    }

    @Override
    public String toString() {
        return String.format("UniFiWlanThingConfig{wid: '%s'}", wid);
    }
}
