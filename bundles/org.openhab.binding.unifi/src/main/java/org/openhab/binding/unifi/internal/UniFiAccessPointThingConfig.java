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

/**
 * The {@link UniFiAccessPointThingConfig} encapsulates all the configuration options for an instance of the
 * {@link org.openhab.binding.unifi.internal.handler.UniFiAccessPointThingHandler}.
 *
 * @author Thomas Lauterbach - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("unused")
public class UniFiAccessPointThingConfig {

    private String macAddress = "";

    private String site = "";

    public String getSite() {
        return site;
    }

    private void setSite(final String site) {
        // method to avoid ide auto format mark the field as final
        this.site = site;
    }

    public String getMacAddress() {
        return macAddress;
    }

    private void setMacAddress(final String macAddress) {
        // method to avoid ide auto format mark the field as final
        this.macAddress = macAddress;
    }

    public boolean isValid() {
        return !macAddress.isBlank();
    }
}
