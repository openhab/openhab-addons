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
 * Dto class extending System Parameter Block to support all system parameter values of a WPM3/WPM3i compatible heat
 * pump
 *
 * @author Thomas Burri - Initial contribution
 *
 */
public class SystemParameterBlockAllWpm extends SystemParameterBlock {

    // Note: comfortTemperatureHeatingHc1 covered by comfortTemperatureHeating in base class
    // Note: ecoTemperatureHeatingHc1; covered by ecoTemperatureHeating in base class
    public short heatingCurveRiseHc1;
    public short comfortTemperatureHeatingHc2;
    public short ecoTemperatureHeatingHc2;
    public short heatingCurveRiseHc2;
    public short fixedValueOperation;

    public short dualModeTemperatureHeating; // Bivalenz Temperature Heating

    public int hotwaterStages;
    public short hotwaterDualModeTemperature; // Bivalenz Temperature Warm Water

    public short flowTemperatureAreaCooling;
    public short flowTemperatureHysteresisAreaCooling;
    public short roomTemperatureAreaCooling;

    public short flowTemperatureFanCooling;
    public short flowTemperatureHysteresisFanCooling;
    public short roomTemperatureFanCooling;

    public int reset;
    public int restartIsg;

    @Override
    public String toString() {
        return "System Parameter Block {" + "\n  heatingCurveRiseHc1=" + heatingCurveRiseHc1
                + "\n  comfortTemperatureHeatingHc2=" + comfortTemperatureHeatingHc2 + "\n  ecoTemperatureHeatingHc2="
                + ecoTemperatureHeatingHc2 + "\n  heatingCurveRiseHc2=" + heatingCurveRiseHc2
                + "\n  fixedValueOperation=" + fixedValueOperation + "\n  dualModeTemperatureHeating="
                + dualModeTemperatureHeating + "\n  hotwaterStages=" + hotwaterStages
                + "\n  hotwaterDualModeTemperature=" + hotwaterDualModeTemperature + "\n  flowTemperatureAreaCooling="
                + flowTemperatureAreaCooling + "\n  flowTemperatureHysteresisAreaCooling="
                + flowTemperatureHysteresisAreaCooling + "\n  roomTemperatureAreaCooling=" + roomTemperatureAreaCooling
                + "\n  flowTemperatureFanCooling=" + flowTemperatureFanCooling
                + "\n  flowTemperatureHysteresisFanCooling=" + flowTemperatureHysteresisFanCooling
                + "\n  roomTemperatureFanCooling=" + roomTemperatureFanCooling + "\n  reset=" + reset
                + "\n  restartIsg=" + restartIsg + "\n}";
    }
}
