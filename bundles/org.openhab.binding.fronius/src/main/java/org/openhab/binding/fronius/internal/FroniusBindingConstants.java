/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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

    // List of all Urls
    public static final String INVERTER_REALTIME_DATA_URL = "http://%IP%/solar_api/v1/GetInverterRealtimeData.cgi?Scope=Device&DeviceId=%DEVICEID%&DataCollection=CommonInverterData";
    public static final String POWERFLOW_REALTIME_DATA = "http://%IP%/solar_api/v1/GetPowerFlowRealtimeData.fcgi";

}
