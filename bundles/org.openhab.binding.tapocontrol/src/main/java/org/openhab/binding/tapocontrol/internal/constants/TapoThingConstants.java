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
package org.openhab.binding.tapocontrol.internal.constants;

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TapoBindingSettings} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 ***/
@NonNullByDefault
public class TapoThingConstants {
    public static final String DEVICE_VENDOR = "Tapo";

    /*** LIST OF SUPPORTED DEVICE NAMES ***/
    public static final String DEVICE_BRIDGE = "bridge";
    public static final String DEVICE_P100 = "P100";
    public static final String DEVICE_P105 = "P105";
    public static final String DEVICE_P110 = "P110";
    public static final String DEVICE_P115 = "P115";
    public static final String DEVICE_L510 = "L510";
    public static final String DEVICE_L530 = "L530";
    public static final String DEVICE_L610 = "L610";
    public static final String DEVICE_L630 = "L630";
    public static final String DEVICE_L900 = "L900";
    public static final String DEVICE_L920 = "L920";
    public static final String DEVICE_L930 = "L930";
    public static final String DEVICE_UNIVERSAL = "Test_Device";

    /*** LIST OF SUPPORTED DEVICE DESCRIPTIONS ***/
    public static final String DEVICE_DESCRIPTION_BRIDGE = "TapoControl Cloud-Login";
    public static final String DEVICE_DESCRIPTION_SMART_PLUG = "SmartPlug";
    public static final String DEVICE_DESCRIPTION_WHITE_BULB = "White-Light-Bulb";
    public static final String DEVICE_DESCRIPTION_COLOR_BULB = "Color-Light-Bulb";
    public static final String DEVICE_DESCRIPTION_LIGHTSTRIP = "LightStrip";

