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
package org.openhab.binding.solarman.internal.sunsynk;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Compares Java observations to the actual pinned Python sensor implementation.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
class SunsynkProfileTest {
    static Stream<JsonNode> oracle() throws IOException {
        try (InputStream stream = Objects
                .requireNonNull(SunsynkProfileTest.class.getResourceAsStream("/sunsynk/oracle.json"))) {
            JsonNode data = Objects.requireNonNull(new ObjectMapper().readTree(stream));
            return StreamSupport.stream(data.spliterator(), false).toList().stream();
        }
    }

    static Stream<String> profiles() {
        return SolarmanBindingConstants.SUNSYNK_PROFILES.values().stream();
    }

    static Stream<JsonNode> writeOracle() throws IOException {
        try (InputStream stream = Objects
                .requireNonNull(SunsynkProfileTest.class.getResourceAsStream("/sunsynk/write-oracle.json"))) {
            JsonNode data = Objects.requireNonNull(new ObjectMapper().readTree(stream));
            return StreamSupport.stream(data.spliterator(), false).toList().stream();
        }
    }

    @ParameterizedTest(name = "{index}: Python control encoding")
    @MethodSource("writeOracle")
    void matchesPythonDiscreteAndTimeWrites(JsonNode fixture) throws IOException {
        SunsynkProfile profile = new SunsynkProfile(fixture.path("profile").asText());
        SunsynkSensor sensor = find(profile, fixture.path("sensor").asText());
        String value = fixture.path("command").asText();
        org.openhab.core.types.Command command = switch (sensor.kind()) {
            case "SwitchRWSensor" -> "ON".equals(value) ? OnOffType.ON : OnOffType.OFF;
            case "SystemTimeRWSensor" -> new DateTimeType(value);
            default -> new StringType(value);
        };
        Map<String, State> states = new HashMap<>();
        for (String limit : java.util.List.of("min", "max")) {
            JsonNode dependency = sensor.definition().path(limit).path("sensor");
            if (dependency.isTextual()) {
                states.put(dependency.asText(), new StringType("00:00"));
            }
        }
        Map<Integer, byte[]> raw = new HashMap<>();
        sensor.registers().forEach(address -> raw.put(address, word(0xA5A5)));
        byte[] encoded = sensor.encode(command, states, raw, ZoneOffset.UTC);
        assertEquals(fixture.path("expected").size() * 2, encoded.length);
        for (int i = 0; i < fixture.path("expected").size(); i++) {
            assertEquals(fixture.path("expected").path(i).asInt(),
                    Short.toUnsignedInt(ByteBuffer.wrap(encoded, i * 2, 2).getShort()),
                    fixture.path("profile").asText() + "/" + sensor.id() + " " + value);
        }
    }

    @ParameterizedTest
    @MethodSource("profiles")
    void computesPvTotalsForEveryProfile(String name) throws IOException {
        SunsynkProfile profile = new SunsynkProfile(name);
        Map<Integer, byte[]> raw = new HashMap<>();
        BigDecimal expected = BigDecimal.ZERO;
        for (SunsynkSensor sensor : profile.sensors()) {
            if (sensor.id().matches("pv[1-8]_power")) {
                raw.put(sensor.registers().getFirst(), word(2));
                expected = expected
                        .add(sensor.definition().path("factor").decimalValue().abs().multiply(BigDecimal.valueOf(2)));
            }
        }
        State totalState = Objects.requireNonNull(profile.decode(raw, ZoneOffset.UTC).get("pv_power"));
        if (raw.isEmpty()) {
            assertEquals(UnDefType.UNDEF, totalState, "No MPPT sensors are defined for this source profile");
        } else {
            QuantityType<?> total = assertInstanceOf(QuantityType.class, totalState);
            assertEquals(0, expected.compareTo(total.toBigDecimal()));
        }
    }

