/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ambientweather.internal.config;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link StationConfig} is responsible for storing the
 * Ambient Weather weather station thing configuration.
 *
 * @author Mark Hilbush - Initial contribution
 */
public class StationConfig {
    /**
     * MAC address of the device
     */
    private String macAddress;

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public boolean isValid() {
        if (StringUtils.isBlank(macAddress)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AmbientWeatherStationConfig{ macAddress=" + macAddress + " }";
    }
}
