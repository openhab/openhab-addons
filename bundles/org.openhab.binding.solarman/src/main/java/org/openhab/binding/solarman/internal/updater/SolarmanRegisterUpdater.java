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
package org.openhab.binding.solarman.internal.updater;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.defmodel.Lookup;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.enums.IntegerValueType;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnection;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanProtocol;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.binding.solarman.internal.util.ModbusRegisterCodec;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarmanRegisterUpdater} is responsible for updating registers from received commands
 *
 * @author Oleksandr Mishchuk - Initial contribution
 * @author Ricardo Costa - Advanced profiles and shared compatibility corrections
 */
@NonNullByDefault
public class SolarmanRegisterUpdater {
    private final Logger logger;

    private static final Pattern TIME_PATTERN = Pattern.compile("^(?:[01][0-9]|2[0-3]):[0-5][0-9]$");

    private final SolarmanLoggerConnector solarmanLoggerConnector;
    private final SolarmanProtocol solarmanProtocol;
    private final Map<ChannelUID, ParameterItem> writableChannels;

    public SolarmanRegisterUpdater(Map<ParameterItem, ChannelUID> paramToChannelMapping,
            SolarmanLoggerConnector solarmanLoggerConnector, SolarmanProtocol solarmanProtocol) {
        this(paramToChannelMapping, solarmanLoggerConnector, solarmanProtocol,
                LoggerFactory.getLogger(SolarmanRegisterUpdater.class));
    }

    SolarmanRegisterUpdater(Map<ParameterItem, ChannelUID> paramToChannelMapping,
            SolarmanLoggerConnector solarmanLoggerConnector, SolarmanProtocol solarmanProtocol, Logger logger) {
        this.logger = logger;
        this.solarmanProtocol = solarmanProtocol;
        this.solarmanLoggerConnector = solarmanLoggerConnector;

        writableChannels = paramToChannelMapping.entrySet().stream()
                .filter(e -> Boolean.FALSE.equals(e.getKey().getIsReadOnly())).filter(e -> {
                    final List<Integer> registers = e.getKey().getRegisters();
                    if (registers.isEmpty()) {
                        logger.warn("Writeable channel {} have no registers, skipping", e.getValue());
                        return false;
                    }

                    final int firstRegister = registers.getFirst();
                    int i = 0;
                    while (i < registers.size() && registers.get(i) == firstRegister + i) {
                        i++;
                    }
                    if (i != registers.size()) {
                        logger.warn("Writeable channel {} should have consecutive registers, skipping", e.getValue());
                        return false;
                    }

                    return true;
                }).collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    }

    public void updateLoggerRegisters(ChannelUID channelUID, Command command) {
        // Ignore refresh command as we are using polling for reading data
        if (command instanceof RefreshType) {
            return;
        }

        final ParameterItem channelToUpdate = writableChannels.get(channelUID);
        if (channelToUpdate == null) {
            logger.warn("Channel '{}' is either read-only or doesn't exist", channelUID);
            return;
        }

        switch (channelToUpdate.getRule()) {
            case 1 -> updateIntRegisters(channelUID, channelToUpdate, command, Short.BYTES, IntegerValueType.UNSIGNED);
            case 2 -> updateIntRegisters(channelUID, channelToUpdate, command, Short.BYTES, IntegerValueType.SIGNED);
            case 3 ->
                updateIntRegisters(channelUID, channelToUpdate, command, Integer.BYTES, IntegerValueType.UNSIGNED);
            case 4 -> updateIntRegisters(channelUID, channelToUpdate, command, Integer.BYTES, IntegerValueType.SIGNED);
            case 5 -> updateStringRegisters(channelUID, channelToUpdate, command);
            case 6 -> updateRawRegisters(channelUID, channelToUpdate, command);
            case 7 -> updateVersionRegisters(channelUID, channelToUpdate, command);
            case 8 -> updateDateTimeRegisters(channelUID, channelToUpdate, command);
            case 9 -> updateTimeRegisters(channelUID, channelToUpdate, command);
        }
    }