    /*** LIST OF SUPPORTED THING UIDS ***/
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_BRIDGE);
    public static final ThingTypeUID P100_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P100);
    public static final ThingTypeUID P105_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P105);
    public static final ThingTypeUID P110_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P110);
    public static final ThingTypeUID P115_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P115);
    public static final ThingTypeUID L510_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L510);
    public static final ThingTypeUID L530_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L530);
    public static final ThingTypeUID L610_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L610);
    public static final ThingTypeUID L630_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L630);
    public static final ThingTypeUID L900_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L900);
    public static final ThingTypeUID L920_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L920);
    public static final ThingTypeUID L930_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L930);
    public static final ThingTypeUID UNIVERSAL_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_UNIVERSAL);

    /*** SET OF SUPPORTED UIDS ***/
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_UIDS = Set.of(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_SMART_PLUG_UIDS = Set.of(P100_THING_TYPE, P105_THING_TYPE,
            P110_THING_TYPE, P115_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_WHITE_BULB_UIDS = Set.of(L510_THING_TYPE, L610_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_COLOR_BULB_UIDS = Set.of(L530_THING_TYPE, L630_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_LIGHT_STRIP_UIDS = Set.of(L900_THING_TYPE, L920_THING_TYPE,
            L930_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream
                    .of(SUPPORTED_BRIDGE_UIDS, SUPPORTED_SMART_PLUG_UIDS, SUPPORTED_WHITE_BULB_UIDS,
                            SUPPORTED_COLOR_BULB_UIDS, SUPPORTED_LIGHT_STRIP_UIDS)
                    .flatMap(Set::stream).collect(Collectors.toSet()));
    /*** THINGS WITH ENERGY DATA ***/
    public static final Set<ThingTypeUID> SUPPORTED_ENERGY_DATA_UIDS = Set.of(P110_THING_TYPE, P115_THING_TYPE);

    /*** THINGS WITH CHANNEL GROUPS ***/
    public static final Set<ThingTypeUID> CHANNEL_GROUP_THING_SET = Collections
            .unmodifiableSet(Stream
                    .of(SUPPORTED_BRIDGE_UIDS, SUPPORTED_SMART_PLUG_UIDS, SUPPORTED_WHITE_BULB_UIDS,
                            SUPPORTED_COLOR_BULB_UIDS, SUPPORTED_LIGHT_STRIP_UIDS)
                    .flatMap(Set::stream).collect(Collectors.toSet()));

    /*** DEVICE PROPERTY STRINGS (CLOUD) ***/
    public static final String CLOUD_PROPERTY_ALIAS = "alias";
    public static final String CLOUD_PROPERTY_FW = "fwVer";
    public static final String CLOUD_PROPERTY_HW = "deviceHwVer";
    public static final String CLOUD_PROPERTY_ID = "deviceId";
    public static final String CLOUD_PROPERTY_MAC = "deviceMac";
    public static final String CLOUD_PROPERTY_MODEL = "deviceName"; // use name cause modell returns different values
    public static final String CLOUD_PROPERTY_NAME = "deviceName";
    public static final String CLOUD_PROPERTY_REGION = "deviceRegion";
    public static final String CLOUD_PROPERTY_SERVER_URL = "appServerUrl";
    public static final String CLOUD_PROPERTY_TYPE = "deviceType";

    /*** DEVICE PROPERTY STRINGS (DEVICE) ***/
    public static final String DEVICE_PROPERTY_BRIGHTNES = "brightness";
    public static final String DEVICE_PROPERTY_COLORTEMP = "color_temp";
    public static final String DEVICE_PROPERTY_FW = "fw_ver";
    public static final String DEVICE_PROPERTY_HUE = "hue";
    public static final String DEVICE_PROPERTY_HW = "hw_ver";
    public static final String DEVICE_PROPERTY_ID = "device_id";
    public static final String DEVICE_PROPERTY_IP = "ip";
    public static final String DEVICE_PROPERTY_MAC = "mac";
    public static final String DEVICE_PROPERTY_MODEL = "model";
    public static final String DEVICE_PROPERTY_NICKNAME = "nickname";
    public static final String DEVICE_PROPERTY_ON = "device_on";
    public static final String DEVICE_PROPERTY_ONTIME = "on_time";
    public static final String DEVICE_PROPERTY_OVERHEAT = "overheated";
    public static final String DEVICE_PROPERTY_REGION = "region";
    public static final String DEVICE_PROPERTY_SATURATION = "saturation";
    public static final String DEVICE_PROPERTY_SIGNAL = "signal_level";
    public static final String DEVICE_PROPERTY_SIGNAL_RSSI = "rssi";
    public static final String DEVICE_PROPERTY_TYPE = "type";
    public static final String DEVICE_PROPERTY_USAGE_7 = "time_usage_past7";
    public static final String DEVICE_PROPERTY_USAGE_30 = "time_usage_past30";
    public static final String DEVICE_PROPERTY_USAGE_TODAY = "time_usage_today";
    public static final String DEVICE_REPRESENTATION_PROPERTY = "macAddress";
    // lightning effects
    public static final String DEVICE_PROPERTY_EFFECT = "lighting_effect";
    public static final String PROPERTY_LIGHTNING_EFFECT_BRIGHNTESS = "brightness";
    public static final String PROPERTY_LIGHTNING_EFFECT_COLORTEMPRANGE = "color_temp_range";
    public static final String PROPERTY_LIGHTNING_EFFECT_CUSTOM = "custom";
    public static final String PROPERTY_LIGHTNING_EFFECT_DISPLAYCOLORS = "displayColors";
    public static final String PROPERTY_LIGHTNING_EFFECT_ENABLE = "enable";
    public static final String PROPERTY_LIGHTNING_EFFECT_ID = "id";
    public static final String PROPERTY_LIGHTNING_EFFECT_NAME = "name";
    // energy monitoring
    public static final String ENERGY_PROPERTY_POWER = "current_power";
    public static final String ENERGY_PROPERTY_RUNTIME_TODAY = "today_runtime";
    public static final String ENERGY_PROPERTY_RUNTIME_MONTH = "month_runtime";
    public static final String ENERGY_PROPERTY_ENERGY_TODAY = "today_energy";
    public static final String ENERGY_PROPERTY_ENERGY_MONTH = "month_energy";
    public static final String ENERGY_PROPERTY_PAST24H = "past24h";
    public static final String ENERGY_PROPERTY_PAST7D = "past7d";
    public static final String ENERGY_PROPERTY_PAST30D = "past30d";
    public static final String ENERGY_PROPERTY_PAST1Y = "past1y";

    /*** DEVICE SETTINGS ***/
    public static final Integer BULB_MIN_COLORTEMP = 2500;
    public static final Integer BULB_MAX_COLORTEMP = 6500;

    /*** CHANNEL LISTS ***/
    // channel group actuator
    public static final String CHANNEL_GROUP_ACTUATOR = "actuator";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMP = "colorTemperature";
    public static final String CHANNEL_OUTPUT = "output";
    public static final String CHANNEL_SWITCH = "switch";
    // channel group device
    public static final String CHANNEL_GROUP_DEVICE = "device";
    public static final String CHANNEL_ONTIME = "onTime";
    public static final String CHANNEL_OVERHEAT = "overheated";
    public static final String CHANNEL_WIFI_STRENGTH = "wifiSignal";
    // channel group energy monitor
    public static final String CHANNEL_GROUP_ENERGY = "energy";
    public static final String CHANNEL_NRG_POWER = "actualPower";
    public static final String CHANNEL_NRG_USAGE_TODAY = "todayEnergyUsage";
    public static final String CHANNEL_NRG_RUNTIME_TODAY = "todayRuntime";
    // channel group effect
    public static final String CHANNEL_GROUP_EFFECTS = "effect";
    public static final String CHANNEL_FX_BRIGHTNESS = "brightness";
    public static final String CHANNEL_FX_COLORS = "displayColors";
    public static final String CHANNEL_FX_CUSTOM = "custom";
    public static final String CHANNEL_FX_ENABLE = "enable";
    public static final String CHANNEL_FX_NAME = "name";

    /*** LIST OF PROPERTY NAMES ***/
    public static final String PROPERTY_FAMILY = "deviceFamily";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_WIFI_LEVEL = "signal-strength";
}
