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
package org.openhab.binding.rfxcom.internal;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rfxcom.internal.config.RFXComDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComLighting4DeviceConfiguration;
import org.openhab.binding.rfxcom.internal.config.RFXComRawDeviceConfiguration;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RFXComBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class RFXComBindingConstants {

    public static final String BINDING_ID = "rfxcom";

    // List of all Bridge Type UIDs
    public static final String BRIDGE_TYPE_MANUAL_BRIDGE = "bridge";
    public static final String BRIDGE_TYPE_TCP_BRIDGE = "tcpbridge";
    public static final String BRIDGE_TYPE_RFXTRX433 = "RFXtrx433";
    public static final String BRIDGE_TYPE_RFXTRX315 = "RFXtrx315";
    public static final String BRIDGE_TYPE_RFXREC433 = "RFXrec433";

    public static final ThingTypeUID BRIDGE_MANUAL = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_MANUAL_BRIDGE);
    public static final ThingTypeUID BRIDGE_TCP = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_TCP_BRIDGE);
    public static final ThingTypeUID BRIDGE_RFXTRX443 = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_RFXTRX433);
    public static final ThingTypeUID BRIDGE_RFXTRX315 = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_RFXTRX315);
    public static final ThingTypeUID BRIDGE_RFXREC443 = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_RFXREC433);

    public static final int MAX_RFXCOM_MESSAGE_LEN = 256;

    /**
     * Presents all supported Bridge types by RFXCOM binding.
     */
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGE_MANUAL, BRIDGE_TCP, BRIDGE_RFXTRX443, BRIDGE_RFXTRX315, BRIDGE_RFXREC443)
                    .collect(Collectors.toSet()));

    /**
     * Presents all discoverable Bridge types by RFXCOM binding.
     */
    public static final Set<ThingTypeUID> DISCOVERABLE_BRIDGE_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(BRIDGE_RFXTRX443, BRIDGE_RFXTRX315, BRIDGE_RFXREC443).collect(Collectors.toSet()));

    // List of all Channel ids
    public static final String CHANNEL_RAW_MESSAGE = "rawMessage";
    public static final String CHANNEL_RAW_PAYLOAD = "rawPayload";
    public static final String CHANNEL_PULSES = "pulses";
    public static final String CHANNEL_SHUTTER = "shutter";
    public static final String CHANNEL_VENETIAN_BLIND = "venetianBlind";
    public static final String CHANNEL_SUN_WIND_DETECTOR = "sunWindDetector";
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_COMMAND_SECOND = "command2nd";
    public static final String CHANNEL_CONTROL = "control";
    public static final String CHANNEL_PROGRAM = "program";
    public static final String CHANNEL_COMMAND_ID = "commandId";
    public static final String CHANNEL_COMMAND_STRING = "commandString";
    public static final String CHANNEL_MOOD = "mood";
    public static final String CHANNEL_SIGNAL_LEVEL = "signalLevel";
    public static final String CHANNEL_DIMMING_LEVEL = "dimmingLevel";
    public static final String CHANNEL_UV = "uv";
    public static final String CHANNEL_FAN_LIGHT = "fanLight";
    public static final String CHANNEL_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_FOOD_TEMPERATURE = "foodTemperature";
    public static final String CHANNEL_BBQ_TEMPERATURE = "bbqTemperature";
    public static final String CHANNEL_CHILL_TEMPERATURE = "chillTemperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_HUMIDITY_STATUS = "humidityStatus";
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_LOW_BATTERY = "lowBattery";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_FORECAST = "forecast";
    public static final String CHANNEL_RAIN_RATE = "rainRate";
    public static final String CHANNEL_RAIN_TOTAL = "rainTotal";
    public static final String CHANNEL_WIND_DIRECTION = "windDirection";
    public static final String CHANNEL_WIND_SPEED = "windSpeed";
    public static final String CHANNEL_AVG_WIND_SPEED = "avgWindSpeed";
    public static final String CHANNEL_INSTANT_POWER = "instantPower";
    public static final String CHANNEL_TOTAL_USAGE = "totalUsage";
    public static final String CHANNEL_INSTANT_AMPS = "instantAmp";
    public static final String CHANNEL_TOTAL_AMP_HOUR = "totalAmpHour";
    public static final String CHANNEL_CHANNEL1_AMPS = "channel1Amps";
    public static final String CHANNEL_CHANNEL2_AMPS = "channel2Amps";
    public static final String CHANNEL_CHANNEL3_AMPS = "channel3Amps";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_CONTACT_1 = "contact1";
    public static final String CHANNEL_CONTACT_2 = "contact2";
    public static final String CHANNEL_CONTACT_3 = "contact3";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_REFERENCE_VOLTAGE = "referenceVoltage";
    public static final String CHANNEL_SET_POINT = "setpoint";
    public static final String CHANNEL_DATE_TIME = "dateTime";
    public static final String CHANNEL_CHIME_SOUND = "chimeSound";

    // List of all Thing Type UIDs
    private static final ThingTypeUID THING_TYPE_BAROMETRIC = new ThingTypeUID(BINDING_ID, "barometric");
    private static final ThingTypeUID THING_TYPE_BBQ_TEMPERATURE = new ThingTypeUID(BINDING_ID, "bbqtemperature");
    private static final ThingTypeUID THING_TYPE_BLINDS1 = new ThingTypeUID(BINDING_ID, "blinds1");
    private static final ThingTypeUID THING_TYPE_CAMERA1 = new ThingTypeUID(BINDING_ID, "camera1");
    private static final ThingTypeUID THING_TYPE_CHIME = new ThingTypeUID(BINDING_ID, "chime");
    private static final ThingTypeUID THING_TYPE_CURRENT = new ThingTypeUID(BINDING_ID, "current");
    private static final ThingTypeUID THING_TYPE_CURRENT_ENERGY = new ThingTypeUID(BINDING_ID, "currentenergy");
    private static final ThingTypeUID THING_TYPE_CURTAIN1 = new ThingTypeUID(BINDING_ID, "curtain1");
    private static final ThingTypeUID THING_TYPE_DATE_TIME = new ThingTypeUID(BINDING_ID, "datetime");
    private static final ThingTypeUID THING_TYPE_ENERGY = new ThingTypeUID(BINDING_ID, "energy");
    private static final ThingTypeUID THING_TYPE_FAN = new ThingTypeUID(BINDING_ID, "fan");
    private static final ThingTypeUID THING_TYPE_FAN_SF01 = new ThingTypeUID(BINDING_ID, "fan_sf01");
    private static final ThingTypeUID THING_TYPE_FAN_ITHO = new ThingTypeUID(BINDING_ID, "fan_itho");
    private static final ThingTypeUID THING_TYPE_FAN_SEAV = new ThingTypeUID(BINDING_ID, "fan_seav");
    private static final ThingTypeUID THING_TYPE_FAN_LUCCI_DC = new ThingTypeUID(BINDING_ID, "fan_lucci_dc");
    private static final ThingTypeUID THING_TYPE_FAN_FT1211R = new ThingTypeUID(BINDING_ID, "fan_ft1211r");
    private static final ThingTypeUID THING_TYPE_FAN_FALMEC = new ThingTypeUID(BINDING_ID, "fan_falmec");
    private static final ThingTypeUID THING_TYPE_FAN_LUCCI_DC_II = new ThingTypeUID(BINDING_ID, "fan_lucci_dc_ii");
    private static final ThingTypeUID THING_TYPE_FAN_NOVY = new ThingTypeUID(BINDING_ID, "fan_novy");
    private static final ThingTypeUID THING_TYPE_FS20 = new ThingTypeUID(BINDING_ID, "fs20");
    private static final ThingTypeUID THING_TYPE_GAS_USAGE = new ThingTypeUID(BINDING_ID, "gasusage");
    private static final ThingTypeUID THING_TYPE_HOME_CONFORT = new ThingTypeUID(BINDING_ID, "homeconfort");
    private static final ThingTypeUID THING_TYPE_HUMIDITY = new ThingTypeUID(BINDING_ID, "humidity");
    private static final ThingTypeUID THING_TYPE_IO_LINES = new ThingTypeUID(BINDING_ID, "iolines");
    private static final ThingTypeUID THING_TYPE_LIGHTNING1 = new ThingTypeUID(BINDING_ID, "lighting1");
    private static final ThingTypeUID THING_TYPE_LIGHTNING2 = new ThingTypeUID(BINDING_ID, "lighting2");
    private static final ThingTypeUID THING_TYPE_LIGHTNING3 = new ThingTypeUID(BINDING_ID, "lighting3");
    private static final ThingTypeUID THING_TYPE_LIGHTNING4 = new ThingTypeUID(BINDING_ID, "lighting4");
    private static final ThingTypeUID THING_TYPE_LIGHTNING5 = new ThingTypeUID(BINDING_ID, "lighting5");
    private static final ThingTypeUID THING_TYPE_LIGHTNING6 = new ThingTypeUID(BINDING_ID, "lighting6");
    private static final ThingTypeUID THING_TYPE_POWER = new ThingTypeUID(BINDING_ID, "power");
    private static final ThingTypeUID THING_TYPE_RADIATOR1 = new ThingTypeUID(BINDING_ID, "radiator1");
    private static final ThingTypeUID THING_TYPE_RAIN = new ThingTypeUID(BINDING_ID, "rain");
    private static final ThingTypeUID THING_TYPE_RAW = new ThingTypeUID(BINDING_ID, "raw");
    private static final ThingTypeUID THING_TYPE_REMOTE_CONTROL = new ThingTypeUID(BINDING_ID, "remotecontrol");
    private static final ThingTypeUID THING_TYPE_RFX_METER = new ThingTypeUID(BINDING_ID, "rfxmeter");
    private static final ThingTypeUID THING_TYPE_RFX_SENSOR = new ThingTypeUID(BINDING_ID, "rfxsensor");
    private static final ThingTypeUID THING_TYPE_RFY = new ThingTypeUID(BINDING_ID, "rfy");
    private static final ThingTypeUID THING_TYPE_SECURITY1 = new ThingTypeUID(BINDING_ID, "security1");
    private static final ThingTypeUID THING_TYPE_SECURITY2 = new ThingTypeUID(BINDING_ID, "security2");
    private static final ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    private static final ThingTypeUID THING_TYPE_TEMPERATURE_HUMIDITY = new ThingTypeUID(BINDING_ID,
            "temperaturehumidity");
    private static final ThingTypeUID THING_TYPE_TEMPERATURE_HUMIDITY_BAROMETRIC = new ThingTypeUID(BINDING_ID,
            "temperaturehumiditybarometric");
    private static final ThingTypeUID THING_TYPE_TEMPERATURE_RAIN = new ThingTypeUID(BINDING_ID, "temperaturerain");
    private static final ThingTypeUID THING_TYPE_THERMOSTAT1 = new ThingTypeUID(BINDING_ID, "thermostat1");
    private static final ThingTypeUID THING_TYPE_THERMOSTAT2 = new ThingTypeUID(BINDING_ID, "thermostat2");
    private static final ThingTypeUID THING_TYPE_THERMOSTAT3 = new ThingTypeUID(BINDING_ID, "thermostat3");
    private static final ThingTypeUID THING_TYPE_UNDECODED = new ThingTypeUID(BINDING_ID, "undecoded");
    private static final ThingTypeUID THING_TYPE_UV = new ThingTypeUID(BINDING_ID, "uv");
    private static final ThingTypeUID THING_TYPE_WATER_USAGE = new ThingTypeUID(BINDING_ID, "waterusage");
    private static final ThingTypeUID THING_TYPE_WEIGHTING_SCALE = new ThingTypeUID(BINDING_ID, "weightingscale");
    private static final ThingTypeUID THING_TYPE_WIND = new ThingTypeUID(BINDING_ID, "wind");

    /**
     * Presents all supported Thing types by RFXCOM binding.
     */
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_BAROMETRIC, THING_TYPE_BBQ_TEMPERATURE, THING_TYPE_BLINDS1,
                    THING_TYPE_CAMERA1, THING_TYPE_CHIME, THING_TYPE_CURRENT, THING_TYPE_CURRENT_ENERGY,
                    THING_TYPE_CURTAIN1, THING_TYPE_DATE_TIME, THING_TYPE_ENERGY, THING_TYPE_FAN, THING_TYPE_FAN_SF01,
                    THING_TYPE_FAN_ITHO, THING_TYPE_FAN_SEAV, THING_TYPE_FAN_LUCCI_DC, THING_TYPE_FAN_FT1211R,
                    THING_TYPE_FAN_FALMEC, THING_TYPE_FAN_LUCCI_DC_II, THING_TYPE_FAN_NOVY, THING_TYPE_GAS_USAGE,
                    THING_TYPE_HOME_CONFORT, THING_TYPE_HUMIDITY, THING_TYPE_IO_LINES, THING_TYPE_LIGHTNING1,
                    THING_TYPE_LIGHTNING2, THING_TYPE_LIGHTNING3, THING_TYPE_LIGHTNING4, THING_TYPE_LIGHTNING5,
                    THING_TYPE_LIGHTNING6, THING_TYPE_POWER, THING_TYPE_RADIATOR1, THING_TYPE_RAIN, THING_TYPE_RAW,
                    THING_TYPE_REMOTE_CONTROL, THING_TYPE_RFX_METER, THING_TYPE_RFX_SENSOR, THING_TYPE_RFY,
                    THING_TYPE_SECURITY1, THING_TYPE_SECURITY2, THING_TYPE_TEMPERATURE, THING_TYPE_TEMPERATURE_HUMIDITY,
                    THING_TYPE_TEMPERATURE_HUMIDITY_BAROMETRIC, THING_TYPE_TEMPERATURE_RAIN, THING_TYPE_THERMOSTAT1,
                    THING_TYPE_THERMOSTAT2, THING_TYPE_THERMOSTAT3, THING_TYPE_UNDECODED, THING_TYPE_UV,
                    THING_TYPE_WATER_USAGE, THING_TYPE_WEIGHTING_SCALE, THING_TYPE_WIND).collect(Collectors.toSet()));

    /**
     * Map Device ThingTypeUIDs to their Configuration class
     */
    public static final Map<ThingTypeUID, Class<? extends RFXComDeviceConfiguration>> THING_TYPE_UID_CONFIGURATION_CLASS_MAP = Map
            .ofEntries(
                    new AbstractMap.SimpleEntry<ThingTypeUID, Class<? extends RFXComDeviceConfiguration>>(
                            THING_TYPE_RAW, RFXComRawDeviceConfiguration.class),
                    new AbstractMap.SimpleEntry<ThingTypeUID, Class<? extends RFXComDeviceConfiguration>>(
                            THING_TYPE_LIGHTNING4, RFXComLighting4DeviceConfiguration.class));

    /**
     * Map RFXCOM packet types to RFXCOM Thing types and vice versa.
     */
    public static final Map<PacketType, ThingTypeUID> PACKET_TYPE_THING_TYPE_UID_MAP = Collections
            .unmodifiableMap(new HashMap<PacketType, ThingTypeUID>() {
                {
                    put(PacketType.BAROMETRIC, RFXComBindingConstants.THING_TYPE_BAROMETRIC);
                    put(PacketType.BBQ, RFXComBindingConstants.THING_TYPE_BBQ_TEMPERATURE);
                    put(PacketType.BLINDS1, RFXComBindingConstants.THING_TYPE_BLINDS1);
                    put(PacketType.CAMERA1, RFXComBindingConstants.THING_TYPE_CAMERA1);
                    put(PacketType.CHIME, RFXComBindingConstants.THING_TYPE_CHIME);
                    put(PacketType.CURRENT, RFXComBindingConstants.THING_TYPE_CURRENT);
                    put(PacketType.CURRENT_ENERGY, RFXComBindingConstants.THING_TYPE_CURRENT_ENERGY);
                    put(PacketType.CURTAIN1, RFXComBindingConstants.THING_TYPE_CURTAIN1);
                    put(PacketType.DATE_TIME, RFXComBindingConstants.THING_TYPE_DATE_TIME);
                    put(PacketType.ENERGY, RFXComBindingConstants.THING_TYPE_ENERGY);
                    put(PacketType.FAN, RFXComBindingConstants.THING_TYPE_FAN);
                    put(PacketType.FAN_SF01, RFXComBindingConstants.THING_TYPE_FAN_SF01);
                    put(PacketType.FAN_ITHO, RFXComBindingConstants.THING_TYPE_FAN_ITHO);
                    put(PacketType.FAN_SEAV, RFXComBindingConstants.THING_TYPE_FAN_SEAV);
                    put(PacketType.FAN_LUCCI_DC, RFXComBindingConstants.THING_TYPE_FAN_LUCCI_DC);
                    put(PacketType.FAN_FT1211R, RFXComBindingConstants.THING_TYPE_FAN_FT1211R);
                    put(PacketType.FAN_FALMEC, RFXComBindingConstants.THING_TYPE_FAN_FALMEC);
                    put(PacketType.FAN_LUCCI_DC_II, RFXComBindingConstants.THING_TYPE_FAN_LUCCI_DC_II);
                    put(PacketType.FAN_NOVY, RFXComBindingConstants.THING_TYPE_FAN_NOVY);
                    put(PacketType.FS20, RFXComBindingConstants.THING_TYPE_FS20);
                    put(PacketType.GAS, RFXComBindingConstants.THING_TYPE_GAS_USAGE);
                    put(PacketType.HOME_CONFORT, RFXComBindingConstants.THING_TYPE_HOME_CONFORT);
                    put(PacketType.HUMIDITY, RFXComBindingConstants.THING_TYPE_HUMIDITY);
                    put(PacketType.IO_LINES, RFXComBindingConstants.THING_TYPE_IO_LINES);
                    put(PacketType.LIGHTING1, RFXComBindingConstants.THING_TYPE_LIGHTNING1);
                    put(PacketType.LIGHTING2, RFXComBindingConstants.THING_TYPE_LIGHTNING2);
                    put(PacketType.LIGHTING3, RFXComBindingConstants.THING_TYPE_LIGHTNING3);
                    put(PacketType.LIGHTING4, RFXComBindingConstants.THING_TYPE_LIGHTNING4);
                    put(PacketType.LIGHTING5, RFXComBindingConstants.THING_TYPE_LIGHTNING5);
                    put(PacketType.LIGHTING6, RFXComBindingConstants.THING_TYPE_LIGHTNING6);
                    put(PacketType.POWER, RFXComBindingConstants.THING_TYPE_POWER);
                    put(PacketType.RADIATOR1, RFXComBindingConstants.THING_TYPE_RADIATOR1);
                    put(PacketType.RAIN, RFXComBindingConstants.THING_TYPE_RAIN);
                    put(PacketType.RAW, RFXComBindingConstants.THING_TYPE_RAW);
                    put(PacketType.REMOTE_CONTROL, RFXComBindingConstants.THING_TYPE_REMOTE_CONTROL);
                    put(PacketType.RFXMETER, RFXComBindingConstants.THING_TYPE_RFX_METER);
                    put(PacketType.RFXSENSOR, RFXComBindingConstants.THING_TYPE_RFX_SENSOR);
                    put(PacketType.RFY, RFXComBindingConstants.THING_TYPE_RFY);
                    put(PacketType.SECURITY1, RFXComBindingConstants.THING_TYPE_SECURITY1);
                    put(PacketType.SECURITY2, RFXComBindingConstants.THING_TYPE_SECURITY2);
                    put(PacketType.TEMPERATURE, RFXComBindingConstants.THING_TYPE_TEMPERATURE);
                    put(PacketType.TEMPERATURE_HUMIDITY, RFXComBindingConstants.THING_TYPE_TEMPERATURE_HUMIDITY);
                    put(PacketType.TEMPERATURE_HUMIDITY_BAROMETRIC,
                            RFXComBindingConstants.THING_TYPE_TEMPERATURE_HUMIDITY_BAROMETRIC);
                    put(PacketType.TEMPERATURE_RAIN, RFXComBindingConstants.THING_TYPE_TEMPERATURE_RAIN);
                    put(PacketType.THERMOSTAT1, RFXComBindingConstants.THING_TYPE_THERMOSTAT1);
                    put(PacketType.THERMOSTAT2, RFXComBindingConstants.THING_TYPE_THERMOSTAT2);
                    put(PacketType.THERMOSTAT3, RFXComBindingConstants.THING_TYPE_THERMOSTAT3);
                    put(PacketType.UNDECODED_RF_MESSAGE, RFXComBindingConstants.THING_TYPE_UNDECODED);
                    put(PacketType.UV, RFXComBindingConstants.THING_TYPE_UV);
                    put(PacketType.WATER, RFXComBindingConstants.THING_TYPE_WATER_USAGE);
                    put(PacketType.WEIGHT, RFXComBindingConstants.THING_TYPE_WEIGHTING_SCALE);
                    put(PacketType.WIND, RFXComBindingConstants.THING_TYPE_WIND);
                }
            });
}
