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
package org.openhab.binding.wlanthermo.internal.api.mini;

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
import org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin.App;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * The {@link WlanThermoMiniCommandHandlerTest} class tests the {@link WlanThermoMiniCommandHandler}
 *
 * @author Christian Schlipp - Initial contribution
 */
@NonNullByDefault
class WlanThermoMiniCommandHandlerTest {

    private static final ThingUID THING_UID = new ThingUID("wlanthermo", "mini", "test");

    @Nullable
    private App app;

    @BeforeEach
    void setUp() {
        ClassLoader classLoader = Objects.requireNonNull(WlanThermoMiniCommandHandlerTest.class.getClassLoader());
        InputStream stream = Objects.requireNonNull(classLoader.getResourceAsStream("mini/app.json"));
        app = new Gson().fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), App.class);
    }

    static Stream<Arguments> getState() {
        return Stream.of(
                // System channels
                Arguments.of(SYSTEM, SYSTEM_CPU_TEMP, new DecimalType(93.56), null),
                Arguments.of(SYSTEM, SYSTEM_CPU_LOAD, new DecimalType(94.267515923567), null),

                // all channels
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_NAME, new StringType("Kanal0"), null),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_NAME, new StringType("Kanal1"), null),
                Arguments.of(CHANNEL_PREFIX + "2", CHANNEL_NAME, new StringType("Kanal2"), null),
                Arguments.of(CHANNEL_PREFIX + "3", CHANNEL_NAME, new StringType("Kanal3"), null),
                Arguments.of(CHANNEL_PREFIX + "4", CHANNEL_NAME, new StringType("Kanal4"), null),
                Arguments.of(CHANNEL_PREFIX + "5", CHANNEL_NAME, new StringType("Kanal5"), null),
                Arguments.of(CHANNEL_PREFIX + "6", CHANNEL_NAME, new StringType("Kanal6"), null),
                Arguments.of(CHANNEL_PREFIX + "7", CHANNEL_NAME, new StringType("Kanal7"), null),
                Arguments.of(CHANNEL_PREFIX + "8", CHANNEL_NAME, new StringType("Kanal8 - Maverick 1"), null),
                Arguments.of(CHANNEL_PREFIX + "9", CHANNEL_NAME, new StringType("Kanal9 - Maverick 2"), null),
                // invalid channel number
                Arguments.of(CHANNEL_PREFIX + "10", CHANNEL_NAME, UnDefType.UNDEF,
                        WlanThermoUnknownChannelException.class),

                // all channel values
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_NAME, new StringType("Kanal0"), null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_TEMP, new QuantityType<>(78.28, ImperialUnits.FAHRENHEIT),
                        null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_MIN, new QuantityType<>(-20, ImperialUnits.FAHRENHEIT),
                        null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_MAX, new QuantityType<>(200, ImperialUnits.FAHRENHEIT),
                        null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_ALARM_DEVICE, OnOffType.from("false"), null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_ALARM_OPENHAB_HIGH, OnOffType.OFF, null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_ALARM_OPENHAB_LOW, OnOffType.OFF, null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_COLOR,
                        HSBType.fromRGB(Color.decode(WlanThermoMiniUtil.toHex("green")).getRed(),
                                Color.decode(WlanThermoMiniUtil.toHex("green")).getGreen(),
                                Color.decode(WlanThermoMiniUtil.toHex("green")).getBlue()),
                        null),
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_COLOR_NAME, new StringType("green"), null),

                // all pitmaster
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_ENABLED, OnOffType.from(true), null),
                Arguments.of(CHANNEL_PITMASTER_2, CHANNEL_PITMASTER_ENABLED, UnDefType.UNDEF, null),

                // all pitmaster values
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_ENABLED, OnOffType.from(true), null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_CURRENT, new DecimalType(77.86), null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_SETPOINT,
                        new QuantityType<>(110, ImperialUnits.FAHRENHEIT), null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_DUTY_CYCLE, new DecimalType(100), null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_LID_OPEN, OnOffType.OFF, null),
                Arguments.of(CHANNEL_PITMASTER_1, CHANNEL_PITMASTER_CHANNEL_ID, new DecimalType(0), null));
    }

    static Stream<Arguments> getTrigger() {
        return Stream.of(
                // all channels
                Arguments.of(CHANNEL_PREFIX + "0", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "1", CHANNEL_ALARM_OPENHAB, TRIGGER_NONE, null),
                Arguments.of(CHANNEL_PREFIX + "2", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "3", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "4", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "5", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "6", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "7", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "8", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                Arguments.of(CHANNEL_PREFIX + "9", CHANNEL_ALARM_OPENHAB, "", WlanThermoUnknownChannelException.class),
                // invalid channel number
                Arguments.of(CHANNEL_PREFIX + "10", CHANNEL_ALARM_OPENHAB, "",
                        WlanThermoUnknownChannelException.class));
    }

    @ParameterizedTest
    @MethodSource("getTrigger")
    void getTrigger(String groupId, String id, String expectedTrigger,
            @Nullable Class<WlanThermoException> exceptionClass) {
        Executable test = () -> Assertions.assertEquals(expectedTrigger, WlanThermoMiniCommandHandler
                .getTrigger(new ChannelUID(THING_UID, groupId, id), WlanThermoUtil.requireNonNull(app)));
        if (exceptionClass != null) {
            Assertions.assertThrows(exceptionClass, test);
        } else {
            Assertions.assertDoesNotThrow(test);
        }
    }

    @ParameterizedTest
    @MethodSource("getState")
    void getState(String groupId, String id, State expectedState, @Nullable Class<WlanThermoException> exceptionClass) {
        Executable test = () -> Assertions.assertEquals(expectedState, WlanThermoMiniCommandHandler
                .getState(new ChannelUID(THING_UID, groupId, id), WlanThermoUtil.requireNonNull(app)));
        if (exceptionClass != null) {
            Assertions.assertThrows(exceptionClass, test);
        } else {
            Assertions.assertDoesNotThrow(test);
        }
    }
}
