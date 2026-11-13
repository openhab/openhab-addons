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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.SolarmanBindingConstants;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.typeprovider.ChannelUtils;
import org.openhab.binding.solarman.internal.util.ModbusRegisterCodec;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Interprets a pinned Python sensor definition, including masks and dependent limits.
 *
 * @author Ricardo Costa - Initial contribution
 */
@NonNullByDefault
public final class SunsynkSensor {
    private final JsonNode definition;
    private final List<Integer> registers;
    private final ArrayDeque<int[]> history = new ArrayDeque<>();

    public SunsynkSensor(JsonNode definition) {
        this.definition = definition;
        List<Integer> addresses = new ArrayList<>();
        definition.path("registers").forEach(r -> addresses.add(r.asInt()));
        registers = List.copyOf(addresses);
        Objects.requireNonNull(SolarmanBindingConstants.SUNSYNK_CHANNEL_IDS.get(id()), "Unknown channel ID");
    }

    public String id() {
        return definition.path("id").asText();
    }

    public String channelId() {
        return Objects.requireNonNull(SolarmanBindingConstants.SUNSYNK_CHANNEL_IDS.get(id()));
    }

    public String kind() {
        return definition.path("kind").asText();
    }

    public List<Integer> registers() {
        return registers;
    }

    public JsonNode definition() {
        return definition;
    }

    public String itemType() {
        return switch (kind()) {
            case "SwitchRWSensor", "BinarySensor" -> "Switch";
            case "SystemTimeRWSensor" -> "DateTime";
            case "SelectRWSensor", "EnumSensor", "TimeRWSensor", "FaultSensor", "HVFaultSensor", "InverterStateSensor",
                    "SDStatusSensor", "RegisterStringSensor", "RegisterTextSensor" ->
                "String";
            default -> {
                ParameterItem item = new ParameterItem();
                item.setUom(definition.path("unit").asText());
                yield ChannelUtils.getItemType(item);
            }
        };
    }

    public boolean writable() {
        return definition.path("writable").asBoolean();
    }

    public boolean accepts(Command command) {
        if (!writable()) {
            return false;
        }
        return switch (kind()) {
            case "NumberRWSensor" -> command instanceof DecimalType || command instanceof QuantityType<?>;
            case "SwitchRWSensor" -> command instanceof OnOffType;
            case "SelectRWSensor", "TimeRWSensor" -> command instanceof StringType;
            case "SystemTimeRWSensor" -> command instanceof DateTimeType;
            default -> false;
        };
    }

    private int word(Map<Integer, byte[]> snapshot, int address) {
        byte @Nullable [] bytes = snapshot.get(address);
        if (bytes == null || bytes.length != 2) {
            throw new IllegalArgumentException("Required register was not read");
        }
        return Short.toUnsignedInt(ByteBuffer.wrap(bytes).getShort());
    }

    public State decode(Map<Integer, byte[]> snapshot, ZoneId zone) {
        if (!snapshot.keySet().containsAll(registers)) {
            return UnDefType.UNDEF;
        }
        if ("Constant".equals(kind())) {
            return numeric(definition.path("value").decimalValue());
        }
        int first = word(snapshot, registers.getFirst());
        int mask = definition.path("mask").asInt();
        int masked = mask == 0 ? first : first & mask;
        return switch (kind()) {
            case "Sensor", "Sensor16", "NumberRWSensor" -> numeric(decodeNumber(snapshot));
            case "RegisterStringSensor" -> new StringType(Integer.toString(first));
            case "RegisterTextSensor" -> {
                StringBuilder text = new StringBuilder();
                for (int address : registers) {
                    int value = word(snapshot, address);
                    text.append((char) (value >>> 8)).append((char) (value & 255));
                }
                yield new StringType(text.toString().trim());
            }
            case "TempSensor" -> numeric(
                    BigDecimal.valueOf(first * factor().abs().doubleValue() - definition.path("offset").doubleValue()));
            case "MathSensor" -> numeric(decodeMath(snapshot));
            case "SwitchRWSensor", "BinarySensor" -> {
                JsonNode on = definition.path("on");
                boolean enabled = on.isNumber() ? masked == on.asInt() : masked != definition.path("off").asInt();
                yield enabled ? OnOffType.ON : OnOffType.OFF;
            }
            case "SelectRWSensor", "EnumSensor" -> {
                JsonNode option = definition.path("options").path(Integer.toString(masked));
                if (option.isTextual()) {
                    yield new StringType(option.asText());
                }
                JsonNode unknown = definition.path("unknown");
                yield unknown.isTextual() ? new StringType(unknown.asText().replace("{}", Integer.toString(masked)))
                        : UnDefType.UNDEF;
            }
            case "TimeRWSensor" -> new StringType(LocalTime.of(first / 100, first % 100).toString());
            case "SystemTimeRWSensor" -> {
                int middle = word(snapshot, registers.get(1));
                int last = word(snapshot, registers.get(2));
                LocalDateTime time = LocalDateTime.of((first >>> 8) + definition.path("year_offset").asInt(2000),
                        first & 255, middle >>> 8, middle & 255, last >>> 8, last & 255);
                yield new DateTimeType(time.atZone(zone));
            }
            case "FaultSensor", "HVFaultSensor" -> new StringType(decodeFaults(snapshot));
            case "SDStatusSensor" ->
                new StringType(first == 1000 ? "fault" : first == 2000 ? "ok" : "unknown " + first);
            case "InverterStateSensor" -> new StringType(switch (first) {
                case 0 -> "standby";
                case 1 -> "selfcheck";
                case 2 -> "ok";
                case 3 -> "alarm";
                case 4 -> "fault";
                case 5 -> "activating";
                default -> "unknown " + first;
            });
            default -> throw new IllegalArgumentException("Unsupported sensor kind: " + kind());
        };
    }

