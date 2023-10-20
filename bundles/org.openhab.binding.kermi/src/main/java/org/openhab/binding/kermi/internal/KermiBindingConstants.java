/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.kermi.internal;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link KermiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marco Descher - Initial contribution
 */
@NonNullByDefault
public class KermiBindingConstants {

    private static final String BINDING_ID = "kermi";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_DRINKINGWATER_HEATING = new ThingTypeUID(BINDING_ID,
            "drinkingwater-heating");
    public static final ThingTypeUID THING_TYPE_ROOM_HEATING = new ThingTypeUID(BINDING_ID, "room-heating");
    public static final ThingTypeUID THING_TYPE_HEATPUMP = new ThingTypeUID(BINDING_ID, "heatpump");
    public static final ThingTypeUID THING_TYPE_HEATPUMP_MANAGER = new ThingTypeUID(BINDING_ID, "heatpump-manager");

    public static final ChannelTypeUID CHANNEL_TYPE_TEMPERATURE = new ChannelTypeUID(BINDING_ID, "temperature");
    public static final ChannelTypeUID CHANNEL_TYPE_POWER = new ChannelTypeUID(BINDING_ID, "power");
    public static final ChannelTypeUID CHANNEL_TYPE_NUMBER = new ChannelTypeUID(BINDING_ID, "number");
    public static final ChannelTypeUID CHANNEL_TYPE_ONOFF = new ChannelTypeUID(BINDING_ID, "onoff");
    public static final ChannelTypeUID CHANNEL_TYPE_STRING = new ChannelTypeUID(BINDING_ID, "string");

    // Device Constants
    public static final String DEVICE_ID_HEATPUMP_MANAGER = "00000000-0000-0000-0000-000000000000";
    public static final int DEVICE_TYPE_HEATING_SYSTEM = 95;
    public static final int DEVICE_TYPE_HEATPUMP = 97;
    public static final int DEVICE_TYPE_HEATPUMP_MANAGER = 0;

    public static final String WELL_KNOWN_NAME_BS_TWE_TEMP_ACT = "BufferSystem_TweTemperatureActual";
    public static final String WELL_KNOWN_NAME_FS_COOL_TEMP_ACT = "BufferSystem_CoolingTemperatureActual";
    public static final String WELL_KNOWN_NAME_FS_HEAT_TEMP_ACT = "BufferSystem_HeatingTemperatureActual";
    public static final String WELL_KNOWN_NAME_COMB_HEATPUMP_STATE = "Rubin_CombinedHeatpumpState";
    public static final String WELL_KNOWN_NAME_COMB_HEATPUMP_CURR_COP = "Rubin_CurrentCOP";
    public static final String WELL_KNOWN_NAME_CURR_OUT_CAP = "Rubin_CurrentOutputCapacity";
    public static final String WELL_KNOWN_NAME_HEAT_AIR_TEMPERATURE = "LuftTemperatur";

    // All Urls
    public static final String HPM_DEVICE_GETDEVICESBYFILTER_URL = "http://%IP%/api/Device/GetDevicesByFilter/00000000-0000-0000-0000-000000000000";
    public static final String HPM_DEVICE_GETDEVICE_URL = "http://%IP%/api/Device/GetDevice/00000000-0000-0000-0000-000000000000";
    public static final String HPM_DEVICE_GETALLDEVICES_URL = "http://%IP%/api/Device/GetAllDevices/00000000-0000-0000-0000-000000000000";
    public static final String HPM_MENU_GETCHILDENTRIES_URL = "http://%IP%/api/Menu/GetChildEntries/00000000-0000-0000-0000-000000000000";
    public static final String HPM_DATAPOINT_READVALUES_URL = "http://%IP%/api/Datapoint/ReadValues/00000000-0000-0000-0000-000000000000";

    public static String parseUrl(String url, String ip) {
        return url.replace("%IP%", ip.trim());
    }

    public static File getKermiUserDataFolder() {
        File kermiUserDataFolder = new File(OpenHAB.getUserDataFolder(), "binding.kermi");
        kermiUserDataFolder.mkdir();
        return kermiUserDataFolder;
    }
}
