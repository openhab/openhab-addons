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
package org.openhab.binding.solarman.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link SolarmanLoggerConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanLoggerConfiguration {

    /**
     * Solarman Logger Thing Configuration Parameters
     */
    public String hostname = "";
    public Integer port = 8899;
    public String serialNumber = "";
    public String inverterType = "sg04lp3";
    public int refreshInterval = 30;
    public String solarmanLoggerMode = SolarmanLoggerMode.V5MODBUS.toString();
    @Nullable
    public String additionalRequests;

    public SolarmanLoggerConfiguration() {
    }

    public SolarmanLoggerConfiguration(String hostname, Integer port, String serialNumber, String inverterType,
            int refreshInterval, String solarmanLoggerMode, @Nullable String additionalRequests) {
        this.hostname = hostname;
        this.port = port;
        this.serialNumber = serialNumber;
        this.inverterType = inverterType;
        this.refreshInterval = refreshInterval;
        this.solarmanLoggerMode = solarmanLoggerMode;
        this.additionalRequests = additionalRequests;
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPort() {
        return port;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getInverterType() {
        return inverterType;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }

    public SolarmanLoggerMode getSolarmanLoggerMode() {
        return SolarmanLoggerMode.valueOf(solarmanLoggerMode);
    }

    @Nullable
    public String getAdditionalRequests() {
        return additionalRequests;
    }
}
