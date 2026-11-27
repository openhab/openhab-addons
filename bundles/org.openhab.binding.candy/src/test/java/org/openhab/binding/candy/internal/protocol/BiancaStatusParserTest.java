/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.candy.internal.protocol;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.candy.internal.CandyBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/**
 * Status interpretation without a physical washer or account.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
class BiancaStatusParserTest {
    private final BiancaStatusParser parser = new BiancaStatusParser();

    @ParameterizedTest
    @CsvSource({ "true, ON", "TRUE, ON", "TrUe, ON", "false, OFF", "FALSE, OFF", "FaLsE, OFF" })
    void handlesBooleanTextRegardlessOfCase(String value, OnOffType expected) throws CandyProtocolException {
        var states = parser.parse("""
                {"statusLavatrice":{"MachMd":2,"WiFiStatus":"%1$s","PrNm":"%1$s","PrCode":"%1$s","Err":"%1$s"}}
                """.formatted(value)).states();
        assertEquals(expected, states.get(CHANNEL_REMOTE_CONTROL_ENABLED));
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_PROGRAM_NUMBER));
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_PROGRAM_CODE));
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_ERROR_CODE));
    }

    @ParameterizedTest
    @CsvSource({ "0, NO_STATE", "1, SELECTING", "2, RUNNING", "3, PAUSED", "4, DELAY_SELECTION", "5, DELAYED",
            "6, ERROR", "7, FINISHED" })
    void preservesMachineModeWireCodesAndChannelValues(int code, String value) throws CandyProtocolException {
        var states = parser.parse("{\"statusLavatrice\":{\"MachMd\":" + code + "}}").states();
        assertEquals(new DecimalType(code), states.get(CHANNEL_MACHINE_MODE_CODE));
        assertEquals(new StringType(value), states.get(CHANNEL_MACHINE_STATE));
    }

    @ParameterizedTest
    @CsvSource({ "0, NONE", "1, PREWASH", "2, WASH", "3, RINSE", "4, LAST_RINSE", "5, FINISHED", "6, DRYING",
            "7, ERROR", "9, GOOD_NIGHT_PAUSE" })
    void preservesProgramPhaseWireCodesAndChannelValues(int code, String value) throws CandyProtocolException {
        var states = parser.parse("{\"statusLavatrice\":{\"PrPh\":" + code + "}}").states();
        assertEquals(new DecimalType(code), states.get(CHANNEL_PROGRAM_PHASE_CODE));
        assertEquals(new StringType(value), states.get(CHANNEL_PROGRAM_PHASE));
    }

    @Test
    void preservesUnknownCodesWithoutUsingEnumOrdinals() throws CandyProtocolException {
        var states = parser.parse("""
                {"statusLavatrice":{"MachMd":8,"PrPh":8}}
                """).states();
        assertEquals(new DecimalType(8), states.get(CHANNEL_MACHINE_MODE_CODE));
        assertEquals(new DecimalType(8), states.get(CHANNEL_PROGRAM_PHASE_CODE));
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_MACHINE_STATE));
        assertEquals(UnDefType.UNDEF, states.get(CHANNEL_PROGRAM_PHASE));
    }

    @Test
    void parsesSyntheticRunningFixture() throws IOException, CandyProtocolException {
        try (InputStream input = getClass().getResourceAsStream("/bianca/running.json")) {
            assertNotNull(input);
            var states = parser.parse(new String(input.readAllBytes(), StandardCharsets.UTF_8)).states();
            assertEquals(LIVE_CHANNELS, states.keySet());
            assertEquals(new StringType("RUNNING"), states.get(CHANNEL_MACHINE_STATE));
            assertEquals(new StringType("WASH"), states.get(CHANNEL_PROGRAM_PHASE));
            assertEquals(new StringType("003"), states.get(CHANNEL_PROGRAM_NUMBER));
            assertEquals(new StringType("12"), states.get(CHANNEL_PROGRAM_CODE));
            assertEquals(new QuantityType<>(1800, Units.SECOND), states.get(CHANNEL_REMAINING_TIME));
            assertEquals(new QuantityType<>(40, SIUnits.CELSIUS), states.get(CHANNEL_TARGET_TEMPERATURE));
            assertEquals(new QuantityType<>(1300, Units.RPM), states.get(CHANNEL_TARGET_SPIN_SPEED));
            assertEquals(OnOffType.ON, states.get(CHANNEL_REMOTE_CONTROL_ENABLED));
            assertEquals(new StringType("0"), states.get(CHANNEL_ERROR_CODE));
            assertThrows(UnsupportedOperationException.class, () -> states.clear());
        }
    }

    @Test
    void preservesZerosAndAlternativePrimitiveForms() throws CandyProtocolException {
        var states = parser
                .parse("""
                        {"statusLavatrice":{"MachMd":"0","PrPh":0,"Temp":0,"SpinSp":0,"RemTime":0,"WiFiStatus":false,"Err":"E01"}}
                        """)
                .states();
        assertEquals(new StringType("NO_STATE"), states.get(CHANNEL_MACHINE_STATE));
        assertEquals(new QuantityType<>(0, Units.SECOND), states.get(CHANNEL_REMAINING_TIME));
        assertEquals(new QuantityType<>(0, SIUnits.CELSIUS), states.get(CHANNEL_TARGET_TEMPERATURE));
        assertEquals(new QuantityType<>(0, Units.RPM), states.get(CHANNEL_TARGET_SPIN_SPEED));
        assertEquals(OnOffType.OFF, states.get(CHANNEL_REMOTE_CONTROL_ENABLED));
        assertEquals(new StringType("E01"), states.get(CHANNEL_ERROR_CODE));
    }

    @Test
    void handlesUnknownMalformedMissingAndSentinelValues() throws CandyProtocolException {
        var states = parser.parse("""
                {"statusLavatrice":{"MachMd":99,"PrPh":255,"Temp":255,"SpinSp":255,"RemTime":60000,
                "WiFiStatus":"unexpected","PrNm":{},"Err":null,"additional":"ignored"}}
                """).states();
        assertEquals(new DecimalType(99), states.get(CHANNEL_MACHINE_MODE_CODE));
        assertEquals(new DecimalType(255), states.get(CHANNEL_PROGRAM_PHASE_CODE));
        for (String channel : LIVE_CHANNELS) {
            if (!CHANNEL_MACHINE_MODE_CODE.equals(channel) && !CHANNEL_PROGRAM_PHASE_CODE.equals(channel)) {
                assertEquals(UnDefType.UNDEF, states.get(channel), channel);
            }
        }
        var partial = parser.parse("""
                {"statusLavatrice":{"MachMd":2,"Temp":-1,"SpinSp":-1,"RemTime":"9999999999999999999"}}
                """).states();
        assertEquals(UnDefType.UNDEF, partial.get(CHANNEL_REMAINING_TIME));
        assertEquals(UnDefType.UNDEF, partial.get(CHANNEL_TARGET_TEMPERATURE));
        assertEquals(UnDefType.UNDEF, partial.get(CHANNEL_TARGET_SPIN_SPEED));
        assertEquals(UnDefType.UNDEF, partial.get(CHANNEL_REMOTE_CONTROL_ENABLED));
    }

    @Test
    void retainsDirectRpmAndLongDuration() throws CandyProtocolException {
        var states = parser.parse("""
                {"statusLavatrice":{"MachMd":5,"PrPh":9,"SpinSp":1300,"RemTime":61000}}
                """).states();
        assertEquals(new QuantityType<>(1300, Units.RPM), states.get(CHANNEL_TARGET_SPIN_SPEED));
        assertEquals(new QuantityType<>(61000, Units.SECOND), states.get(CHANNEL_REMAINING_TIME));
        assertEquals(new StringType("DELAYED"), states.get(CHANNEL_MACHINE_STATE));
        assertEquals(new StringType("GOOD_NIGHT_PAUSE"), states.get(CHANNEL_PROGRAM_PHASE));
    }

    @Test
    void rejectsInvalidResponsesWithoutLeakingTheirContents() {
        for (String json : List.of("", "{", "null", "[]", "{}", "{\"statusLavatrice\":[]}", "{\"statusLavatrice\":{}}",
                "{\"statusLavatrice\":{\"unknown\":1}}", "{statusLavatrice:{MachMd:2}}",
                "{\"statusLavatrice\":{\"MachMd\":2}} trailing",
                "{\"appliance\":{\"current_status_parameters\":{\"MachMd\":2}}}")) {
            CandyProtocolException exception = assertThrows(CandyProtocolException.class, () -> parser.parse(json));
            assertNull(exception.getCause());
        }
    }
}