    private BigDecimal factor() {
        return definition.path("factor").decimalValue();
    }

    private BigDecimal decodeNumber(Map<Integer, byte[]> snapshot) {
        List<Integer> addresses = registers;
        if ("Sensor16".equals(kind())) {
            int low = word(snapshot, registers.getFirst());
            int high = word(snapshot, registers.get(1));
            history.addLast(new int[] { low, high });
            if (history.size() > 10) {
                history.removeFirst();
            }
            double mean = history.stream().mapToInt(pair -> pair[0]).average().orElse(low);
            if (history.stream().anyMatch(pair -> pair[1] == 0) || high == 65535 && mean - low > 10000) {
                addresses = List.of(registers.getFirst());
            }
        }
        BigDecimal raw = new BigDecimal(ModbusRegisterCodec.decode(addresses, snapshot, factor().signum() < 0));
        int mask = definition.path("mask").asInt();
        if (mask != 0) {
            raw = BigDecimal.valueOf(word(snapshot, registers.getFirst()) & mask);
        }
        return BigDecimal.valueOf(raw.doubleValue() * factor().abs().doubleValue());
    }

    private BigDecimal decodeMath(Map<Integer, byte[]> snapshot) {
        BigDecimal result = BigDecimal.ZERO;
        for (int i = 0; i < registers.size(); i++) {
            result = result.add(new BigDecimal(ModbusRegisterCodec.decode(List.of(registers.get(i)), snapshot, true))
                    .multiply(definition.path("factors").path(i).decimalValue()));
        }
        if (definition.path("absolute").asBoolean()) {
            result = result.abs();
        }
        return definition.path("no_negative").asBoolean() ? result.max(BigDecimal.ZERO) : result;
    }

    private String decodeFaults(Map<Integer, byte[]> snapshot) {
        StringJoiner faults = new StringJoiner(", ");
        for (int i = 0; i < registers.size(); i++) {
            int raw = word(snapshot, registers.get(i));
            for (int bit = 0; bit < 16; bit++) {
                if ((raw & (1 << bit)) != 0) {
                    int number = i * 16 + bit + 1;
                    String label = definition.path("faults").path(Integer.toString(number)).asText("");
                    faults.add((String.format(Locale.ROOT, "F%02d", number) + " " + label).trim());
                }
            }
        }
        return faults.toString();
    }

    public State numeric(BigDecimal value) {
        // Python rounds the actual binary floating-point value, including half-cent boundary behavior.
        BigDecimal rounded = new BigDecimal(value.doubleValue()).setScale(2, RoundingMode.HALF_EVEN);
        String unit = definition.path("unit").asText();
        if (unit.isBlank()) {
            return new DecimalType(rounded);
        }
        Unit<?> target = ChannelUtils.getUnitFromDefinition(unit);
        if (target == null) {
            throw new IllegalArgumentException("Unsupported profile unit: " + unit);
        }
        return new QuantityType<>(rounded, target);
    }

