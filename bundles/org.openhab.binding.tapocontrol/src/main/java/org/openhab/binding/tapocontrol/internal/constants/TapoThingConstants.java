/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.tapocontrol.internal.constants.TapoBindingSettings.BINDING_ID;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TapoThingConstants} class defines thing constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 ***/
@NonNullByDefault
public class TapoThingConstants {
    public static final String DEVICE_VENDOR = "Tapo";

    /*** LIST OF SUPPORTED DEVICE NAMES ***/
    public static final String DEVICE_BRIDGE = "bridge";
    public static final String DEVICE_H100 = "H100";
    public static final String DEVICE_P100 = "P100";
    public static final String DEVICE_P105 = "P105";
    public static final String DEVICE_P110 = "P110";
    public static final String DEVICE_P115 = "P115";
    public static final String DEVICE_P300 = "P300";
    public static final String DEVICE_L510 = "L510";
    public static final String DEVICE_L530 = "L530";
    public static final String DEVICE_L610 = "L610";
    public static final String DEVICE_L630 = "L630";
    public static final String DEVICE_L900 = "L900";
    public static final String DEVICE_L920 = "L920";
    public static final String DEVICE_L930 = "L930";
    public static final String DEVICE_T110 = "T110";
    public static final String DEVICE_T310 = "T310";
    public static final String DEVICE_T315 = "T315";
    public static final String DEVICE_UNIVERSAL = "Test_Device";

    /*** LIST OF SUPPORTED DEVICE DESCRIPTIONS ***/
    public static final String DEVICE_DESCRIPTION_BRIDGE = "TapoControl Cloud-Login";
    public static final String DEVICE_DESCRIPTION_HUB = "SmartHub";
    public static final String DEVICE_DESCRIPTION_SOCKET = "SmartPlug";
    public static final String DEVICE_DESCRIPTION_SOCKET_STRIP = "PowerStrip";
    public static final String DEVICE_DESCRIPTION_WHITE_BULB = "White-Light-Bulb";
    public static final String DEVICE_DESCRIPTION_COLOR_BULB = "Color-Light-Bulb";
    public static final String DEVICE_DESCRIPTION_LIGHTSTRIP = "LightStrip";
    public static final String DEVICE_DESCRIPTION_SMART_CONTACT = "Smart-Contact-Sensor";
    public static final String DEVICE_DESCRIPTION_MOTION_SENSOR = "Motion-Sensor";
    public static final String DEVICE_DESCRIPTION_TEMP_SENSOR = "Temperature-Sensor";

