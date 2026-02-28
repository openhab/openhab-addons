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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onecta.internal.api.dto.commands.CommandFloat;
import org.openhab.binding.onecta.internal.api.dto.commands.CommandInteger;
import org.openhab.binding.onecta.internal.api.dto.commands.CommandString;

/**
 * The {@link OnectaProperties} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Drent - Initial contribution
 */
@NonNullByDefault
public class OnectaProperties {

    private static final String BASE_URL = "https://api.onecta.daikineurope.com/v1/gateway-devices/%s";
    private static final String BASE_URL_COMMAND = "/management-points/%s/characteristics/%s";
    private static final String COMMAND_ONOFFMODE = "onOffMode";
    private static final String COMMAND_POWERFULMODE = "powerfulMode";
    private static final String COMMAND_ECONOMODE = "econoMode";
    private static final String COMMAND_OPERATIONMODE = "operationMode";
    private static final String COMMAND_TEMPERATURECONTROL = "temperatureControl";
    private static final String COMMAND_STREAMERMODE = "streamerMode";
    private static final String COMMAND_HOLIDAYMODE = "holidayMode";
    private static final String COMMAND_SUBPATH_TEMPERATURECONTROL_ROOM = "/operationModes/%s/setpoints/roomTemperature";
    private static final String COMMAND_SUBPATH_TEMPERATURECONTROL_HOTWATERTANK = "/operationModes/%s/setpoints/domesticHotWaterTemperature";
    private static final String COMMAND_SUBPATH_TEMPERATURECONTROL_LEAVINGWATEROFFSET = "/operationModes/%s/setpoints/leavingWaterOffset";
    private static final String COMMAND_SUBPATH_TEMPERATURECONTROL_LEAVINGWATERTEMP = "/operationModes/%s/setpoints/leavingWaterTemperature";
    private static final String COMMAND_FANSPEED_CONTROL = "fanControl";
    private static final String COMMAND_SUBPATH_FANSPEED = "/operationModes/%s/fanSpeed/currentMode";
    private static final String COMMAND_SUBPATH_FANSPEED_FIXED = "/operationModes/%s/fanSpeed/modes/fixed";
    private static final String COMMAND_SUBPATH_FANDITECTION_HOR = "/operationModes/%s/fanDirection/horizontal/currentMode";
    private static final String COMMAND_SUBPATH_FANDITECTION_VER = "/operationModes/%s/fanDirection/vertical/currentMode";
    private static final String COMMAND_SUBPATH_DEMAND_CONTROL = "/currentMode";
    private static final String COMMAND_SUBPATH_DEMAND_CONTROL_FIXED_VALUE = "/modes/fixed";

    public static String getBaseUrl(String unitId) {
        return StringUtils.removeEnd(String.format(BASE_URL, unitId), "/");
    }

    public static String getUrlOnOff(String unitId, Enums.ManagementPoint managementPointType) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, managementPointType.getValue(), COMMAND_ONOFFMODE);
    }

    public static String getUrlPowerfulModeOnOff(String unitId, Enums.ManagementPoint managementPointType) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, managementPointType.getValue(),
                COMMAND_POWERFULMODE);
    }

    public static String getEconoMode(String unitId, Enums.ManagementPoint managementPointType) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, managementPointType.getValue(), COMMAND_ECONOMODE);
    }

    public static String getOperationModeUrl(String unitId, Enums.ManagementPoint managementPointType) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, managementPointType.getValue(),
                COMMAND_OPERATIONMODE);
    }

    public static CommandFloat getTargetTemperaturCommand(float value) {
        return new CommandFloat(value);
    }

    public static CommandString getOperationModeCommand(Enums.OperationMode operationMode) {
        return new CommandString(operationMode.getValue());
    }

    public static String getTemperatureControlUrl(String unitId, String embeddedId) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, embeddedId, COMMAND_TEMPERATURECONTROL);
    }

    public static CommandFloat getTemperatureRoomControlCommand(float value, Enums.OperationMode currentMode) {
        return new CommandFloat(value, String.format(COMMAND_SUBPATH_TEMPERATURECONTROL_ROOM, currentMode.getValue()));
    }

    public static CommandFloat getSetpointLeavingWaterOffsetCommand(float value, Enums.OperationMode currentMode) {
        return new CommandFloat(value,
                String.format(COMMAND_SUBPATH_TEMPERATURECONTROL_LEAVINGWATEROFFSET, currentMode.getValue()));
    }

    public static CommandFloat getSetpointLeavingWaterTemperatureCommand(float value, Enums.OperationMode currentMode) {
        return new CommandFloat(value,
                String.format(COMMAND_SUBPATH_TEMPERATURECONTROL_LEAVINGWATERTEMP, currentMode.getValue()));
    }

    public static CommandFloat getTemperatureHotWaterControlCommand(float value, Enums.OperationMode currentMode) {
        return new CommandFloat(value,
                String.format(COMMAND_SUBPATH_TEMPERATURECONTROL_HOTWATERTANK, currentMode.getValue()));
    }

    public static String getTFanControlUrl(String unitId, String embeddedId) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, embeddedId, COMMAND_FANSPEED_CONTROL);
    }

    public static CommandString getTFanSpeedCurrentCommand(Enums.OperationMode currentMode, Enums.FanSpeed fanspeed) {
        return new CommandString(fanspeed.getValueMode(),
                String.format(COMMAND_SUBPATH_FANSPEED, currentMode.getValue()));
    }

    public static CommandInteger getTFanSpeedFixedCommand(Enums.OperationMode currentMode, Enums.FanSpeed fanspeed) {
        return new CommandInteger(fanspeed.getValueSpeed(),
                String.format(COMMAND_SUBPATH_FANSPEED_FIXED, currentMode.getValue()));
    }

    public static CommandString getTFanDirectionHorCommand(Enums.OperationMode currentMode,
            Enums.FanMovementHor fanMovement) {
        return new CommandString(fanMovement.getValue(),
                String.format(COMMAND_SUBPATH_FANDITECTION_HOR, currentMode.getValue()));
    }

    public static CommandString getTFanDirectionVerCommand(Enums.OperationMode currentMode,
            Enums.FanMovementVer fanMovement) {
        return new CommandString(fanMovement.getValue(),
                String.format(COMMAND_SUBPATH_FANDITECTION_VER, currentMode.getValue()));
    }

    public static String getStreamerMode(String unitId, String embeddedId) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, embeddedId, COMMAND_STREAMERMODE);
    }

    public static String getHolidayMode(String unitId, String embeddedId) {
        return String.format(getBaseUrl(unitId) + BASE_URL_COMMAND, embeddedId, COMMAND_HOLIDAYMODE);
    }

    public static CommandString getTDemandControlCommand(Enums.DemandControl value) {
        return new CommandString(value.getValue(), String.format(COMMAND_SUBPATH_DEMAND_CONTROL, value.getValue()));
    }

    public static CommandInteger getTDemandControlFixedValueCommand(Integer value) {
        return new CommandInteger(value, COMMAND_SUBPATH_DEMAND_CONTROL_FIXED_VALUE);
    }
}
