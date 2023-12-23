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
package org.openhab.binding.infokeydinrail.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link InfokeyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Themistoklis Anastasopoulos - Initial contribution
 */
@NonNullByDefault
public class InfokeyBindingConstants {

    private static final String BINDING_ID = "infokey";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_RELAY_OPTO_DIN_V1 = new ThingTypeUID(BINDING_ID, "relayoptodinv1");
    public static final ThingTypeUID THING_TYPE_OPTO_DIN_V1 = new ThingTypeUID(BINDING_ID, "optodinv1");
    public static final ThingTypeUID THING_TYPE_MOSFET_DIN_V1 = new ThingTypeUID(BINDING_ID, "mosfetdinv1");
    public static final ThingTypeUID THING_TYPE_2COIL_RELAY_DIN_V1 = new ThingTypeUID(BINDING_ID, "2coilrelaydinv1");
    public static final ThingTypeUID THING_TYPE_MCP3008_DIN_V1 = new ThingTypeUID(BINDING_ID, "mcp3008dinv1");
    public static final ThingTypeUID THING_TYPE_DHT = new ThingTypeUID(BINDING_ID, "dhtmodule");
    public static final ThingTypeUID THING_TYPE_WATER_FLOWMETER = new ThingTypeUID(BINDING_ID, "waterflowmeteryfs201");

    // supported thing types for discovery
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(Arrays.asList(
            THING_TYPE_RELAY_OPTO_DIN_V1, THING_TYPE_OPTO_DIN_V1, THING_TYPE_MOSFET_DIN_V1,
            THING_TYPE_2COIL_RELAY_DIN_V1, THING_TYPE_MCP3008_DIN_V1, THING_TYPE_DHT, THING_TYPE_WATER_FLOWMETER));

    public static final String DEFAULT_STATE = "default_state";
    public static final String ADDRESS = "address";
    public static final String BUS_NUMBER = "bus_number";
    public static final String PIN = "pin";
    public static final String PULL_MODE = "pull_mode";
    public static final String DEFAULT_PULL_MODE = "OFF";
    public static final String DEFAULT_MIN_VALUE_CHANGE = "10";
    public static final String DEFAULT_VALUE_RENDER_TYPE = "RAW";
    public static final String PULSE_DURATION = "pulse_duration";
    public static final String INPUT_DEFAULT_OPEN_STATE = "default_open_state";

    public static final String POLLING_INTERVAL = "polling_interval";
    public static final String VALUE_RENDERER_TYPE = "value_renderer_type";
    public static final String MIN_VALUE_CHANGE = "min_value_change";

    public static final String SPI_CHANNEL = "spi_channel";
    public static final String SPI_CHIP_SELECT = "spi_chip_select";

    public static final String DHT_SERVER_IP = "dht_server_ip";
    public static final String DHT_POLLING_INTERVAL = "dht_polling_interval";
    public static final String DHT_DATA_PIN = "dht_data_pin";
    public static final String DHT_MODEL = "dht_model";

    public static final String ACTIVE_LOW = "active_low";

    public static final String ACTIVE_LOW_ENABLED = "y";

    public static final String CHANNEL_GROUP_INPUT = "input";
    public static final String CHANNEL_GROUP_OUTPUT = "output";
    public static final String CHANNEL_GROUP_PULSE = "pulseoutput";
    public static final String CHANNEL_GROUP_ANALOG_INPUT = "analoginput";
    public static final String CHANNEL_GROUP_DHT_INPUT = "dht_inputs";

    public static final String WATER_FLOWMETER_PIN = "water_flowmeter_pin";
    public static final String WATER_FLOWMETER_L = "water_flowmeter_l";

    public static final Set<String> SUPPORTED_CHANNEL_GROUPS = Stream.of(CHANNEL_GROUP_INPUT, CHANNEL_GROUP_OUTPUT,
            CHANNEL_GROUP_PULSE, CHANNEL_GROUP_ANALOG_INPUT, CHANNEL_GROUP_DHT_INPUT).collect(Collectors.toSet());

    public static final String CHANNEL_A0 = "A0";
    public static final String CHANNEL_A1 = "A1";
    public static final String CHANNEL_A2 = "A2";
    public static final String CHANNEL_A3 = "A3";
    public static final String CHANNEL_A4 = "A4";
    public static final String CHANNEL_A5 = "A5";
    public static final String CHANNEL_A6 = "A6";
    public static final String CHANNEL_A7 = "A7";
    public static final String CHANNEL_B0 = "B0";
    public static final String CHANNEL_B1 = "B1";
    public static final String CHANNEL_B2 = "B2";
    public static final String CHANNEL_B3 = "B3";
    public static final String CHANNEL_B4 = "B4";
    public static final String CHANNEL_B5 = "B5";
    public static final String CHANNEL_B6 = "B6";
    public static final String CHANNEL_B7 = "B7";

    public static final String CHANNEL_0 = "CH0";
    public static final String CHANNEL_1 = "CH1";
    public static final String CHANNEL_2 = "CH2";
    public static final String CHANNEL_3 = "CH3";
    public static final String CHANNEL_4 = "CH4";
    public static final String CHANNEL_5 = "CH5";
    public static final String CHANNEL_6 = "CH6";
    public static final String CHANNEL_7 = "CH7";

    public static final Set<String> SUPPORTED_CHANNELS = Stream
            .of(CHANNEL_A0, CHANNEL_A1, CHANNEL_A2, CHANNEL_A3, CHANNEL_A4, CHANNEL_A5, CHANNEL_A6, CHANNEL_A7,
                    CHANNEL_B0, CHANNEL_B1, CHANNEL_B2, CHANNEL_B3, CHANNEL_B4, CHANNEL_B5, CHANNEL_B6, CHANNEL_B7)
            .collect(Collectors.toSet());

    public static final Set<String> MCP3008_SUPPORTED_CHANNELS = Stream
            .of(CHANNEL_0, CHANNEL_1, CHANNEL_2, CHANNEL_3, CHANNEL_4, CHANNEL_5, CHANNEL_6, CHANNEL_7)
            .collect(Collectors.toSet());
}