    private void updateIntRegisters(ChannelUID channelUID, ParameterItem channelToUpdate, Command command, int size,
            IntegerValueType integerValueType) {
        final DecimalType decimalValue = switch (command) {
            case QuantityType<?> quantity -> {
                String unit = channelToUpdate.getUom();
                QuantityType<?> converted = unit == null || unit.isBlank() ? null : quantity.toUnit(unit);
                if (converted == null) {
                    throw new IllegalArgumentException("Command unit is incompatible with the channel");
                }
                yield new DecimalType(converted.toBigDecimal());
            }
            case Number number -> new DecimalType(number);
            case OnOffType onOff -> onOff == OnOffType.ON ? new DecimalType(1) : DecimalType.ZERO;
            case StringType stringType -> {
                if (channelToUpdate.getLookup().isEmpty()) {
                    logUnexpectedCommand(channelUID, command);
                    yield null;
                } else {
                    final String lookupValue = stringType.toString();
                    final Optional<Lookup> lookupOptional = channelToUpdate.getLookup().stream()
                            .filter(l -> lookupValue.equals(l.getValue())).findFirst();
                    if (lookupOptional.isPresent()) {
                        yield new DecimalType(lookupOptional.get().getKey());
                    } else {
                        logUnexpectedCommand(channelUID, command);
                        yield null;
                    }
                }
            }
            default -> {
                logUnexpectedCommand(channelUID, command);
                yield null;
            }
        };

        if (decimalValue != null) {
            if (channelToUpdate.getRegisters().size() != size / 2) {
                throw new IllegalArgumentException("Conversion width does not match channel registers");
            }
            byte[] data = ModbusRegisterCodec.encode(
                    convertNumericValue(decimalValue, channelToUpdate.getOffset(), channelToUpdate.getScale())
                            .setScale(0, RoundingMode.HALF_UP).toBigIntegerExact(),
                    size / 2, integerValueType == IntegerValueType.SIGNED);
            writeRegisters(channelUID, channelToUpdate, command, data, true);
        }
    }

    private void updateStringRegisters(ChannelUID channelUID, ParameterItem channelToUpdate, Command command) {
        if (command instanceof StringType stringType) {
            byte[] encoded = stringType.toString().getBytes(StandardCharsets.UTF_8);
            int length = channelToUpdate.getRegisters().size() * 2;
            if (encoded.length > length) {
                throw new IllegalArgumentException("String exceeds the configured register capacity");
            }
            byte[] data = ByteBuffer.allocate(length).put(encoded).array();
            writeRegisters(channelUID, channelToUpdate, command, data, false);
        } else {
            logUnexpectedCommand(channelUID, command);
        }
    }

    private void updateRawRegisters(ChannelUID channelUID, ParameterItem channelToUpdate, Command command) {
        logger.warn("Writing Raw to logger is not implemented yet");
    }

    private void updateVersionRegisters(ChannelUID channelUID, ParameterItem channelToUpdate, Command command) {
        logger.warn("Writing Version to logger is not implemented yet");
    }

    private void updateDateTimeRegisters(ChannelUID channelUID, ParameterItem channelToUpdate, Command command) {
        if (command instanceof DateTimeType dateTime && channelToUpdate.getRegisters().size() == 3) {
            ZonedDateTime value = dateTime.getZonedDateTime(ZoneId.systemDefault());
            int year = value.getYear() - 2000;
            if (year < 0 || year > 255) {
                throw new IllegalArgumentException("Device clock year must be between 2000 and 2255");
            }
            byte[] data = ByteBuffer.allocate(6).putShort((short) ((year << 8) | value.getMonthValue()))
                    .putShort((short) ((value.getDayOfMonth() << 8) | value.getHour()))
                    .putShort((short) ((value.getMinute() << 8) | value.getSecond())).array();
            writeRegisters(channelUID, channelToUpdate, command, data, false);
        } else {
            logUnexpectedCommand(channelUID, command);
        }
    }

    private void updateTimeRegisters(ChannelUID channelUID, ParameterItem channelToUpdate, Command command) {
        if (command instanceof StringType string) {
            String timeString = string.toString();
            final Matcher timeMatcher = TIME_PATTERN.matcher(timeString);
            if (timeMatcher.matches()) {
                final int hour = Integer.parseInt(timeString.substring(0, 2));
                final int minute = Integer.parseInt(timeString.substring(3, 5));
                final short value = (short) (hour * 100 + minute);
                byte[] data = ByteBuffer.allocate(2).putShort(value).array();
                writeRegisters(channelUID, channelToUpdate, command, data, false);
            } else {
                logger.warn("Received string '{}' is not correct time format 'HH:mm'", timeString);
            }
        } else {
            logUnexpectedCommand(channelUID, command);
        }
    }

    private BigDecimal convertNumericValue(DecimalType decimal, @Nullable BigDecimal offset,
            @Nullable BigDecimal scale) {
        return decimal.toBigDecimal().divide(scale != null ? scale : BigDecimal.ONE, MathContext.DECIMAL128)
                .add(offset != null ? offset : BigDecimal.ZERO);
    }

