/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class for the System Information Block
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Renamed some members
 *
 */
public class SystemInformationBlock {

    public short temperatureFek;
    public short temperatureFekSetPoint;
    public short humidityFek;
    public short dewpointFek;
    public short temperatureOutdoor;
    public short temperatureHc1;
    public short temperatureHc1SetPoint;
    public short temperatureSupply;
    public short temperatureReturn;
    public short temperatureSource;
    public short temperatureWater;
    public short temperatureWaterSetPoint;

    @Override
    public String toString() {
        return "SystemInformationBlock {\n" + "  temperatureFek=" + temperatureFek + ",\n  temperatureFekSetPoint="
                + temperatureFekSetPoint + ",\n  humidityFek=" + humidityFek + ",\n  dewpointFek=" + dewpointFek
                + ",\n  temperatureOutdoor=" + temperatureOutdoor + ",\n  temperatureHc1=" + temperatureHc1
                + ",\n  temperatureHc1SetPoint=" + temperatureHc1SetPoint + ",\n  temperatureSupply="
                + temperatureSupply + ",\n  temperatureReturn=" + temperatureReturn + ",\n  temperatureSource="
                + temperatureSource + ",\n  temperatureWater=" + temperatureWater + ",\n  temperatureWaterSetPoint="
                + temperatureWaterSetPoint + "}";
    }
}
