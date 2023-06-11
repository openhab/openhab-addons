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
package org.openhab.binding.wlanthermo.internal.api.esp32;

import static org.openhab.binding.wlanthermo.internal.WlanThermoBindingConstants.*;

import java.awt.Color;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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

    @Nullable
    private Data data;
    @Nullable
    private Settings settings;

    @BeforeEach
    void setUp() {
        Gson gson = new Gson();
        ClassLoader classLoader = Objects.requireNonNull(WlanThermoEsp32CommandHandlerTest.class.getClassLoader());
        InputStream dataStream = Objects.requireNonNull(classLoader.getResourceAsStream("esp32/data.json"));
        InputStream settingsStream = Objects.requireNonNull(classLoader.getResourceAsStream("esp32/settings.json"));
        data = gson.fromJson(new InputStreamReader(dataStream, StandardCharsets.UTF_8), Data.class);
        settings = gson.fromJson(new InputStreamReader(settingsStream, StandardCharsets.UTF_8), Settings.class);
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
        Assertions.assertDoesNotThrow(() -> Assertions.assertEquals(expectedResult,
                WlanThermoEsp32CommandHandler.setState(new ChannelUID(THING_UID, groupId, id), command,
                        WlanThermoUtil.requireNonNull(data), WlanThermoUtil.requireNonNull(settings))));
    }
}
