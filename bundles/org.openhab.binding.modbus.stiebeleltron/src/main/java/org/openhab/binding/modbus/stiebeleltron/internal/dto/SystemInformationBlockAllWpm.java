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
package org.openhab.binding.modbus.stiebeleltron.internal.dto;

/**
 * Dto class extending System Information Block to support all system information values of any WPM/WPM3/WPM3i
 * compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SystemInformationBlockAllWpm extends SystemInformationBlock {

    public short temperatureFe7;
    public short temperatureFe7SetPoint;

    public short temperatureHc2;
    public short temperatureHc2SetPoint;

    public short temperatureFlowHp;
    public short temperatureFlowNhz;
    public short temperatureFlow;

    public short temperatureFixedSetPoint;

    public short temperatureBuffer;
    public short temperatureBufferSetPoint;

    public short pressureHeating;
    public short flowRate;

    public short temperatureFanCooling;
    public short temperatureFanCoolingSetPoint;
    public short temperatureAreaCooling;
    public short temperatureAreaCoolingSetPoint;

    // The following three values are only available for WPM3
    public short temperatureCollectorSolar;
    public short temperatureCylinderSolar;
    public int runtimeSolar;

    // The following three values are only available for WPMsystem and WPM3
    public short temperatureExtHeatSource;
    public short temperatureExtHeatSourceSetPoint;
    public int runtimeExtHeatSource;

    public short lowerHeatingLimit;
    public short lowerWaterLimit;

    public short temperatureSourceMin;
    public short pressureSource;

    // The following three values are only available for WPM3i
    public short temperatureHotgas;
    public short pressureHigh;
    public short pressureLow;

    // heat pump 1 to ... information - only first one for now
    public short hp1TemperatureReturn;
    public short hp1TemperatureFlow;
    public short hp1TemperatureHotgas;
    public short hp1PressureLow;
    public short hp1PressureMean;
    public short hp1PressureHigh;
    public short hp1FlowRate;
}
