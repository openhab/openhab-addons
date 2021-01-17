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
package org.openhab.binding.wlanthermo.internal.api.esp32;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.*;
import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.TRIGGER_NONE;

import java.awt.*;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.wlanthermo.internal.WlanThermoException;
import org.openhab.binding.wlanthermo.internal.WlanThermoUnknownChannelException;
import org.openhab.binding.wlanthermo.internal.WlanThermoUtil;
import org.openhab.binding.wlanthermo.internal.api.esp32.dto.data.Data;
import org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings.Settings;
import org.openhab.core.library.types.*;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * The {@link WlanThermoEsp32CommandHandlerTest} class tests the {@link WlanThermoEsp32CommandHandler}
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
class WlanThermoEsp32CommandHandlerTest {

    private static final ThingUID THING_UID = new ThingUID("wlanthermo", "esp32", "test");

    //@formatter:off
    private static final String DATA_INPUT_JSON = "{\n" +
            "  \"system\": {\n" +
            "    \"time\": \"1610894101\",\n" +
            "    \"unit\": \"C\",\n" +
            "    \"soc\": 89,\n" +
            "    \"charge\": false,\n" +
            "    \"rssi\": -32,\n" +
            "    \"online\": 0\n" +
            "  },\n" +
            "  \"channel\": [\n" +
            "    {\n" +
            "      \"number\": 1,\n" +
            "      \"name\": \"Kanal Eins\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 23.7,\n" +
            "      \"min\": 17,\n" +
            "      \"max\": 104,\n" +
            "      \"alarm\": 1,\n" +
            "      \"color\": \"#270000\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 2,\n" +
            "      \"name\": \"Kanal 2\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#22B14C\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 3,\n" +
            "      \"name\": \"Kanal 3\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#EF562D\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 4,\n" +
            "      \"name\": \"Kanal 4\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#FFC100\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 5,\n" +
            "      \"name\": \"Kanal 5\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#A349A4\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 6,\n" +
            "      \"name\": \"Kanal 6\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#804000\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 7,\n" +
            "      \"name\": \"Kanal 7\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 23.7,\n" +
            "      \"min\": 10,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#5587A2\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 8,\n" +
            "      \"name\": \"Kanal 8\",\n" +
            "      \"typ\": 0,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#5C7148\",\n" +
            "      \"fixed\": false,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 9,\n" +
            "      \"name\": \"Kanal 9\",\n" +
            "      \"typ\": 16,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#A349A4\",\n" +
            "      \"fixed\": true,\n" +
            "      \"connected\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"number\": 10,\n" +
            "      \"name\": \"Kanal 10\",\n" +
            "      \"typ\": 16,\n" +
            "      \"temp\": 999,\n" +
            "      \"min\": 50,\n" +
            "      \"max\": 95,\n" +
            "      \"alarm\": 0,\n" +
            "      \"color\": \"#5587A2\",\n" +
            "      \"fixed\": true,\n" +
            "      \"connected\": false\n" +
            "    }\n" +
            "  ],\n" +
            "  \"pitmaster\": {\n" +
            "    \"type\": [\n" +
            "      \"off\",\n" +
            "      \"manual\",\n" +
            "      \"auto\"\n" +
            "    ],\n" +
            "    \"pm\": [\n" +
            "      {\n" +
            "        \"id\": 0,\n" +
            "        \"channel\": 1,\n" +
            "        \"pid\": 1,\n" +
            "        \"value\": 70,\n" +
            "        \"set\": 50,\n" +
            "        \"typ\": \"manual\",\n" +
            "        \"set_color\": \"#ff0000\",\n" +
            "        \"value_color\": \"#000000\"\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    private static final String SETTINGS_INPUT_JSON = "{\n" +
            "  \"device\": {\n" +
            "    \"device\": \"nano\",\n" +
            "    \"serial\": \"98f4ab7570c0\",\n" +
            "    \"cpu\": \"esp32\",\n" +
            "    \"flash_size\": 16777216,\n" +
            "    \"item\": \"n3j04oA200B\",\n" +
            "    \"hw_version\": \"v3\",\n" +
            "    \"sw_version\": \"v1.1.0\",\n" +
            "    \"api_version\": \"1\",\n" +
            "    \"language\": \"de\"\n" +
            "  },\n" +
            "  \"system\": {\n" +
            "    \"time\": \"1610894186\",\n" +
            "    \"unit\": \"C\",\n" +
            "    \"ap\": \"WLANTHERMO-AP\",\n" +
            "    \"host\": \"NANO-98f4ab7570c0\",\n" +
            "    \"language\": \"de\",\n" +
            "    \"version\": \"v1.1.0\",\n" +
            "    \"getupdate\": \"false\",\n" +
            "    \"autoupd\": true,\n" +
            "    \"prerelease\": true,\n" +
            "    \"hwversion\": \"V3\"\n" +
            "  },\n" +
            "  \"hardware\": [\n" +
            "    \"V3\"\n" +
            "  ],\n" +
            "  \"api\": {\n" +
            "    \"version\": \"1\"\n" +
            "  },\n" +
            "  \"sensors\": [\n" +
            "    {\n" +
            "      \"type\": 0,\n" +
            "      \"name\": \"1000K/Maverick\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 1,\n" +
            "      \"name\": \"Fantast-Neu\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 2,\n" +
            "      \"name\": \"Fantast\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 3,\n" +
            "      \"name\": \"100K/iGrill2\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 4,\n" +
            "      \"name\": \"ET-73\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 5,\n" +
            "      \"name\": \"Perfektion\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 6,\n" +
            "      \"name\": \"50K\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 7,\n" +
            "      \"name\": \"Inkbird\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 8,\n" +
            "      \"name\": \"100K6A1B\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 9,\n" +
            "      \"name\": \"Weber_6743\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 10,\n" +
            "      \"name\": \"Santos\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 11,\n" +
            "      \"name\": \"5K3A1B\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 12,\n" +
            "      \"name\": \"PT100\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 13,\n" +
            "      \"name\": \"PT1000\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 14,\n" +
            "      \"name\": \"ThermoWorks\",\n" +
            "      \"fixed\": false\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 15,\n" +
            "      \"name\": \"Typ K\",\n" +
            "      \"fixed\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 16,\n" +
            "      \"name\": \"Bluetooth\",\n" +
            "      \"fixed\": true\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\": 17,\n" +
            "      \"name\": \"Maverick\",\n" +
            "      \"fixed\": true\n" +
            "    }\n" +
            "  ],\n" +
            "  \"features\": {\n" +
            "    \"bluetooth\": true,\n" +
            "    \"pitmaster\": true\n" +
            "  },\n" +
            "  \"pid\": [\n" +
            "    {\n" +
            "      \"name\": \"SSR SousVide\",\n" +
            "      \"id\": 0,\n" +
            "      \"aktor\": 0,\n" +
            "      \"Kp\": 104,\n" +
            "      \"Ki\": 0.2,\n" +
            "      \"Kd\": 0,\n" +
            "      \"DCmmin\": 0,\n" +
            "      \"DCmmax\": 100,\n" +
            "      \"opl\": 0,\n" +
            "      \"SPmin\": 0,\n" +
            "      \"SPmax\": 0,\n" +
            "      \"link\": 0,\n" +
            "      \"tune\": 0,\n" +
            "      \"jp\": 100\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"TITAN 50x50\",\n" +
            "      \"id\": 1,\n" +
            "      \"aktor\": 1,\n" +
            "      \"Kp\": 7,\n" +
            "      \"Ki\": 0.01,\n" +
            "      \"Kd\": 128,\n" +
            "      \"DCmmin\": 25,\n" +
            "      \"DCmmax\": 100,\n" +
            "      \"opl\": 0,\n" +
            "      \"SPmin\": 0,\n" +
            "      \"SPmax\": 0,\n" +
            "      \"link\": 0,\n" +
            "      \"tune\": 0,\n" +
            "      \"jp\": 70\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Servo MG995\",\n" +
            "      \"id\": 2,\n" +
            "      \"aktor\": 2,\n" +
            "      \"Kp\": 104,\n" +
            "      \"Ki\": 0.2,\n" +
            "      \"Kd\": 0,\n" +
            "      \"DCmmin\": 0,\n" +
            "      \"DCmmax\": 100,\n" +
            "      \"opl\": 0,\n" +
            "      \"SPmin\": 25,\n" +
            "      \"SPmax\": 75,\n" +
            "      \"link\": 0,\n" +
            "      \"tune\": 0,\n" +
            "      \"jp\": 100\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Custom\",\n" +
            "      \"id\": 3,\n" +
            "      \"aktor\": 1,\n" +
            "      \"Kp\": 7,\n" +
            "      \"Ki\": 0.2,\n" +
            "      \"Kd\": 0,\n" +
            "      \"DCmmin\": 0,\n" +
            "      \"DCmmax\": 100,\n" +
            "      \"opl\": 0,\n" +
            "      \"SPmin\": 0,\n" +
            "      \"SPmax\": 100,\n" +
            "      \"link\": 0,\n" +
            "      \"tune\": 0,\n" +
            "      \"jp\": 100\n" +
            "    }\n" +
            "  ],\n" +
            "  \"aktor\": [\n" +
            "    \"SSR\",\n" +
            "    \"FAN\",\n" +
            "    \"SERVO\"\n" +
            "  ],\n" +
            "  \"display\": {\n" +
            "    \"updname\": \"\",\n" +
            "    \"orientation\": 0\n" +
            "  },\n" +
            "  \"iot\": {\n" +
            "    \"PMQhost\": \"192.168.2.1\",\n" +
            "    \"PMQport\": 1883,\n" +
            "    \"PMQuser\": \"\",\n" +
            "    \"PMQpass\": \"\",\n" +
            "    \"PMQqos\": 0,\n" +
            "    \"PMQon\": false,\n" +
            "    \"PMQint\": 30,\n" +
            "    \"CLon\": false,\n" +
            "    \"CLtoken\": \"thisisnotatoken\",\n" +
            "    \"CLint\": 30,\n" +
            "    \"CLurl\": \"cloud.wlanthermo.de/index.html\"\n" +
            "  },\n" +
            "  \"notes\": {\n" +
            "    \"fcm\": [],\n" +
            "    \"ext\": {\n" +
            "      \"on\": 0,\n" +
            "      \"token\": \"\",\n" +
            "      \"id\": \"\",\n" +
            "      \"repeat\": 1,\n" +
            "      \"service\": 0,\n" +
            "      \"services\": [\n" +
            "        \"telegram\",\n" +
            "        \"pushover\"\n" +
            "      ]\n" +
            "    }\n" +
            "  }\n" +
            "}";
    //@formatter:on

    @Nullable
    private Data data;
    @Nullable
    private Settings settings;

    @BeforeEach
    void setUp() {
        Gson gson = new Gson();
        data = gson.fromJson(DATA_INPUT_JSON, Data.class);
        settings = gson.fromJson(SETTINGS_INPUT_JSON, Settings.class);
    }

    static Stream<Arguments> getState() {
        return Stream.of(
                // System channels
                Arguments.of(SYSTEM, SYSTEM_SOC, new DecimalType(89), null),
                Arguments.of(SYSTEM, SYSTEM_CHARGE, OnOffType.OFF, null),
                Arguments.of(SYSTEM, SYSTEM_RSSI_SIGNALSTRENGTH, new DecimalType(4), null),
                Arguments.of(SYSTEM, SYSTEM_RSSI, new QuantityType<>(-32, Units.DECIBEL_MILLIWATTS), null),

                // All channels
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_NAME, new StringType("Kanal Eins"), null),
                Arguments.of(CHANNEL_PREFIX + "2", CHANNEL_NAME, new StringType("Kanal 2"), null),
                Arguments.of(CHANNEL_PREFIX + "3", CHANNEL_NAME, new StringType("Kanal 3"), null),
                Arguments.of(CHANNEL_PREFIX + "4", CHANNEL_NAME, new StringType("Kanal 4"), null),
                Arguments.of(CHANNEL_PREFIX + "5", CHANNEL_NAME, new StringType("Kanal 5"), null),
                Arguments.of(CHANNEL_PREFIX + "6", CHANNEL_NAME, new StringType("Kanal 6"), null),
                Arguments.of(CHANNEL_PREFIX + "7", CHANNEL_NAME, new StringType("Kanal 7"), null),
                Arguments.of(CHANNEL_PREFIX + "8", CHANNEL_NAME, new StringType("Kanal 8"), null),
                Arguments.of(CHANNEL_PREFIX + "9", CHANNEL_NAME, new StringType("Kanal 9"), null),
                Arguments.of(CHANNEL_PREFIX + "10", CHANNEL_NAME, new StringType("Kanal 10"), null),
                // invalid channel number
                Arguments.of(CHANNEL_PREFIX + "11", CHANNEL_NAME, UnDefType.UNDEF,
                        WlanThermoUnknownChannelException.class),

                // all channel values
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_NAME, new StringType("Kanal Eins"), null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_TYP, new StringType("1000K/Maverick"), null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_TEMP, new QuantityType<>(23.7, SIUnits.CELSIUS), null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_MIN, new QuantityType<>(17, SIUnits.CELSIUS), null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_MAX, new QuantityType<>(104, SIUnits.CELSIUS), null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_DEVICE, OnOffType.OFF, null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_PUSH, OnOffType.ON, null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_OPENHAB_HIGH, OnOffType.OFF, null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_OPENHAB_LOW, OnOffType.OFF, null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_COLOR,
                        HSBType.fromRGB(Color.decode("#270000").getRed(), Color.decode("#270000").getGreen(),
                                Color.decode("#270000").getBlue()),
                        null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_COLOR_NAME,
                        new StringType(WlanThermoEsp32Util.toColorName("#270000")), null),

                // all pitmaster
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_CHANNEL_ID, new DecimalType(1), null),
                Arguments.of(CHANNEL_PITMASTER_2, CHANNEL_PITMASTER_CHANNEL_ID, UnDefType.UNDEF, null),

