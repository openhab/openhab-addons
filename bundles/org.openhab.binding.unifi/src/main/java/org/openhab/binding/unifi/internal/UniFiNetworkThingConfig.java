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
package org.openhab.binding.unifi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.unifi.internal.handler.UniFiNetworkThingHandler;

/**
 * The {@link UniFiNetworkThingConfig} encapsulates all the configuration options for an instance of the
 * {@link UniFiNetworkThingHandler}.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class UniFiNetworkThingConfig {

    private String nid = "";

    public String getNetworkId() {
        return nid;
    }

    private void setNetworkId(final String nid) {
        // method to avoid auto format mark the field as final
        this.nid = nid;
    }

    public boolean isValid() {
        return !nid.isBlank();
    }

    @Override
    public String toString() {
        return String.format("UniFiNetworkThingConfig{nid: '%s'}", nid);
    }
}
