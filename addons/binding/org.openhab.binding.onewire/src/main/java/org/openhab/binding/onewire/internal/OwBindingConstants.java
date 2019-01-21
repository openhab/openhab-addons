/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.onewire.internal.device.DS18x20;
import org.openhab.binding.onewire.internal.device.DS1923;
import org.openhab.binding.onewire.internal.device.DS2401;
import org.openhab.binding.onewire.internal.device.DS2405;
import org.openhab.binding.onewire.internal.device.DS2406_DS2413;
import org.openhab.binding.onewire.internal.device.DS2408;
import org.openhab.binding.onewire.internal.device.DS2423;
import org.openhab.binding.onewire.internal.device.DS2438;
import org.openhab.binding.onewire.internal.device.EDS006x;
import org.openhab.binding.onewire.internal.device.OwChannelConfig;
import org.openhab.binding.onewire.internal.device.OwSensorType;

/**
 * The {@link OneWireBinding} class defines common constants, which are
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

    // TODO: remove after 0.11.0 release & cleanup
    public static final ThingTypeUID THING_TYPE_MS_TH = new ThingTypeUID(BINDING_ID, "ms-th");
    public static final ThingTypeUID THING_TYPE_MS_TV = new ThingTypeUID(BINDING_ID, "ms-tv");
    public static final ThingTypeUID THING_TYPE_COUNTER = new ThingTypeUID(BINDING_ID, "counter");
    public static final ThingTypeUID THING_TYPE_COUNTER2 = new ThingTypeUID(BINDING_ID, "counter2");
    public static final ThingTypeUID THING_TYPE_DIGITALIO = new ThingTypeUID(BINDING_ID, "digitalio");
    public static final ThingTypeUID THING_TYPE_DIGITALIO2 = new ThingTypeUID(BINDING_ID, "digitalio2");
    public static final ThingTypeUID THING_TYPE_DIGITALIO8 = new ThingTypeUID(BINDING_ID, "digitalio8");
    public static final ThingTypeUID THING_TYPE_IBUTTON = new ThingTypeUID(BINDING_ID, "ibutton");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(Arrays.asList(THING_TYPE_OWSERVER,
            THING_TYPE_TEMPERATURE, THING_TYPE_IBUTTON, THING_TYPE_DIGITALIO, THING_TYPE_DIGITALIO2,
            THING_TYPE_DIGITALIO8, THING_TYPE_AMS, THING_TYPE_BMS, THING_TYPE_MS_TH, THING_TYPE_MS_TX, THING_TYPE_MS_TV,
            THING_TYPE_EDS_ENV, THING_TYPE_COUNTER, THING_TYPE_COUNTER2, THING_TYPE_BASIC));

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
    public static final String CHANNEL_COUNTER = "counter";
    public static final String CHANNEL_COUNTER0 = "counter0";
    public static final String CHANNEL_COUNTER1 = "counter1";

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

    public static final ChannelTypeUID CHANNEL_TYPE_UID_OWFS_NUMBER = new ChannelTypeUID(BINDING_ID, "owfs-number");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_OWFS_STRING = new ChannelTypeUID(BINDING_ID, "owfs-string");

    // Maps for Discovery
    public static final Map<OwSensorType, ThingTypeUID> THING_TYPE_MAP = Collections.unmodifiableMap(Stream
            .of(new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS1420, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS18B20, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS18S20, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS1822, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS1923, THING_TYPE_MS_TX),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS2401, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS2405, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS2406, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS2408, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS2413, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS2423, THING_TYPE_BASIC),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.DS2438, THING_TYPE_MS_TX),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.AMS, THING_TYPE_AMS),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.AMS_S, THING_TYPE_AMS),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.BMS, THING_TYPE_BMS),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.BMS_S, THING_TYPE_BMS),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.EDS0064, THING_TYPE_EDS_ENV),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.EDS0065, THING_TYPE_EDS_ENV),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.EDS0066, THING_TYPE_EDS_ENV),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.EDS0067, THING_TYPE_EDS_ENV),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.EDS0068, THING_TYPE_EDS_ENV),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.MS_TC, THING_TYPE_MS_TX),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.MS_TH, THING_TYPE_MS_TX),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.MS_TL, THING_TYPE_MS_TX),
                    new SimpleEntry<OwSensorType, ThingTypeUID>(OwSensorType.MS_TV, THING_TYPE_MS_TX))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    public static final Map<OwSensorType, String> THING_LABEL_MAP = Collections.unmodifiableMap(Stream
            .of(new SimpleEntry<OwSensorType, String>(OwSensorType.DS1420, "1kb EEPROM"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS18B20, "Temperature Sensor"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS18S20, "Temperature Sensor"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS1822, "Temperature Sensor"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS1923, "Multisensor"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS2401, "iButton"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS2405, "Single Digital I/O"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS2406, "Dual Digital I/O"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS2408, "Octal Digital I/O"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS2413, "Dual Digital I/O"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS2423, "Dual Counter"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.DS2438, "Multisensor (generic)"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.AMS, "Multisensor AMS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.AMS_S, "Multisensor AMS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.BMS, "Multisensor BMS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.BMS_S, "Multisensor BMS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.EDS0064, "Multisensor EDS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.EDS0065, "Multisensor EDS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.EDS0066, "Multisensor EDS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.EDS0067, "Multisensor EDS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.EDS0068, "Multisensor EDS"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.MS_TC, "Multisensor TC"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.MS_TH, "Multisensor TH"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.MS_TL, "Multisensor TL"),
                    new SimpleEntry<OwSensorType, String>(OwSensorType.MS_TV, "Multisensor TV"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    public static final Map<String, String> ACCEPTED_ITEM_TYPES_MAP = Collections.unmodifiableMap(Stream
            .of(new SimpleEntry<String, String>(CHANNEL_HUMIDITY, "Number:Dimensionless"),
                    new SimpleEntry<String, String>(CHANNEL_ABSOLUTE_HUMIDITY, "Number:Density"),
                    new SimpleEntry<String, String>(CHANNEL_DEWPOINT, "Number:Temperature"),
                    new SimpleEntry<String, String>(CHANNEL_TEMPERATURE, "Number:Temperature"),
                    new SimpleEntry<String, String>(CHANNEL_LIGHT, "Number:Illuminance"),
                    new SimpleEntry<String, String>(CHANNEL_PRESSURE, "Number:Pressure"),
                    new SimpleEntry<String, String>(CHANNEL_VOLTAGE, "Number:ElectricPotential"),
                    new SimpleEntry<String, String>(CHANNEL_SUPPLYVOLTAGE, "Number:ElectricPotential"),
                    new SimpleEntry<String, String>(CHANNEL_CURRENT, "Number:ElectricCurrent"),
                    new SimpleEntry<String, String>(CHANNEL_COUNTER, "Number"),
                    new SimpleEntry<String, String>(CHANNEL_COUNTER0, "Number"),
                    new SimpleEntry<String, String>(CHANNEL_COUNTER1, "Number"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL0, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL1, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL2, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL3, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL4, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL5, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL6, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_DIGITAL7, "Switch"),
                    new SimpleEntry<String, String>(CHANNEL_PRESENT, "Switch"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    public static final Map<OwSensorType, Set<OwChannelConfig>> SENSOR_TYPE_CHANNEL_MAP = Collections.unmodifiableMap(
            Stream.of(new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.AMS, DS2438.CHANNELS_AMS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.AMS_S, DS2438.CHANNELS_AMS_S),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.BMS, DS2438.CHANNELS_BMS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.BMS_S, DS2438.CHANNELS_BMS_S),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS1420, DS2401.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS18B20, DS18x20.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS18S20, DS18x20.CHANNELS_DS18S20),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS1822, DS18x20.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS1923, DS1923.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS2401, DS2401.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS2405, DS2405.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS2406, DS2406_DS2413.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS2413, DS2406_DS2413.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS2408, DS2408.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS2423, DS2423.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.DS2438, DS2438.CHANNELS),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.EDS0064, EDS006x.CHANNELS_EDS0064),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.EDS0065, EDS006x.CHANNELS_EDS0065),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.EDS0066, EDS006x.CHANNELS_EDS0066),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.EDS0067, EDS006x.CHANNELS_EDS0067),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.EDS0068, EDS006x.CHANNELS_EDS0068),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.MS_TC, DS2438.CHANNELS_MS_TC),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.MS_TH, DS2438.CHANNELS_MS_TH),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.MS_TL, DS2438.CHANNELS_MS_TL),
                    new SimpleEntry<OwSensorType, Set<OwChannelConfig>>(OwSensorType.MS_TV, DS2438.CHANNELS_MS_TV))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
}
