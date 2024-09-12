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
package org.openhab.binding.onewire.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.onewire.internal.device.OwChannelConfig;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link OwBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class OwBindingConstants {
    public static final String BINDING_ID = "onewire";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OWSERVER = new ThingTypeUID(BINDING_ID, "owserver");
    public static final ThingTypeUID THING_TYPE_MS_TX = new ThingTypeUID(BINDING_ID, "ms-tx");
    public static final ThingTypeUID THING_TYPE_BMS = new ThingTypeUID(BINDING_ID, "bms");
    public static final ThingTypeUID THING_TYPE_AMS = new ThingTypeUID(BINDING_ID, "ams");
    public static final ThingTypeUID THING_TYPE_BASIC = new ThingTypeUID(BINDING_ID, "basic");
    public static final ThingTypeUID THING_TYPE_EDS_ENV = new ThingTypeUID(BINDING_ID, "edsenv");
    public static final ThingTypeUID THING_TYPE_BAE091X = new ThingTypeUID(BINDING_ID, "bae091x");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_OWSERVER, THING_TYPE_AMS, THING_TYPE_BMS, THING_TYPE_MS_TX,
                    THING_TYPE_EDS_ENV, THING_TYPE_BASIC, THING_TYPE_BAE091X).collect(Collectors.toSet()));

    // List of all config options
    public static final String CONFIG_ADDRESS = "network-address";
    public static final String CONFIG_PORT = "port";

    public static final String CONFIG_ID = "id";
    public static final String CONFIG_RESOLUTION = "resolution";
    public static final String CONFIG_IGNORE_POR = "ignorepor";
    public static final String CONFIG_REFRESH = "refresh";
    public static final String CONFIG_DIGITALREFRESH = "digitalrefresh";
    public static final String CONFIG_OFFSET = "offset";
    public static final String CONFIG_HUMIDITY = "humiditytype";
    public static final String CONFIG_DIGITAL_MODE = "mode";
    public static final String CONFIG_DIGITAL_LOGIC = "logic";
    public static final String CONFIG_TEMPERATURESENSOR = "temperaturesensor";
    public static final String CONFIG_LIGHTSENSOR = "lightsensor";
    public static final String CONFIG_BAE_PIN_DISABLED = "disabled";
    public static final String CONFIG_BAE_PIN_PIO = "pio";
    public static final String CONFIG_BAE_PIN_COUNTER = "counter";
    public static final String CONFIG_BAE_PIN_PWM = "pwm";
    public static final String CONFIG_BAE_PIN_ANALOG = "analog";
    public static final String CONFIG_BAE_PIN_IN = "input";
    public static final String CONFIG_BAE_PIN_OUT = "output";

    // list of all properties
    public static final String PROPERTY_MODELID = "modelId";
    public static final String PROPERTY_VENDOR = "vendor";
    public static final String PROPERTY_SENSORCOUNT = "sensorCount";
    public static final String PROPERTY_PROD_DATE = "prodDate";
    public static final String PROPERTY_HW_REVISION = "hwRevision";

    // List of all channel ids
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_ABSOLUTE_HUMIDITY = "absolutehumidity";
    public static final String CHANNEL_DEWPOINT = "dewpoint";
    public static final String CHANNEL_PRESENT = "present";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_LIGHT = "light";
    public static final String CHANNEL_SUPPLYVOLTAGE = "supplyvoltage";
    public static final String CHANNEL_VOLTAGE = "voltage";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_DIGITAL = "digital";
    public static final String CHANNEL_DIGITAL0 = "digital0";
    public static final String CHANNEL_DIGITAL1 = "digital1";
    public static final String CHANNEL_DIGITAL2 = "digital2";
    public static final String CHANNEL_DIGITAL3 = "digital3";
    public static final String CHANNEL_DIGITAL4 = "digital4";
    public static final String CHANNEL_DIGITAL5 = "digital5";
    public static final String CHANNEL_DIGITAL6 = "digital6";
    public static final String CHANNEL_DIGITAL7 = "digital7";
    public static final String CHANNEL_DIGITAL8 = "digital8";
    public static final String CHANNEL_COUNTER = "counter";
    public static final String CHANNEL_COUNTER0 = "counter0";
    public static final String CHANNEL_COUNTER1 = "counter1";
    public static final String CHANNEL_PWM_DUTY1 = "pwmduty1";
    public static final String CHANNEL_PWM_DUTY2 = "pwmduty2";
    public static final String CHANNEL_PWM_DUTY3 = "pwmduty3";
    public static final String CHANNEL_PWM_DUTY4 = "pwmduty4";
    public static final String CHANNEL_PWM_FREQ1 = "pwmfreq1";
    public static final String CHANNEL_PWM_FREQ2 = "pwmfreq2";

    public static final ChannelTypeUID CHANNEL_TYPE_UID_ABSHUMIDITY = new ChannelTypeUID(BINDING_ID, "abshumidity");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_COUNTER = new ChannelTypeUID(BINDING_ID, "counter");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_CURRENT = new ChannelTypeUID(BINDING_ID, "current");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DEWPOINT = new ChannelTypeUID(BINDING_ID, "dewpoint");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_DIO = new ChannelTypeUID(BINDING_ID, "dio");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_HUMIDITY = new ChannelTypeUID(BINDING_ID, "humidity");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_HUMIDITYCONF = new ChannelTypeUID(BINDING_ID, "humidityconf");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_LIGHT = new ChannelTypeUID(BINDING_ID, "light");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_PRESENT = new ChannelTypeUID(BINDING_ID, "present");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_PRESSURE = new ChannelTypeUID(BINDING_ID, "pressure");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_TEMPERATURE = new ChannelTypeUID(BINDING_ID, "temperature");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_TEMPERATURE_POR = new ChannelTypeUID(BINDING_ID,
            "temperature-por");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_TEMPERATURE_POR_RES = new ChannelTypeUID(BINDING_ID,
            "temperature-por-res");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_VOLTAGE = new ChannelTypeUID(BINDING_ID, "voltage");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_ANALOG = new ChannelTypeUID(BINDING_ID, "bae-analog");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_COUNTER = new ChannelTypeUID(BINDING_ID, "bae-counter");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_DIGITAL_OUT = new ChannelTypeUID(BINDING_ID, "bae-do");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_DIN = new ChannelTypeUID(BINDING_ID, "bae-in");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_DOUT = new ChannelTypeUID(BINDING_ID, "bae-out");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_PIO = new ChannelTypeUID(BINDING_ID, "bae-pio");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_PWM_DUTY = new ChannelTypeUID(BINDING_ID, "bae-pwm-duty");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_BAE_PWM_FREQUENCY = new ChannelTypeUID(BINDING_ID,
            "bae-pwm-frequency");

    public static final ChannelTypeUID CHANNEL_TYPE_UID_OWFS_NUMBER = new ChannelTypeUID(BINDING_ID, "owfs-number");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_OWFS_STRING = new ChannelTypeUID(BINDING_ID, "owfs-string");

    // Maps for Discovery
    public static final Map<OwSensorType, ThingTypeUID> THING_TYPE_MAP;
    public static final Map<OwSensorType, String> THING_LABEL_MAP;
    public static final Map<OwSensorType, Set<OwChannelConfig>> SENSOR_TYPE_CHANNEL_MAP;

    static {
        Map<String, String> properties = Util.readPropertiesFile("sensor.properties");
        THING_TYPE_MAP = properties.entrySet().stream().filter(e -> e.getKey().endsWith(".thingtype"))
                .collect(Collectors.toConcurrentMap(e -> OwSensorType.valueOf(e.getKey().split("\\.")[0]),
                        e -> new ThingTypeUID(BINDING_ID, e.getValue())));
        SENSOR_TYPE_CHANNEL_MAP = properties.entrySet().stream().filter(e -> e.getKey().endsWith(".channels"))
                .collect(
                        Collectors.toConcurrentMap(e -> OwSensorType.valueOf(e.getKey().split("\\.")[0]),
                                e -> !e.getValue().isEmpty() ? Stream.of(e.getValue().split(","))
                                        .map(OwChannelConfig::fromString).collect(Collectors.toSet())
                                        : new HashSet<>()));
        THING_LABEL_MAP = properties.entrySet().stream().filter(e -> e.getKey().endsWith(".label")).collect(
                Collectors.toConcurrentMap(e -> OwSensorType.valueOf(e.getKey().split("\\.")[0]), e -> e.getValue()));
    }
}