    @ParameterizedTest(name = "{index}: Python conversion")
    @MethodSource("oracle")
    void matchesPythonReadConversions(JsonNode fixture) throws IOException {
        SunsynkProfile profile = new SunsynkProfile(fixture.path("profile").asText());
        SunsynkSensor sensor = find(profile, fixture.path("sensor").asText());
        for (JsonNode sample : fixture.path("cases")) {
            Map<Integer, byte[]> raw = new HashMap<>();
            for (int i = 0; i < sensor.registers().size(); i++) {
                raw.put(sensor.registers().get(i), word(sample.path("words").path(i).asInt()));
            }
            State actual = sensor.decode(raw, ZoneOffset.UTC);
            JsonNode expected = sample.path("expected");
            String context = fixture.path("profile").asText() + "/" + sensor.id() + " " + sample.path("words");
            if (expected.isNull()) {
                assertEquals(UnDefType.UNDEF, actual, context);
            } else if (expected.isNumber()) {
                BigDecimal value = actual instanceof QuantityType<?> quantity ? quantity.toBigDecimal()
                        : ((DecimalType) actual).toBigDecimal();
                assertEquals(0, expected.decimalValue().compareTo(value), context + ": " + value + " != " + expected);
            } else if (expected.isBoolean()) {
                assertEquals(expected.asBoolean() ? OnOffType.ON : OnOffType.OFF, actual, context);
            } else if ("SystemTimeRWSensor".equals(sensor.kind())) {
                assertEquals(LocalDateTime.parse(expected.asText().replace(' ', 'T')).toInstant(ZoneOffset.UTC),
                        ((DateTimeType) actual).getInstant(), context);
            } else {
                assertEquals(expected.asText(), actual.toString(), context);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("profiles")
    void batchesEveryRequiredAddressAndKeepsReadOnlyControls(String name) throws IOException {
        SunsynkProfile profile = new SunsynkProfile(name);
        var plan = profile.readPlan();
        assertTrue(plan.size() < profile.sensors().size());
        plan.forEach(block -> assertTrue(
                block.getEnd() - block.getStart() + 1 >= 2 && block.getEnd() - block.getStart() + 1 <= 125));
        for (SunsynkSensor sensor : profile.sensors()) {
            sensor.registers()
                    .forEach(address -> assertTrue(
                            plan.stream().anyMatch(block -> address >= block.getStart() && address <= block.getEnd()),
                            sensor.id()));
            if (!sensor.writable()) {
                assertFalse(sensor.accepts(OnOffType.ON));
                assertFalse(sensor.accepts(new DecimalType(1)));
            }
        }
    }

    @Test
    void sharedProgramFieldsPreserveUnrelatedBits() throws IOException {
        SunsynkProfile profile = new SunsynkProfile("single_phase");
        Map<Integer, byte[]> raw = new HashMap<>();
        raw.put(274, word(0xA5E0));
        byte[] charge = find(profile, "prog1_charge").encode(new StringType("Allow Grid & Gen"), Map.of(), raw,
                ZoneOffset.UTC);
        assertEquals(0xA5E3, unsigned(charge));
        raw.put(274, charge);
        byte[] mode = find(profile, "prog1_mode").encode(new StringType("Charge"), Map.of(), raw, ZoneOffset.UTC);
        assertEquals(0xA5EF, unsigned(mode));
        raw.put(274, mode);
        byte[] sell = find(profile, "prog1_sell").encode(OnOffType.OFF, Map.of(), raw, ZoneOffset.UTC);
        assertEquals(0xA5AF, unsigned(sell));
        assertThrows(IllegalArgumentException.class,
                () -> find(profile, "prog1_charge").encode(new StringType("invalid"), Map.of(), raw, ZoneOffset.UTC));
        assertThrows(IllegalArgumentException.class,
                () -> find(profile, "prog1_sell").encode(OnOffType.ON, Map.of(), Map.of(), ZoneOffset.UTC));
    }

    @Test
    void normalizesUnitsAndRejectsInvalidLimitsOrPrecision() throws IOException {
        SunsynkProfile profile = new SunsynkProfile("single_phase");
        SunsynkSensor current = find(profile, "battery_max_charge_current");
        assertEquals(100, unsigned(current.encode(new QuantityType<>(100000, MetricPrefix.MILLI(Units.AMPERE)),
                Map.of(), Map.of(), ZoneOffset.UTC)));
        assertThrows(IllegalArgumentException.class,
                () -> current.encode(new DecimalType(186), Map.of(), Map.of(), ZoneOffset.UTC));
        assertThrows(IllegalArgumentException.class,
                () -> current.encode(new QuantityType<>(10, Units.VOLT), Map.of(), Map.of(), ZoneOffset.UTC));
        SunsynkProfile micro = new SunsynkProfile("micro_hybrid_ess");
        SunsynkSensor microCurrent = find(micro, "battery_max_charge_current");
        assertEquals(2050, unsigned(microCurrent.encode(new DecimalType("20.5"), Map.of(), Map.of(), ZoneOffset.UTC)));
        assertThrows(ArithmeticException.class,
                () -> microCurrent.encode(new DecimalType("20.501"), Map.of(), Map.of(), ZoneOffset.UTC));
        assertThrows(IllegalArgumentException.class,
                () -> find(profile, "prog1_capacity").encode(new DecimalType(50), Map.of(), Map.of(), ZoneOffset.UTC));
    }

    @Test
    void profileSpecificVoltageBoundsAndClockEncoding() throws IOException {
        SunsynkProfile lv = new SunsynkProfile("three_phase_lv");
        SunsynkProfile hv = new SunsynkProfile("three_phase_hv");
        Map<String, State> lvState = lv.decode(Map.of(), ZoneOffset.UTC);
        Map<String, State> hvState = hv.decode(Map.of(), ZoneOffset.UTC);
        assertThrows(IllegalArgumentException.class, () -> find(lv, "battery_absorption_voltage")
                .encode(new DecimalType(500), lvState, Map.of(), ZoneOffset.UTC));
        assertEquals(50000, unsigned(find(hv, "battery_absorption_voltage").encode(new DecimalType(500), hvState,
                Map.of(), ZoneOffset.UTC)));
        SunsynkSensor clock = find(lv, "date_time");
        DateTimeType value = new DateTimeType("2026-10-30T12:34:56Z");
        byte[] encoded = clock.encode(value, Map.of(), Map.of(), ZoneOffset.UTC);
        assertArrayEquals(new byte[] { 26, 10, 30, 12, 34, 56 }, encoded);
        assertEquals(value.getInstant(),
                ((DateTimeType) clock.decode(Map.of(clock.registers().get(0), word((26 << 8) | 10),
                        clock.registers().get(1), word((30 << 8) | 12), clock.registers().get(2), word((34 << 8) | 56)),
                        ZoneOffset.UTC)).getInstant());
    }

    @Test
    void validatesCircularProgramTimesAndComputesPvFromAvailableMppts() throws IOException {
        SunsynkProfile profile = new SunsynkProfile("single_phase");
        SunsynkSensor time = find(profile, "prog1_time");
        Map<String, State> neighbors = Map.of("prog6_time", new StringType("22:00"), "prog2_time",
                new StringType("06:00"));
        assertEquals(2330, unsigned(time.encode(new StringType("23:30"), neighbors, Map.of(), ZoneOffset.UTC)));
        assertThrows(IllegalArgumentException.class,
                () -> time.encode(new StringType("12:00"), neighbors, Map.of(), ZoneOffset.UTC));
        assertThrows(IllegalArgumentException.class,
                () -> time.encode(new StringType("24:00"), neighbors, Map.of(), ZoneOffset.UTC));
        Map<String, State> states = profile.decode(Map.of(186, word(10), 187, word(20), 188, word(30), 189, word(40)),
                ZoneOffset.UTC);
        assertEquals(100, ((QuantityType<?>) Objects.requireNonNull(states.get("pv_power"))).intValue());
        assertEquals(UnDefType.UNDEF, profile.decode(Map.of(186, word(10)), ZoneOffset.UTC).get("pv_power"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "communication_board_version", "control_board_version" })
    void boardVersionChannelsPublishUnsignedDecimalStringsAndRejectCommands(String id) throws IOException {
        SunsynkProfile profile = new SunsynkProfile("single_phase");
        SunsynkSensor sensor = find(profile, id);
        int address = "communication_board_version".equals(id) ? 14 : 13;
        assertEquals(List.of(address), sensor.registers());
        assertEquals("String", sensor.itemType());
        assertFalse(sensor.writable());
        for (int raw : new int[] { 0, 32767, 32768, 65535 }) {
            assertEquals(new StringType(Integer.toString(raw)),
                    profile.decode(Map.of(address, word(raw)), ZoneOffset.UTC).get(id));
        }
        assertEquals(UnDefType.UNDEF, profile.decode(Map.of(), ZoneOffset.UTC).get(id));
        assertEquals(UnDefType.UNDEF, profile.decode(Map.of(address, new byte[] { 1 }), ZoneOffset.UTC).get(id));
        assertFalse(sensor.accepts(new StringType("123")));
        assertThrows(IllegalArgumentException.class,
                () -> sensor.encode(new StringType("123"), Map.of(), Map.of(address, word(1)), ZoneOffset.UTC));
    }

    @Test
    void inverterIdPreservesRegisterAndCharacterOrderAndHandlesPaddingAndUnavailableWords() throws IOException {
        SunsynkProfile profile = new SunsynkProfile("single_phase");
        SunsynkSensor sensor = find(profile, "inverter_id");
        assertEquals(List.of(3, 4, 5, 6, 7), sensor.registers());
        assertEquals("String", sensor.itemType());
        assertFalse(sensor.writable());
        Map<Integer, byte[]> snapshot = new HashMap<>();
        String serial = "TEST000001";
        for (int i = 0; i < 5; i++) {
            snapshot.put(3 + i, word((serial.charAt(i * 2) << 8) | serial.charAt(i * 2 + 1)));
        }
        assertEquals(new StringType(serial), profile.decode(snapshot, ZoneOffset.UTC).get("inverter_id"));
        snapshot.put(7, word(0));
        assertEquals(new StringType("TEST0000"), profile.decode(snapshot, ZoneOffset.UTC).get("inverter_id"));
        snapshot.remove(5);
        assertEquals(UnDefType.UNDEF, profile.decode(snapshot, ZoneOffset.UTC).get("inverter_id"));
        snapshot.put(5, new byte[] { 1 });
        assertEquals(UnDefType.UNDEF, profile.decode(snapshot, ZoneOffset.UTC).get("inverter_id"));
        assertThrows(IllegalArgumentException.class,
                () -> sensor.encode(new StringType(serial), Map.of(), snapshot, ZoneOffset.UTC));
    }

    @Test
    void bindingMetadataIsSinglePhaseOnlyAndUsesExistingReadWindows() throws IOException {
        SunsynkProfile profile = new SunsynkProfile("single_phase");
        assertEquals(150, profile.sensors().size());
        assertEquals(List.of(List.of(0, 116), List.of(150, 274), List.of(275, 325), List.of(603, 611)),
                profile.readPlan().stream().map(r -> List.of(r.getStart(), r.getEnd())).toList());
        List<String> ids = List.of("communication_board_version", "control_board_version", "inverter_id");
        for (String id : ids) {
            assertEquals("binding:deye_hybrid", find(profile, id).definition().path("source").asText());
        }
        for (String name : SolarmanBindingConstants.SUNSYNK_PROFILES.values()) {
            if (!"single_phase".equals(name)) {
                assertTrue(new SunsynkProfile(name).sensors().stream().noneMatch(s -> ids.contains(s.id())));
            }
        }
    }

    private static SunsynkSensor find(SunsynkProfile profile, String id) {
        return profile.sensors().stream().filter(sensor -> sensor.id().equals(id)).findFirst().orElseThrow();
    }

    private static byte[] word(int raw) {
        return ByteBuffer.allocate(2).putShort((short) raw).array();
    }

    private static int unsigned(byte[] data) {
        return Short.toUnsignedInt(ByteBuffer.wrap(data).getShort());
    }
}
