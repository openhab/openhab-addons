/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solarview.internal;

import org.openhab.binding.solarview.handler.SolarviewBridgeHandler;

/**
 * Meter types supported by the <B>Solarview</B> bridge.
 * It provides informations how to send query through the {@link SolarviewBridgeHandler}.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
public class Device {

    // Request message strings
    private static final String SOLARVIEW_REQUEST_EOL = "\r\n";

    /**
     * Enumeration of <B>Solarview</B> device identification which is normally accessed via {@link getQueryString}.
     */
    public static enum SolarviewDeviceType {
        METER_PRODUCTION(0),
        METER_INVERTER_ONE(1),
        METER_INVERTER_TWO(2),
        METER_INVERTER_THREE(3),
        METER_INVERTER_FOUR(4),
        METER_INVERTER_FIVE(5),
        METER_INVERTER_SIX(6),
        METER_INVERTER_SEVEN(7),
        METER_INVERTER_EIGHT(8),
        METER_INVERTER_NINE(9),
        METER_INJECTION(21),
        METER_IMPORT(22);

        private int deviceType;

        SolarviewDeviceType(int thisDeviceType) {
            this.deviceType = thisDeviceType;
        }

        /**
         * Return a device-type specific <B>queryString</B> to be used for method
         * {@link org.openhab.binding.solarview.handler.SolarviewBridgeHandler#updateEnergyDataFromServer},
         * which returns (for any kind of <B>Solarview</B> device) the set of results in
         * a common information structure of type {@link Energy}.
         *
         * @return queryString of type {@link String}.
         */
        public String getQueryString() {
            return (String.format("%02d*%s", this.deviceType, SOLARVIEW_REQUEST_EOL));
        }

    }

    private Device() {
    }

}
/**
 * end-of-Device.java
 */