    private void writeRegisters(ChannelUID channelUID, ParameterItem item, Command command, byte[] data,
            boolean retryNumericMismatch) {
        int firstRegister = item.getRegisters().getFirst();
        int registerCount = item.getRegisters().size();
        String addresses = item.getRegisters().stream().map(register -> String.format(Locale.ROOT, "0x%04X", register))
                .collect(Collectors.joining(","));
        if (data.length != registerCount * 2) {
            throw new IllegalArgumentException("Payload size does not match the declared register count");
        }
        try (SolarmanLoggerConnection connection = solarmanLoggerConnector.createConnection()) {
            if (!connection.isConnected()) {
                throw new SolarmanConnectionException("Unable to connect to logger");
            }
            Map<Integer, byte[]> before = readWriteRegisters(connection, firstRegister, registerCount);
            int attempts = retryNumericMismatch ? 2 : 1;
            for (int attempt = 1; attempt <= attempts; attempt++) {
                logger.debug("Writing channel {} registers [{}]: requested={}, payload={}", channelUID, addresses,
                        command, HexUtils.bytesToHex(data));
                if (!solarmanProtocol.writeRegisters(connection, firstRegister, data)) {
                    throw new SolarmanConnectionException("Logger rejected the register write");
                }
                Map<Integer, byte[]> after = readWriteRegisters(connection, firstRegister, registerCount);
                if (Arrays.equals(data, registerBytes(item, after))) {
                    if (!Arrays.equals(registerBytes(item, before), registerBytes(item, after))) {
                        logger.info(
                                "Confirmed register change: channel={}, registers=[{}], previous={}, requested={}, confirmed={}",
                                channelUID, addresses, auditValue(item, before), command, auditValue(item, after));
                    } else {
                        logger.debug(
                                "Confirmed unchanged value: channel={}, registers=[{}], requested={}, confirmed={}",
                                channelUID, addresses, command, auditValue(item, after));
                    }
                    return;
                }
                if (item.getRule() == 8) {
                    // A running device clock can advance between the write and the read; do not claim exact success.
                    logger.debug(
                            "Clock write acknowledged without exact confirmation: channel={}, registers=[{}], requested={}, observed={}",
                            channelUID, addresses, command, auditValue(item, after));
                    return;
                }
                logger.warn(
                        "Write confirmation mismatch: channel={}, registers=[{}], requested={}, observed={}, attempt={}/{}",
                        channelUID, addresses, command, auditValue(item, after), attempt, attempts);
                if (attempt < attempts) {
                    logger.warn(
                            "Repeating numeric write after confirmed mismatch: channel={}, registers=[{}], requested={}",
                            channelUID, addresses, command);
                }
            }
            throw new SolarmanConnectionException("Register write did not match device readback at " + addresses);
        } catch (SolarmanException e) {
            logger.warn("Write or confirmation failed: channel={}, registers=[{}], requested={}, reason={}", channelUID,
                    addresses, command, e.getMessage());
            throw new IllegalStateException("Failed to write logger registers: " + e.getMessage(), e);
        }
    }

    private Map<Integer, byte[]> readWriteRegisters(SolarmanLoggerConnection connection, int firstRegister,
            int registerCount) throws SolarmanException {
        Map<Integer, byte[]> values = solarmanProtocol.readRegisters(connection, (byte) 3, firstRegister,
                firstRegister + registerCount - 1);
        for (int index = 0; index < registerCount; index++) {
            byte @Nullable [] word = values.get(firstRegister + index);
            if (word == null || word.length != Short.BYTES) {
                throw new SolarmanConnectionException("Incomplete write audit read at 0x"
                        + String.format(Locale.ROOT, "%04X", firstRegister + index));
            }
        }
        return values;
    }

    private byte[] registerBytes(ParameterItem item, Map<Integer, byte[]> values) {
        ByteBuffer bytes = ByteBuffer.allocate(item.getRegisters().size() * Short.BYTES);
        item.getRegisters().forEach(register -> bytes.put(Objects.requireNonNull(values.get(register))));
        return bytes.array();
    }

    private String auditValue(ParameterItem item, Map<Integer, byte[]> values) {
        if (item.getRule() >= 1 && item.getRule() <= 4) {
            var raw = ModbusRegisterCodec.decode(item.getRegisters(), values,
                    item.getRule() == 2 || item.getRule() == 4);
            if (item.hasLookup()) {
                return Objects
                        .requireNonNull(item.getLookup().stream().filter(option -> option.getKey() == raw.intValue())
                                .map(Lookup::getValue).findFirst().orElse(raw.toString()));
            }
            BigDecimal value = new BigDecimal(raw)
                    .subtract(Objects.requireNonNullElse(item.getOffset(), BigDecimal.ZERO))
                    .multiply(Objects.requireNonNullElse(item.getScale(), BigDecimal.ONE));
            return value.stripTrailingZeros().toPlainString() + " " + Objects.requireNonNullElse(item.getUom(), "");
        }
        if (item.getRule() == 5) {
            return new String(registerBytes(item, values), StandardCharsets.UTF_8).replace("\u0000", "");
        }
        if (item.getRule() == 9) {
            int time = Short.toUnsignedInt(ByteBuffer.wrap(registerBytes(item, values)).getShort());
            return String.format(Locale.ROOT, "%02d:%02d", time / 100, time % 100);
        }
        return HexUtils.bytesToHex(registerBytes(item, values));
    }

    private void logUnexpectedCommand(ChannelUID uid, Command command) {
        logger.warn("Received unexpected command {} ({}) in channel {}", command, command.getClass(), uid);
    }
}