    /*** LIST OF SUPPORTED THING UIDS ***/
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_BRIDGE);
    public static final ThingTypeUID H100_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_H100);
    public static final ThingTypeUID P100_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P100);
    public static final ThingTypeUID P105_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P105);
    public static final ThingTypeUID P110_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P110);
    public static final ThingTypeUID P115_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P115);
    public static final ThingTypeUID P300_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P300);
    public static final ThingTypeUID L510_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L510);
    public static final ThingTypeUID L530_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L530);
    public static final ThingTypeUID L610_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L610);
    public static final ThingTypeUID L630_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L630);
    public static final ThingTypeUID L900_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L900);
    public static final ThingTypeUID L920_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L920);
    public static final ThingTypeUID L930_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L930);
    public static final ThingTypeUID UNIVERSAL_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_UNIVERSAL);

    /*** LIST OF SUPPORTED HUB CHILD THING UIDS ***/
    public static final ThingTypeUID T110_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_T110);
    public static final ThingTypeUID T310_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_T310);
    public static final ThingTypeUID T315_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_T315);

    /*** SET OF SUPPORTED UIDS ***/
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_UIDS = Set.of(BRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_HUB_UIDS = Set.of(H100_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_SOCKET_UIDS = Set.of(P100_THING_TYPE, P105_THING_TYPE,
            P110_THING_TYPE, P115_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_SOCKET_STRIP_UIDS = Set.of(P300_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_WHITE_BULB_UIDS = Set.of(L510_THING_TYPE, L610_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_COLOR_BULB_UIDS = Set.of(L530_THING_TYPE, L630_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_LIGHT_STRIP_UIDS = Set.of(L900_THING_TYPE, L920_THING_TYPE,
            L930_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_HUB_CHILD_TYPES_UIDS = Set.of(T110_THING_TYPE, T310_THING_TYPE,
            T315_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_SMART_CONTACTS = Set.of(T110_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_MOTION_SENSORS = Set.of();
    public static final Set<ThingTypeUID> SUPPORTED_WHEATHER_SENSORS = Set.of(T310_THING_TYPE, T315_THING_TYPE);

    /*** SET OF ALL SUPPORTED THINGS ***/
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(SUPPORTED_BRIDGE_UIDS, SUPPORTED_HUB_UIDS, SUPPORTED_SOCKET_UIDS, SUPPORTED_SOCKET_STRIP_UIDS,
                    SUPPORTED_WHITE_BULB_UIDS, SUPPORTED_COLOR_BULB_UIDS, SUPPORTED_LIGHT_STRIP_UIDS,
                    SUPPORTED_SMART_CONTACTS, SUPPORTED_MOTION_SENSORS, SUPPORTED_WHEATHER_SENSORS)
            .flatMap(Set::stream).collect(Collectors.toSet()));

    /*** THINGS WITH ENERGY DATA ***/
    public static final Set<ThingTypeUID> SUPPORTED_ENERGY_DATA_UIDS = Set.of(P110_THING_TYPE, P115_THING_TYPE);

    /*** THINGS WITH CHANNEL GROUPS ***/
    public static final Set<ThingTypeUID> CHANNEL_GROUP_THING_SET = Collections.unmodifiableSet(Stream
            .of(SUPPORTED_BRIDGE_UIDS, SUPPORTED_HUB_UIDS, SUPPORTED_SOCKET_UIDS, SUPPORTED_SOCKET_STRIP_UIDS,
                    SUPPORTED_WHITE_BULB_UIDS, SUPPORTED_COLOR_BULB_UIDS, SUPPORTED_LIGHT_STRIP_UIDS,
                    SUPPORTED_SMART_CONTACTS, SUPPORTED_MOTION_SENSORS, SUPPORTED_WHEATHER_SENSORS)
            .flatMap(Set::stream).collect(Collectors.toSet()));

    public static final String CHILD_REPRESENTATION_PROPERTY = "serialNumber";

    /*** DEVICE SETTINGS ***/
    public static final Integer BULB_MIN_COLORTEMP = 2500;
    public static final Integer BULB_MAX_COLORTEMP = 6500;

    /*** CHANNEL LISTS ***/
    // channel group actuator
    public static final String CHANNEL_GROUP_ACTUATOR = "actuator";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMP = "colorTemperature";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_OUTPUT = "output";
    public static final String CHANNEL_SWITCH = "switch";
    // channel group device
    public static final String CHANNEL_GROUP_DEVICE = "device";
    public static final String CHANNEL_BATTERY_LOW = "batteryLow";
    public static final String CHANNEL_ONTIME = "onTime";
    public static final String CHANNEL_OVERHEAT = "overheated";
    public static final String CHANNEL_SIGNAL_STRENGTH = "signalStrength";
    public static final String CHANNEL_WIFI_STRENGTH = "wifiSignal";
    // channel group alarm
    public static final String CHANNEL_GROUP_ALARM = "alarm";
    public static final String CHANNEL_ALARM_ACTIVE = "alarmActive";
    public static final String CHANNEL_ALARM_SOURCE = "alarmSource";
    // channel group sensor
    public static final String CHANNEL_GROUP_SENSOR = "sensor";
    public static final String CHANNEL_IS_OPEN = "isOpen";
    public static final String CHANNEL_TEMPERATURE = "currentTemp";
    public static final String CHANNEL_HUMIDITY = "currentHumidity";
    // channel group energy monitor
    public static final String CHANNEL_GROUP_ENERGY = "energy";
    public static final String CHANNEL_NRG_POWER = "actualPower";
    public static final String CHANNEL_NRG_USAGE_TODAY = "todayEnergyUsage";
    public static final String CHANNEL_NRG_RUNTIME_TODAY = "todayRuntime";
    public static final String CHANNEL_NRG_USAGE_MONTH = "monthEnergyUsage";
    public static final String CHANNEL_NRG_RUNTIME_MONTH = "monthRuntime";
    // channel group effect
    public static final String CHANNEL_GROUP_EFFECTS = "effects";
    public static final String CHANNEL_FX_BRIGHTNESS = "fxBrightness";
    public static final String CHANNEL_FX_COLORS = "fxColors";
    public static final String CHANNEL_FX_NAME = "fxName";

    /*** LIST OF PROPERTY NAMES ***/
    public static final String PROPERTY_FAMILY = "deviceFamily";
    public static final String PROPERTY_LOCATION = "location";
    public static final String PROPERTY_WIFI_LEVEL = "signal-strength";

    /*** EVENT LISTS ***/
    // hub child events
    public static final String EVENT_BATTERY_LOW = "batteryIsLow";
    public static final String EVENT_CONTACT_OPENED = "contactOpened";
    public static final String EVENT_CONTACT_CLOSED = "contactClosed";
    public static final String EVENT_STATE_BATTERY_LOW = "batteryLow";
    public static final String EVENT_STATE_OPENED = "open";
    public static final String EVENT_STATE_CLOSED = "closed";
}
