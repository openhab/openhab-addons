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
package org.openhab.binding.broadlinkthermostat.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BroadlinkThermostatConfig} class holds the configuration properties of the thing.
 *
 * @author Florian Mueller - Initial contribution
 */

@NonNullByDefault
public class BroadlinkThermostatConfig {
    private String host;
    private String macAddress;

    public BroadlinkThermostatConfig() {
        this.host = "0.0.0.0";
        this.macAddress = "00:00:00:00";
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
}
