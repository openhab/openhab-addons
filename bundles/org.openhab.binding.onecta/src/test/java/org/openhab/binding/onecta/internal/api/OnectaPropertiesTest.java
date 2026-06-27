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
package org.openhab.binding.onecta.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author Alexander Drent - Initial contribution
 */
public class OnectaPropertiesTest {

    final String UNITID = "1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6";
    final Enums.ManagementPoint MANAGEMENTPOINTTYPE = Enums.ManagementPoint.CLIMATECONTROL;

    @Test
    public void startScanTest() {

        assertEquals("https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6",
                OnectaProperties.getBaseUrl(UNITID));

        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/onOffMode",
                OnectaProperties.getUrlOnOff(UNITID, MANAGEMENTPOINTTYPE));
        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/powerfulMode",
                OnectaProperties.getUrlPowerfulModeOnOff(UNITID, MANAGEMENTPOINTTYPE));
        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/econoMode",
                OnectaProperties.getEconoMode(UNITID, MANAGEMENTPOINTTYPE));
        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/operationMode",
                OnectaProperties.getOperationModeUrl(UNITID, MANAGEMENTPOINTTYPE));

        assertEquals(20f, OnectaProperties.getTargetTemperaturCommand(20f).value);
        assertEquals(null, OnectaProperties.getTargetTemperaturCommand(20f).path);

        assertEquals("dry", OnectaProperties.getOperationModeCommand(Enums.OperationMode.DEHUMIDIFIER).value);
        assertEquals(null, OnectaProperties.getOperationModeCommand(Enums.OperationMode.DEHUMIDIFIER).path);

        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/temperatureControl",
                OnectaProperties.getTemperatureControlUrl(UNITID, MANAGEMENTPOINTTYPE.getValue()));

        assertEquals(20f, OnectaProperties.getTemperatureRoomControlCommand(20f, Enums.OperationMode.COLD).value);
        assertEquals("/operationModes/cooling/setpoints/roomTemperature",
                OnectaProperties.getTemperatureRoomControlCommand(20f, Enums.OperationMode.COLD).path);

        assertEquals(20f, OnectaProperties.getSetpointLeavingWaterOffsetCommand(20f, Enums.OperationMode.COLD).value);
        assertEquals("/operationModes/cooling/setpoints/leavingWaterOffset",
                OnectaProperties.getSetpointLeavingWaterOffsetCommand(20f, Enums.OperationMode.COLD).path);

        assertEquals(20f,
                OnectaProperties.getSetpointLeavingWaterTemperatureCommand(20f, Enums.OperationMode.HEAT).value);
        assertEquals("/operationModes/heating/setpoints/leavingWaterTemperature",
                OnectaProperties.getSetpointLeavingWaterTemperatureCommand(20f, Enums.OperationMode.HEAT).path);

        assertEquals(20f, OnectaProperties.getTemperatureHotWaterControlCommand(20f, Enums.OperationMode.AUTO).value);
        assertEquals("/operationModes/auto/setpoints/domesticHotWaterTemperature",
                OnectaProperties.getTemperatureHotWaterControlCommand(20f, Enums.OperationMode.AUTO).path);

        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/fanControl",
                OnectaProperties.getTFanControlUrl(UNITID, MANAGEMENTPOINTTYPE.getValue()));

        assertEquals("fixed",
                OnectaProperties.getTFanSpeedCurrentCommand(Enums.OperationMode.FAN, Enums.FanSpeed.LEVEL_1).value);
        assertEquals("/operationModes/fanOnly/fanSpeed/currentMode",
                OnectaProperties.getTFanSpeedCurrentCommand(Enums.OperationMode.FAN, Enums.FanSpeed.LEVEL_1).path);

        assertEquals(1,
                OnectaProperties.getTFanSpeedFixedCommand(Enums.OperationMode.FAN, Enums.FanSpeed.LEVEL_1).value);
        assertEquals("/operationModes/fanOnly/fanSpeed/modes/fixed",
                OnectaProperties.getTFanSpeedFixedCommand(Enums.OperationMode.FAN, Enums.FanSpeed.LEVEL_1).path);

        assertEquals("stop", OnectaProperties.getTFanDirectionHorCommand(Enums.OperationMode.FAN,
                Enums.FanMovementHor.STOPPED).value);
        assertEquals("/operationModes/fanOnly/fanDirection/horizontal/currentMode", OnectaProperties
                .getTFanDirectionHorCommand(Enums.OperationMode.FAN, Enums.FanMovementHor.STOPPED).path);

        assertEquals("swing",
                OnectaProperties.getTFanDirectionVerCommand(Enums.OperationMode.FAN, Enums.FanMovementVer.SWING).value);
        assertEquals("/operationModes/fanOnly/fanDirection/vertical/currentMode",
                OnectaProperties.getTFanDirectionVerCommand(Enums.OperationMode.FAN, Enums.FanMovementVer.SWING).path);

        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/streamerMode",
                OnectaProperties.getStreamerMode(UNITID, MANAGEMENTPOINTTYPE.getValue()));

        assertEquals(
                "https://api.onecta.daikineurope.com/v1/gateway-devices/1ce8c13f-5271-4343-ac9f-a1b2c3d4e5f6/management-points/climateControl/characteristics/holidayMode",
                OnectaProperties.getHolidayMode(UNITID, MANAGEMENTPOINTTYPE.getValue()));

        assertEquals("scheduled", OnectaProperties.getTDemandControlCommand(Enums.DemandControl.SCHEDULED).value);
        assertEquals("/currentMode", OnectaProperties.getTDemandControlCommand(Enums.DemandControl.SCHEDULED).path);

        assertEquals(12, OnectaProperties.getTDemandControlFixedValueCommand(12).value);
        assertEquals("/modes/fixed", OnectaProperties.getTDemandControlFixedValueCommand(12).path);
    }
}
