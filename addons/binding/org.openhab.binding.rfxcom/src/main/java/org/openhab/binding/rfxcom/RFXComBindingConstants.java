/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom;

import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * The {@link RFXComBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class RFXComBindingConstants {

    public static final String BINDING_ID = "rfxcom";

    public static final String SERIAL_PORT = "serialPort";
    public static final String BRIDGE_ID = "bridgeId";
    public static final String DEVICE_ID = "deviceId";
    public static final String SUB_TYPE = "subType";

    // List of all Bridge Type UIDs
    public static final String BRIDGE_TYPE_MANUAL_BRIDGE = "bridge";
    public static final String BRIDGE_TYPE_TCP_BRIDGE = "tcpbridge";
    public static final String BRIDGE_TYPE_RFXTRX433 = "RFXtrx433";
    public static final String BRIDGE_TYPE_RFXTRX315 = "RFXtrx315";
    public static final String BRIDGE_TYPE_RFXREC433 = "RFXrec433";

    // Transceiver types
    public static final String TRANSCEIVER_310MHz = "310MHz";
    public static final String TRANSCEIVER_315MHz = "315MHz";
    public static final String TRANSCEIVER_433_92MHz = "433.92MHz";
    public static final String TRANSCEIVER_433_92MHz_R = "433.92MHz receiver only";

    public final static ThingTypeUID BRIDGE_MANUAL = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_MANUAL_BRIDGE);
    public final static ThingTypeUID BRIDGE_TCP = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_TCP_BRIDGE);
    public final static ThingTypeUID BRIDGE_RFXTRX443 = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_RFXTRX433);
    public final static ThingTypeUID BRIDGE_RFXTRX315 = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_RFXTRX315);
    public final static ThingTypeUID BRIDGE_RFXREC443 = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_RFXREC433);

    /**
     * Presents all supported Bridge types by RFXCOM binding.
     */
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(BRIDGE_MANUAL, BRIDGE_TCP,
            BRIDGE_RFXTRX443, BRIDGE_RFXTRX315, BRIDGE_RFXREC443);

    /**
     * Presents all discoverable Bridge types by RFXCOM binding.
     */
    public final static Set<ThingTypeUID> DISCOVERABLE_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(BRIDGE_RFXTRX443,
            BRIDGE_RFXTRX315, BRIDGE_RFXREC443);

    // List of all Channel ids
    public final static String CHANNEL_RAW_MESSAGE = "rawMessage";
    public final static String CHANNEL_RAW_PAYLOAD = "rawPayload";
    public final static String CHANNEL_SHUTTER = "shutter";
    public final static String CHANNEL_COMMAND = "command";
    public final static String CHANNEL_MOOD = "mood";
    public final static String CHANNEL_SIGNAL_LEVEL = "signalLevel";
    public final static String CHANNEL_DIMMING_LEVEL = "dimmingLevel";
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_HUMIDITY_STATUS = "humidityStatus";
    public final static String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public final static String CHANNEL_LOW_BATTERY = "lowBattery";
    public final static String CHANNEL_PRESSURE = "pressure";
    public final static String CHANNEL_FORECAST = "forecast";
    public final static String CHANNEL_RAIN_RATE = "rainRate";
    public final static String CHANNEL_RAIN_TOTAL = "rainTotal";
    public final static String CHANNEL_WIND_DIRECTION = "windDirection";
    public final static String CHANNEL_WIND_SPEED = "windSpeed";
    public final static String CHANNEL_GUST = "gust";
    public final static String CHANNEL_CHILL_FACTOR = "chillFactor";
    public final static String CHANNEL_INSTANT_POWER = "instantPower";
    public final static String CHANNEL_TOTAL_USAGE = "totalUsage";
    public final static String CHANNEL_INSTANT_AMPS = "instantAmp";
    public final static String CHANNEL_TOTAL_AMP_HOUR = "totalAmpHour";
    public static final String CHANNEL_CHANNEL1_AMPS = "channel1Amps";
    public static final String CHANNEL_CHANNEL2_AMPS = "channel2Amps";
    public static final String CHANNEL_CHANNEL3_AMPS = "channel3Amps";
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_MOTION = "motion";
    public final static String CHANNEL_CONTACT = "contact";
    public final static String CHANNEL_VOLTAGE = "voltage";
    public final static String CHANNEL_SET_POINT = "setpoint";
    public static final String CHANNEL_DATE_TIME = "dateTime";

    // List of all Thing Type UIDs
    private final static ThingTypeUID THING_TYPE_BAROMETRIC = new ThingTypeUID(BINDING_ID, "barometric");
    private final static ThingTypeUID THING_TYPE_BBQ_TEMPERATURE = new ThingTypeUID(BINDING_ID, "bbqtemperature");
    private final static ThingTypeUID THING_TYPE_BLINDS1 = new ThingTypeUID(BINDING_ID, "blinds1");
    private final static ThingTypeUID THING_TYPE_CAMERA1 = new ThingTypeUID(BINDING_ID, "camera1");
    private final static ThingTypeUID THING_TYPE_CHIME = new ThingTypeUID(BINDING_ID, "chime");
    private final static ThingTypeUID THING_TYPE_CURRENT = new ThingTypeUID(BINDING_ID, "current");
    private final static ThingTypeUID THING_TYPE_CURRENT_ENERGY = new ThingTypeUID(BINDING_ID, "currentenergy");
    private final static ThingTypeUID THING_TYPE_CURTAIN1 = new ThingTypeUID(BINDING_ID, "curtain1");
    private final static ThingTypeUID THING_TYPE_DATE_TIME = new ThingTypeUID(BINDING_ID, "datetime");
    private final static ThingTypeUID THING_TYPE_ENERGY = new ThingTypeUID(BINDING_ID, "energy");
    private final static ThingTypeUID THING_TYPE_FAN = new ThingTypeUID(BINDING_ID, "fan");
    private final static ThingTypeUID THING_TYPE_FS20 = new ThingTypeUID(BINDING_ID, "fs20");
    private final static ThingTypeUID THING_TYPE_GAS_USAGE = new ThingTypeUID(BINDING_ID, "gasusage");
    private final static ThingTypeUID THING_TYPE_HOME_CONFORT = new ThingTypeUID(BINDING_ID, "homeconfort");
    private final static ThingTypeUID THING_TYPE_HUMIDITY = new ThingTypeUID(BINDING_ID, "humidity");
    private final static ThingTypeUID THING_TYPE_IO_LINES = new ThingTypeUID(BINDING_ID, "iolines");
    private final static ThingTypeUID THING_TYPE_LIGHTNING1 = new ThingTypeUID(BINDING_ID, "lighting1");
    private final static ThingTypeUID THING_TYPE_LIGHTNING2 = new ThingTypeUID(BINDING_ID, "lighting2");
    private final static ThingTypeUID THING_TYPE_LIGHTNING3 = new ThingTypeUID(BINDING_ID, "lighting3");
    private final static ThingTypeUID THING_TYPE_LIGHTNING4 = new ThingTypeUID(BINDING_ID, "lighting4");
    private final static ThingTypeUID THING_TYPE_LIGHTNING5 = new ThingTypeUID(BINDING_ID, "lighting5");
    private final static ThingTypeUID THING_TYPE_LIGHTNING6 = new ThingTypeUID(BINDING_ID, "lighting6");
    private final static ThingTypeUID THING_TYPE_POWER = new ThingTypeUID(BINDING_ID, "power");
    private final static ThingTypeUID THING_TYPE_RADIATOR1 = new ThingTypeUID(BINDING_ID, "radiator1");
    private final static ThingTypeUID THING_TYPE_RAIN = new ThingTypeUID(BINDING_ID, "rain");
    private final static ThingTypeUID THING_TYPE_REMOTE_CONTROL = new ThingTypeUID(BINDING_ID, "remotecontrol");
    private final static ThingTypeUID THING_TYPE_RFX_METER = new ThingTypeUID(BINDING_ID, "rfxmeter");
    private final static ThingTypeUID THING_TYPE_RFX_SENSOR = new ThingTypeUID(BINDING_ID, "rfxsensor");
    private final static ThingTypeUID THING_TYPE_RFY = new ThingTypeUID(BINDING_ID, "rfy");
    private final static ThingTypeUID THING_TYPE_SECURITY1 = new ThingTypeUID(BINDING_ID, "security1");
    private final static ThingTypeUID THING_TYPE_SECURITY2 = new ThingTypeUID(BINDING_ID, "security2");
    private final static ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    private final static ThingTypeUID THING_TYPE_TEMPERATURE_HUMIDITY = new ThingTypeUID(BINDING_ID,
            "temperaturehumidity");
    private final static ThingTypeUID THING_TYPE_TEMPERATURE_HUMIDITY_BAROMETRIC = new ThingTypeUID(BINDING_ID,
            "temperaturehumiditybarometric");
    private final static ThingTypeUID THING_TYPE_TEMPERATURE_RAIN = new ThingTypeUID(BINDING_ID, "temperaturerain");
    private final static ThingTypeUID THING_TYPE_THERMOSTAT1 = new ThingTypeUID(BINDING_ID, "thermostat1");
    private final static ThingTypeUID THING_TYPE_THERMOSTAT2 = new ThingTypeUID(BINDING_ID, "thermostat2");
    private final static ThingTypeUID THING_TYPE_THERMOSTAT3 = new ThingTypeUID(BINDING_ID, "thermostat3");
    private final static ThingTypeUID THING_TYPE_UNDECODED = new ThingTypeUID(BINDING_ID, "undecoded");
    private final static ThingTypeUID THING_TYPE_UV = new ThingTypeUID(BINDING_ID, "uv");
    private final static ThingTypeUID THING_TYPE_WATER_USAGE = new ThingTypeUID(BINDING_ID, "waterusage");
    private final static ThingTypeUID THING_TYPE_WEIGHTING_SCALE = new ThingTypeUID(BINDING_ID, "weightingscale");
    private final static ThingTypeUID THING_TYPE_WIND = new ThingTypeUID(BINDING_ID, "wind");

    /**
     * Presents all supported Thing types by RFXCOM binding.
     */
    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BAROMETRIC,
            THING_TYPE_BBQ_TEMPERATURE, THING_TYPE_BLINDS1, THING_TYPE_CAMERA1, THING_TYPE_CHIME, THING_TYPE_CURRENT,
            THING_TYPE_CURRENT_ENERGY, THING_TYPE_CURTAIN1, THING_TYPE_DATE_TIME, THING_TYPE_ENERGY, THING_TYPE_FAN,
            THING_TYPE_FS20, THING_TYPE_GAS_USAGE, THING_TYPE_HOME_CONFORT, THING_TYPE_HUMIDITY, THING_TYPE_IO_LINES,
            THING_TYPE_LIGHTNING1, THING_TYPE_LIGHTNING2, THING_TYPE_LIGHTNING3, THING_TYPE_LIGHTNING4,
            THING_TYPE_LIGHTNING5, THING_TYPE_LIGHTNING6, THING_TYPE_POWER, THING_TYPE_RADIATOR1, THING_TYPE_RAIN,
            THING_TYPE_REMOTE_CONTROL, THING_TYPE_RFX_METER, THING_TYPE_RFX_SENSOR, THING_TYPE_RFY,
            THING_TYPE_SECURITY1, THING_TYPE_SECURITY2, THING_TYPE_TEMPERATURE, THING_TYPE_TEMPERATURE_HUMIDITY,
            THING_TYPE_TEMPERATURE_HUMIDITY_BAROMETRIC, THING_TYPE_TEMPERATURE_RAIN, THING_TYPE_THERMOSTAT1,
            THING_TYPE_THERMOSTAT2, THING_TYPE_THERMOSTAT3, THING_TYPE_UNDECODED, THING_TYPE_UV, THING_TYPE_WATER_USAGE,
            THING_TYPE_WEIGHTING_SCALE, THING_TYPE_WIND);

    /**
     * Map RFXCOM packet types to RFXCOM Thing types and vice versa.
     */
    public final static Map<PacketType, ThingTypeUID> packetTypeThingMap = ImmutableMap
            .<PacketType, ThingTypeUID>builder()
            .put(PacketType.BAROMETRIC, RFXComBindingConstants.THING_TYPE_BAROMETRIC)
            .put(PacketType.BBQ1, RFXComBindingConstants.THING_TYPE_BBQ_TEMPERATURE)
            .put(PacketType.BLINDS1, RFXComBindingConstants.THING_TYPE_BLINDS1)
            .put(PacketType.CAMERA1, RFXComBindingConstants.THING_TYPE_CAMERA1)
            .put(PacketType.CHIME, RFXComBindingConstants.THING_TYPE_CHIME)
            .put(PacketType.CURRENT, RFXComBindingConstants.THING_TYPE_CURRENT)
            .put(PacketType.CURRENT_ENERGY, RFXComBindingConstants.THING_TYPE_CURRENT_ENERGY)
            .put(PacketType.CURTAIN1, RFXComBindingConstants.THING_TYPE_CURTAIN1)
            .put(PacketType.DATE_TIME, RFXComBindingConstants.THING_TYPE_DATE_TIME)
            .put(PacketType.ENERGY, RFXComBindingConstants.THING_TYPE_ENERGY)
            .put(PacketType.FAN, RFXComBindingConstants.THING_TYPE_FAN)
            .put(PacketType.FS20, RFXComBindingConstants.THING_TYPE_FS20)
            .put(PacketType.GAS, RFXComBindingConstants.THING_TYPE_GAS_USAGE)
            .put(PacketType.HOME_CONFORT, RFXComBindingConstants.THING_TYPE_HOME_CONFORT)
            .put(PacketType.HUMIDITY, RFXComBindingConstants.THING_TYPE_HUMIDITY)
            .put(PacketType.IO_LINES, RFXComBindingConstants.THING_TYPE_IO_LINES)
            .put(PacketType.LIGHTING1, RFXComBindingConstants.THING_TYPE_LIGHTNING1)
            .put(PacketType.LIGHTING2, RFXComBindingConstants.THING_TYPE_LIGHTNING2)
            .put(PacketType.LIGHTING3, RFXComBindingConstants.THING_TYPE_LIGHTNING3)
            .put(PacketType.LIGHTING4, RFXComBindingConstants.THING_TYPE_LIGHTNING4)
            .put(PacketType.LIGHTING5, RFXComBindingConstants.THING_TYPE_LIGHTNING5)
            .put(PacketType.LIGHTING6, RFXComBindingConstants.THING_TYPE_LIGHTNING6)
            .put(PacketType.POWER, RFXComBindingConstants.THING_TYPE_POWER)
            .put(PacketType.RADIATOR1, RFXComBindingConstants.THING_TYPE_RADIATOR1)
            .put(PacketType.RAIN, RFXComBindingConstants.THING_TYPE_RAIN)
            .put(PacketType.REMOTE_CONTROL, RFXComBindingConstants.THING_TYPE_REMOTE_CONTROL)
            .put(PacketType.RFXMETER, RFXComBindingConstants.THING_TYPE_RFX_METER)
            .put(PacketType.RFXSENSOR, RFXComBindingConstants.THING_TYPE_RFX_SENSOR)
            .put(PacketType.RFY, RFXComBindingConstants.THING_TYPE_RFY)
            .put(PacketType.SECURITY1, RFXComBindingConstants.THING_TYPE_SECURITY1)
            .put(PacketType.SECURITY2, RFXComBindingConstants.THING_TYPE_SECURITY2)
            .put(PacketType.TEMPERATURE, RFXComBindingConstants.THING_TYPE_TEMPERATURE)
            .put(PacketType.TEMPERATURE_HUMIDITY, RFXComBindingConstants.THING_TYPE_TEMPERATURE_HUMIDITY)
            .put(PacketType.TEMPERATURE_HUMIDITY_BAROMETRIC,
                    RFXComBindingConstants.THING_TYPE_TEMPERATURE_HUMIDITY_BAROMETRIC)
            .put(PacketType.TEMPERATURE_RAIN, RFXComBindingConstants.THING_TYPE_TEMPERATURE_RAIN)
            .put(PacketType.THERMOSTAT1, RFXComBindingConstants.THING_TYPE_THERMOSTAT1)
            .put(PacketType.THERMOSTAT2, RFXComBindingConstants.THING_TYPE_THERMOSTAT2)
            .put(PacketType.THERMOSTAT3, RFXComBindingConstants.THING_TYPE_THERMOSTAT3)
            .put(PacketType.UNDECODED_RF_MESSAGE, RFXComBindingConstants.THING_TYPE_UNDECODED)
            .put(PacketType.UV, RFXComBindingConstants.THING_TYPE_UV)
            .put(PacketType.WATER, RFXComBindingConstants.THING_TYPE_WATER_USAGE)
            .put(PacketType.WEIGHT, RFXComBindingConstants.THING_TYPE_WEIGHTING_SCALE)
            .put(PacketType.WIND, RFXComBindingConstants.THING_TYPE_WIND).build();
}