                // all pitmaster values
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_CHANNEL_ID, new DecimalType(1), null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_PIDPROFILE, new DecimalType(1), null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_DUTY_CYCLE, new DecimalType(70), null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_SETPOINT, new QuantityType<>(50, SIUnits.CELSIUS),
                        null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_STATE, new StringType("manual"), null));
    }

    static Stream<Arguments> getTrigger() {
        return Stream.of(
                // all channels
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_OPENHAB, TRIGGER_NONE, null),
                Arguments.of(CHANNEL_PREFIX + "2", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "3", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "4", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "5", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "6", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "7", CHANNEL_ALARM_OPENHAB, TRIGGER_NONE, null),
                Arguments.of(CHANNEL_PREFIX + "8", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "9", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "10", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                // invalid channel number
                Arguments.of(CHANNEL_PREFIX + "11", CHANNEL_ALARM_OPENHAB, "",
                        WlanThermoUnknownChannelException.class));
    }

    static Stream<Arguments> setState() {
        return Stream.of(
                // All channels
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_NAME, new StringType("Kanal Eins"), true),
                Arguments.of(CHANNEL_PREFIX + "2", CHANNEL_NAME, new StringType("Kanal 2"), true),
                Arguments.of(CHANNEL_PREFIX + "3", CHANNEL_NAME, new StringType("Kanal 3"), true),
                Arguments.of(CHANNEL_PREFIX + "4", CHANNEL_NAME, new StringType("Kanal 4"), true),
                Arguments.of(CHANNEL_PREFIX + "5", CHANNEL_NAME, new StringType("Kanal 5"), true),
                Arguments.of(CHANNEL_PREFIX + "6", CHANNEL_NAME, new StringType("Kanal 6"), true),
                Arguments.of(CHANNEL_PREFIX + "7", CHANNEL_NAME, new StringType("Kanal 7"), true),
                Arguments.of(CHANNEL_PREFIX + "8", CHANNEL_NAME, new StringType("Kanal 8"), true),
                Arguments.of(CHANNEL_PREFIX + "9", CHANNEL_NAME, new StringType("Kanal 9"), true),
                Arguments.of(CHANNEL_PREFIX + "10", CHANNEL_NAME, new StringType("Kanal 10"), true),
                // invalid channel number
                Arguments.of(CHANNEL_PREFIX + "11", CHANNEL_NAME, new StringType("Kanal 11"), false),

                // all channel values
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_NAME, new StringType("Kanal Eins"), true),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_TYP, new StringType("1000K/Maverick"), false),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_TEMP, new QuantityType<>(23.7, SIUnits.CELSIUS), false),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_MIN, new QuantityType<>(17, SIUnits.CELSIUS), true),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_MAX, new QuantityType<>(104, SIUnits.CELSIUS), true),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_DEVICE, OnOffType.OFF, true),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_PUSH, OnOffType.ON, true),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_OPENHAB_HIGH, OnOffType.OFF, false),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_OPENHAB_LOW, OnOffType.OFF, false),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_COLOR,
                        HSBType.fromRGB(Color.decode("#270000").getRed(), Color.decode("#270000").getGreen(),
                                Color.decode("#270000").getBlue()),
                        true),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_COLOR_NAME,
                        new StringType(WlanThermoEsp32Util.toColorName("#270000")), true),

                // all pitmaster
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_CHANNEL_ID, new DecimalType(1), true),
                Arguments.of(CHANNEL_PITMASTER_2, CHANNEL_PITMASTER_CHANNEL_ID, new DecimalType(1), false),

                // all pitmaster values
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_CHANNEL_ID, new DecimalType(1), true),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_PIDPROFILE, new DecimalType(0), true),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_DUTY_CYCLE, new DecimalType(0), false),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_SETPOINT, new QuantityType<>(100, SIUnits.CELSIUS),
                        true),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_STATE, new StringType("off"), true));
    }

    @ParameterizedTest
    @MethodSource("getTrigger")
    void getTrigger(String groupId, String id, String expectedTrigger,
            @Nullable Class<WlanThermoException> exceptionClass) {
        Executable test = () -> Assertions.assertEquals(expectedTrigger, WlanThermoEsp32CommandHandler
                .getTrigger(new ChannelUID(THING_UID, groupId, id), WlanThermoUtil.requireNonNull(data)));
        if (exceptionClass != null) {
            Assertions.assertThrows(exceptionClass, test);
        } else {
            Assertions.assertDoesNotThrow(test);
        }
    }

    @ParameterizedTest
    @MethodSource("getState")
    void getState(String groupId, String id, State expectedState, @Nullable Class<WlanThermoException> exceptionClass) {
        Executable test = () -> Assertions.assertEquals(expectedState,
                WlanThermoEsp32CommandHandler.getState(new ChannelUID(THING_UID, groupId, id),
                        WlanThermoUtil.requireNonNull(data), WlanThermoUtil.requireNonNull(settings)));
        if (exceptionClass != null) {
            Assertions.assertThrows(exceptionClass, test);
        } else {
            Assertions.assertDoesNotThrow(test);
        }
    }

    @ParameterizedTest
    @MethodSource("setState")
    void setState(String groupId, String id, Command command, boolean expectedResult) {
        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(expectedResult, WlanThermoEsp32CommandHandler
                .setState(new ChannelUID(THING_UID, groupId, id), command, WlanThermoUtil.requireNonNull(data))));
    }
}
