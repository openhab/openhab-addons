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
 * Dto class for the Runtime Block of any WPM/WPM3/WPM3i compatible heat pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class EnergyRuntimeBlockAllWpm extends EnergyBlock {

    // Runtime common
    public int runtimeCompressorHeating;
    public int runtimeCompressorHotwater;
    public int runtimeCompressorCooling;
    public int runtimeNhz1;
    public int runtimeNhz2;
    public int runtimeNhz12;

    // Production
    public int hp1ProductionHeatToday;
    public int hp1ProductionHeatTotalLow;
    public int hp1ProductionHeatTotalHigh;
    public int hp1ProductionWaterToday;
    public int hp1ProductionWaterTotalLow;
    public int hp1ProductionWaterTotalHigh;
    public int hp1ProductionNhzHeatingTotalLow;
    public int hp1ProductionNhzHeatingTotalHigh;
    public int hp1ProductionNhzHotwaterTotalLow;
    public int hp1ProductionNhzHotwaterTotalHigh;

    // Consumption
    public int hp1ConsumptionHeatToday;
    public int hp1ConsumptionHeatTotalLow;
    public int hp1ConsumptionHeatTotalHigh;
    public int hp1ConsumptionWaterToday;
    public int hp1ConsumptionWaterTotalLow;
    public int hp1ConsumptionWaterTotalHigh;

    // Runtime WPMsystem/WPM3
    public int hp1RuntimeCompressor1Heating;
    public int hp1RuntimeCompressor2Heating;
    public int hp1RuntimeCompressor12Heating;
    public int hp1RuntimeCompressor1Hotwater;
    public int hp1RuntimeCompressor2Hotwater;
    public int hp1RuntimeCompressor12Hotwater;
    public int hp1RuntimeCompressorCooling;
}