    public byte[] encode(Command command, Map<String, State> states, Map<Integer, byte[]> snapshot, ZoneId zone) {
        if (!accepts(command) || registers.isEmpty()) {
            throw new IllegalArgumentException("Command type is invalid or channel is read-only");
        }
        for (int i = 0; i < registers.size(); i++) {
            if (registers.get(i) != registers.getFirst() + i) {
                throw new IllegalArgumentException("Writable registers must be contiguous");
            }
        }
        byte[] payload;
        switch (kind()) {
            case "NumberRWSensor" -> {
                BigDecimal value = commandNumber(command);
                BigDecimal minimum = bound(definition.path("min"), states);
                BigDecimal maximum = bound(definition.path("max"), states);
                if (minimum.compareTo(maximum) > 0 || value.compareTo(minimum) < 0 || value.compareTo(maximum) > 0) {
                    throw new IllegalArgumentException("Command is outside the current profile limits");
                }
                payload = ModbusRegisterCodec.encode(
                        value.divide(factor().abs(), 0, RoundingMode.UNNECESSARY).toBigIntegerExact(), registers.size(),
                        factor().signum() < 0);
            }
            case "SwitchRWSensor" -> {
                JsonNode on = definition.path("on");
                int mask = definition.path("mask").asInt();
                int enabled = on.isNumber() ? on.asInt() : mask == 0 ? 255 : 255 & mask;
                int value = command == OnOffType.ON ? enabled : definition.path("off").asInt();
                payload = ByteBuffer.allocate(2).putShort((short) value).array();
            }
            case "SelectRWSensor" -> {
                String requested = command.toString();
                Integer selected = null;
                for (var field : definition.path("options").properties()) {
                    if (requested.equals(field.getValue().asText())) {
                        selected = Integer.valueOf(field.getKey());
                        break;
                    }
                }
                if (selected == null) {
                    throw new IllegalArgumentException("Unknown profile option");
                }
                payload = ByteBuffer.allocate(2).putShort(selected.shortValue()).array();
            }
            case "TimeRWSensor" -> {
                LocalTime time = parseTime(command.toString());
                validateTime(time, states);
                payload = ByteBuffer.allocate(2).putShort((short) (time.getHour() * 100 + time.getMinute())).array();
            }
            case "SystemTimeRWSensor" -> {
                if (!(command instanceof DateTimeType dateTime)) {
                    throw new IllegalArgumentException("DateTime command expected");
                }
                var time = dateTime.getZonedDateTime(zone);
                int year = time.getYear() - definition.path("year_offset").asInt(2000);
                if (year < 0 || year > 255) {
                    throw new IllegalArgumentException("Device clock year is outside its register range");
                }
                payload = ByteBuffer.allocate(6).putShort((short) ((year << 8) | time.getMonthValue()))
                        .putShort((short) ((time.getDayOfMonth() << 8) | time.getHour()))
                        .putShort((short) ((time.getMinute() << 8) | time.getSecond())).array();
            }
            default -> throw new IllegalArgumentException("Unsupported write conversion");
        }
        int mask = definition.path("mask").asInt();
        if (mask != 0) {
            int current = word(snapshot, registers.getFirst());
            int patch = Short.toUnsignedInt(ByteBuffer.wrap(payload).getShort());
            if ((patch & ~mask) != 0) {
                throw new IllegalArgumentException("Write would set bits outside the channel mask");
            }
            payload = ByteBuffer.allocate(2).putShort((short) ((current & ~mask) | (patch & mask))).array();
        }
        return payload;
    }

    private BigDecimal commandNumber(Command command) {
        if (command instanceof QuantityType<?> quantity) {
            Unit<?> unit = ChannelUtils.getUnitFromDefinition(definition.path("unit").asText());
            QuantityType<?> converted = unit == null ? null : quantity.toUnit(unit);
            if (converted == null) {
                throw new IllegalArgumentException("Command unit is incompatible with the channel");
            }
            return converted.toBigDecimal();
        }
        if (command instanceof DecimalType decimal) {
            return decimal.toBigDecimal();
        }
        throw new IllegalArgumentException("Numeric command expected");
    }

    private BigDecimal bound(JsonNode limit, Map<String, State> states) {
        if (limit.isNumber()) {
            return limit.decimalValue();
        }
        State state = states.get(limit.path("sensor").asText());
        if (state instanceof QuantityType<?> quantity) {
            return quantity.toBigDecimal();
        }
        if (state instanceof DecimalType decimal) {
            return decimal.toBigDecimal();
        }
        throw new IllegalArgumentException("A required write limit is unavailable");
    }

    private LocalTime parseTime(String value) {
        if (!value.matches("(?:[01][0-9]|2[0-3]):[0-5][0-9]")) {
            throw new IllegalArgumentException("Program time must use HH:mm format");
        }
        return LocalTime.parse(value);
    }

    private void validateTime(LocalTime requested, Map<String, State> states) {
        JsonNode minimum = definition.path("min");
        JsonNode maximum = definition.path("max");
        if (!minimum.isObject() || !maximum.isObject()) {
            return;
        }
        State previous = states.get(minimum.path("sensor").asText());
        State next = states.get(maximum.path("sensor").asText());
        if (!(previous instanceof StringType) || !(next instanceof StringType)) {
            throw new IllegalArgumentException("Neighboring program times are unavailable");
        }
        int start = parseTime(previous.toString()).toSecondOfDay();
        int end = parseTime(next.toString()).toSecondOfDay();
        int duration = Math.floorMod(end - start, 86400);
        if (duration != 0 && Math.floorMod(requested.toSecondOfDay() - start, 86400) > duration) {
            throw new IllegalArgumentException("Program time is outside its neighboring periods");
        }
    }
}
