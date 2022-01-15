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
package org.openhab.binding.robonect.internal.model;

/**
 * Health information from the mower. This information is just included if the robonect module runs the firmware
 * 1.0 beta or higher.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class Health {

    private int temperature;

    private int humidity;

    /**
     * @return - the temperature in °C measured in the mower.
     */
    public int getTemperature() {
        return temperature;
    }

    /**
     * @return - the humidity in % measured in the mower.
     */
    public int getHumidity() {
        return humidity;
    }
}
