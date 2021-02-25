/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FroniusBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Thomas Rokohl - Initial contribution
 * @author Peter Schraffl - Added device status and error status channels
 */
@NonNullByDefault
public class FroniusBindingConstants {

    private static final String BINDING_ID = "fronius";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INVERTER = new ThingTypeUID(BINDING_ID, "powerinverter");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter");

    // List of all Channel ids
    public static final String InverterDataChannelDayEnergy = "inverterdatachanneldayenergy";
    public static final String InverterDataChannelPac = "inverterdatachannelpac";
    public static final String InverterDataChannelTotal = "inverterdatachanneltotal";
    public static final String InverterDataChannelYear = "inverterdatachannelyear";
    public static final String InverterDataChannelFac = "inverterdatachannelfac";
    public static final String InverterDataChannelIac = "inverterdatachanneliac";
    public static final String InverterDataChannelIdc = "inverterdatachannelidc";
    public static final String InverterDataChannelUac = "inverterdatachanneluac";
    public static final String InverterDataChannelUdc = "inverterdatachanneludc";
    public static final String InverterDataChannelDeviceStatusErrorCode = "inverterdatadevicestatuserrorcode";
    public static final String InverterDataChannelDeviceStatusStatusCode = "inverterdatadevicestatusstatuscode";
    public static final String PowerFlowpGrid = "powerflowchannelpgrid";
    public static final String PowerFlowpLoad = "powerflowchannelpload";
    public static final String PowerFlowpAkku = "powerflowchannelpakku";
    public static final String MeterModel = "model";
    public static final String MeterSerial = "serial";
    public static final String MeterEnable = "enable";
    public static final String MeterLocation = "location";
    public static final String MeterCurrentAcPhase1 = "currentacphase1";
    public static final String MeterCurrentAcPhase2 = "currentacphase2";
    public static final String MeterCurrentAcPhase3 = "currentacphase3";
    public static final String MeterVoltageAcPhase1 = "voltageacphase1";
    public static final String MeterVoltageAcPhase2 = "voltageacphase2";
    public static final String MeterVoltageAcPhase3 = "voltageacphase3";
    public static final String MeterPowerPhase1 = "powerrealphase1";
    public static final String MeterPowerPhase2 = "powerrealphase2";
    public static final String MeterPowerPhase3 = "powerrealphase3";
    public static final String MeterPowerFactorPhase1 = "powerfactorphase1";
    public static final String MeterPowerFactorPhase2 = "powerfactorphase2";
    public static final String MeterPowerFactorPhase3 = "powerfactorphase3";
    public static final String MeterEnergyRealSumConsumed = "energyrealsumconsumed";
    public static final String MeterEnergyRealSumProduced = "energyrealsumproduced";

    // List of all Urls
    public static final String INVERTER_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetInverterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=CommonInverterData";
    public static final String POWERFLOW_REALTIME_DATA = "http://%IP%/solar_api/v1/GetPowerFlowRealtimeData.fcgi";
    public static final String METER_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetMeterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=MeterRealtimeData";
}
