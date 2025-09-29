/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarmanRegisterUpdater} is responsible for updating registers from received commands
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@NonNullByDefault
public class SolarmanRegisterUpdater {
    private static final Logger logger = LoggerFactory.getLogger(SolarmanRegisterUpdater.class);

    private static final Pattern TIME_PATTERN = Pattern.compile("^([0-1][0-9])|(2[0-3]):[0-5][0-9]");

    private final SolarmanLoggerConnector solarmanLoggerConnector;
    private final SolarmanProtocol solarmanProtocol;
    private final Map<ChannelUID, ParameterItem> writableChannels;

    public SolarmanRegisterUpdater(Map<ParameterItem, ChannelUID> paramToChannelMapping,
            SolarmanLoggerConnector solarmanLoggerConnector, SolarmanProtocol solarmanProtocol) {
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
            case DecimalType decimal -> decimal;
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
            long value = convertNumericValue(decimalValue, channelToUpdate.getOffset(), channelToUpdate.getScale())
                    .longValue();
            if (IntegerValueType.UNSIGNED == integerValueType) {
                long mask = (1L << (size * 8)) - 1;
                value = value & mask;
            }
            ByteBuffer buffer = ByteBuffer.allocate(size);
            switch (size) {
                case 2 -> buffer.putShort((short) value);
                case 4 -> buffer.putInt((int) value);
                case 8 -> buffer.putLong(value);
            }
            byte[] data = buffer.array();
            writeRegisters(channelToUpdate.getRegisters().getFirst(), size / 2, data);
        }
    }

    private void updateStringRegisters(ChannelUID channelUID, ParameterItem channelToUpdate, Command command) {
        if (command instanceof StringType stringType) {
            String string = stringType.toString();
            int length = (string.length() + 1) / 2 * 2;
            byte[] data = ByteBuffer.allocate(length).put(string.getBytes(StandardCharsets.UTF_8)).array();
            writeRegisters(channelToUpdate.getRegisters().getFirst(), length / 2, data);
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
        logger.warn("Writing DateTime to logger is not implemented yet");
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
                writeRegisters(channelToUpdate.getRegisters().getFirst(), 1, data);
            } else {
                logger.warn("Received string '{}' is not correct time format 'HH:mm'", timeString);
            }
        } else {
            logUnexpectedCommand(channelUID, command);
        }
    }

    private BigDecimal convertNumericValue(DecimalType decimal, @Nullable BigDecimal offset,
            @Nullable BigDecimal scale) {
        return decimal.toBigDecimal().divide(scale != null ? scale : BigDecimal.ONE, RoundingMode.HALF_UP)
                .add(offset != null ? offset : BigDecimal.ZERO);
    }

    private void writeRegisters(int firstRegister, int registerCount, byte[] data) {
        if (data.length > registerCount * 2) {
            logger.warn(
                    "Data to be written ({}) is longer than the number of 2 byte registers declared for channel ({}). Data will be truncated!",
                    data.length, registerCount);
        }

        data = ByteBuffer.wrap(data, 0, registerCount * 2).array();

        try (SolarmanLoggerConnection solarmanLoggerConnection = solarmanLoggerConnector.createConnection()) {
            logger.debug("Writing data {} to {} logger register(s) starting from 0x{}", HexUtils.bytesToHex(data),
                    registerCount, String.format("%04X", firstRegister));

            if (!solarmanLoggerConnection.isConnected()) {
                throw new SolarmanConnectionException("Unable to connect to logger");
            }

            if (solarmanProtocol.writeRegisters(solarmanLoggerConnection, firstRegister, data)) {
                logger.info("Successfully updated registers");
            } else {
                logger.error("Failed to update registers");
            }
        } catch (SolarmanException e) {
            logger.error("Failed to communicate with logger", e);
        }
    }

    private void logUnexpectedCommand(ChannelUID uid, Command command) {
        logger.warn("Received unexpected command {} in channel {}", command, uid);
    }
}
