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
package org.openhab.binding.satel.internal.event;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event class describing current temperature in a zone.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class ZoneTemperatureEvent implements SatelEvent {

    private int zoneNbr;
    private float temperature;

    /**
     * Constructs new event class.
     *
     * @param zoneNbr zone number
     * @param temperature current temperature in the zone
     */
    public ZoneTemperatureEvent(int zoneNbr, float temperature) {
        this.zoneNbr = zoneNbr;
        this.temperature = temperature;
    }

    /**
     * @return zone number
     */
    public int getZoneNbr() {
        return zoneNbr;
    }

    /**
     * @return temperature in the zone
     */
    public float getTemperature() {
        return temperature;
    }

    @Override
    public String toString() {
        return String.format("ZoneTemperatureEvent: zoneNbr = %d, temperature = %.1f", this.zoneNbr, this.temperature);
    }
}
