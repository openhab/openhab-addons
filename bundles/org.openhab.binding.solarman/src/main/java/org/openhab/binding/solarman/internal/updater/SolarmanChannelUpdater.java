/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solarman.internal.defmodel.Lookup;
import org.openhab.binding.solarman.internal.defmodel.ParameterItem;
import org.openhab.binding.solarman.internal.defmodel.Request;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnection;
import org.openhab.binding.solarman.internal.modbus.SolarmanLoggerConnector;
import org.openhab.binding.solarman.internal.modbus.SolarmanProtocol;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanConnectionException;
import org.openhab.binding.solarman.internal.modbus.exception.SolarmanException;
import org.openhab.binding.solarman.internal.typeprovider.ChannelUtils;
import org.openhab.binding.solarman.internal.util.StreamUtils;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Catalin Sanda - Initial contribution
 */
@NonNullByDefault
public class SolarmanChannelUpdater {
    private final Logger logger = LoggerFactory.getLogger(SolarmanChannelUpdater.class);
    private final StateUpdater stateUpdater;

    public SolarmanChannelUpdater(StateUpdater stateUpdater) {
        this.stateUpdater = stateUpdater;
    }

    public SolarmanProcessResult fetchDataFromLogger(List<Request> requests,
            SolarmanLoggerConnector solarmanLoggerConnector, SolarmanProtocol solarmanProtocol,
            Map<ParameterItem, ChannelUID> paramToChannelMapping) {
        try (SolarmanLoggerConnection solarmanLoggerConnection = solarmanLoggerConnector.createConnection()) {
            logger.debug("Fetching data from logger");

            if (!solarmanLoggerConnection.isConnected()) {
                return SolarmanProcessResult.ofException(Request.NONE,
                        new SolarmanConnectionException("Unable to connect to logger"));
            }

            SolarmanProcessResult solarmanProcessResult = requests.stream().map(request -> {
                try {
                    return SolarmanProcessResult.ofValue(request,
                            solarmanProtocol.readRegisters(solarmanLoggerConnection,
                                    (byte) request.getMbFunctioncode().intValue(), request.getStart(),
                                    request.getEnd()));
                } catch (SolarmanException e) {
                    return SolarmanProcessResult.ofException(request, e);
                }
            }).reduce(new SolarmanProcessResult(), SolarmanProcessResult::merge);

            if (solarmanProcessResult.hasSuccessfulResponses()) {
                updateChannelsForReadRegisters(paramToChannelMapping, solarmanProcessResult.getReadRegistersMap());
            }
            return solarmanProcessResult;
        }
    }

    private void updateChannelsForReadRegisters(Map<ParameterItem, ChannelUID> paramToChannelMapping,
            Map<Integer, byte[]> readRegistersMap) {
        paramToChannelMapping.forEach((parameterItem, channelUID) -> {
            List<Integer> registers = parameterItem.getRegisters();
            if (readRegistersMap.keySet().containsAll(registers)) {
                switch (parameterItem.getRule()) {
                    case 1, 3 -> updateChannelWithNumericValue(parameterItem, channelUID, registers, readRegistersMap,
                            ValueType.UNSIGNED);
                    case 2, 4 -> updateChannelWithNumericValue(parameterItem, channelUID, registers, readRegistersMap,
                            ValueType.SIGNED);
                    case 5 -> updateChannelWithStringValue(channelUID, registers, readRegistersMap);
                    case 6 -> updateChannelWithRawValue(parameterItem, channelUID, registers, readRegistersMap);
                    case 7 -> updateChannelWithVersion(channelUID, registers, readRegistersMap);
                    case 8 -> updateChannelWithDateTime(channelUID, registers, readRegistersMap);
                    case 9 -> updateChannelWithTime(channelUID, registers, readRegistersMap);
                }
            } else {
                logger.warn("Unable to update channel {} because its registers were not read", channelUID.getId());
            }
        });
    }

    private void updateChannelWithTime(ChannelUID channelUID, List<Integer> registers,
            Map<Integer, byte[]> readRegistersMap) {
        String stringValue = registers.stream().map(readRegistersMap::get).map(v -> ByteBuffer.wrap(v).getShort())
                .map(rawVal -> String.format("%02d", rawVal / 100) + ":" + String.format("%02d", rawVal % 100))
                .collect(Collectors.joining());

        logger.debug("Update state: channelUID: {}, state: {}", channelUID.getAsString(), stringValue);
        stateUpdater.updateState(channelUID, new StringType(stringValue));
    }

    private void updateChannelWithDateTime(ChannelUID channelUID, List<Integer> registers,
            Map<Integer, byte[]> readRegistersMap) {
        String stringValue = StreamUtils.zip(IntStream.range(0, registers.size()).boxed(),
                registers.stream().map(readRegistersMap::get).map(v -> ByteBuffer.wrap(v).getShort()),
                StreamUtils.Tuple::new).map(t -> {
                    int index = t.a();
                    short rawVal = t.b();

                    return switch (index) {
                        case 0 -> (rawVal >> 8) + "/" + (rawVal & 0xFF) + "/";
                        case 1 -> (rawVal >> 8) + " " + (rawVal & 0xFF) + ":";
                        case 2 -> (rawVal >> 8) + ":" + (rawVal & 0xFF);
                        default -> (rawVal >> 8) + "" + (rawVal & 0xFF);
                    };
                }).collect(Collectors.joining());

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy/M/d H:m:s");
            LocalDateTime dateTime = LocalDateTime.parse(stringValue, formatter);

            logger.debug("Update state: channelUID: {}, state: {}", channelUID.getAsString(), dateTime.toString());
            stateUpdater.updateState(channelUID, new DateTimeType(dateTime.atZone(ZoneId.systemDefault())));
        } catch (DateTimeParseException e) {
            logger.debug("Unable to parse string date {} to a DateTime object", stringValue);
        }
    }

    private void updateChannelWithVersion(ChannelUID channelUID, List<Integer> registers,
            Map<Integer, byte[]> readRegistersMap) {
        String stringValue = registers.stream().map(readRegistersMap::get).map(v -> ByteBuffer.wrap(v).getShort())
                .map(rawVal -> (rawVal >> 12) + "." + ((rawVal >> 8) & 0x0F) + "." + ((rawVal >> 4) & 0x0F) + "."
                        + (rawVal & 0x0F))
                .collect(Collectors.joining());

        logger.debug("Update Version state: channelUID: {}, state: {}", channelUID.getAsString(), stringValue);
        stateUpdater.updateState(channelUID, new StringType(stringValue));
    }

    private void updateChannelWithStringValue(ChannelUID channelUID, List<Integer> registers,
            Map<Integer, byte[]> readRegistersMap) {
        String stringValue = registers.stream().map(readRegistersMap::get).reduce(new StringBuilder(), (acc, val) -> {
            short shortValue = ByteBuffer.wrap(val).order(ByteOrder.BIG_ENDIAN).getShort();
            return acc.append((char) (shortValue >> 8)).append((char) (shortValue & 0xFF));
        }, StringBuilder::append).toString();

        logger.debug("Update String state: channelUID: {}, state: {}", channelUID.getAsString(), stringValue);
        stateUpdater.updateState(channelUID, new StringType(stringValue));
    }

    private void updateChannelWithNumericValue(ParameterItem parameterItem, ChannelUID channelUID,
            List<Integer> registers, Map<Integer, byte[]> readRegistersMap, ValueType valueType) {
        BigInteger value = extractNumericValue(registers, readRegistersMap, valueType);
        BigDecimal convertedValue = convertNumericValue(value, parameterItem.getOffset(), parameterItem.getScale());
        String uom = Objects.requireNonNullElse(parameterItem.getUom(), "");

        if (parameterItem.hasLookup()) {
            String stringValue = getStringFromLookupList(value.intValue(), parameterItem.getLookup());
            logger.debug("Update Lookup state: channelUID: {}, key: {}, state: {}", channelUID.getAsString(),
                    value.intValue(), stringValue);
            stateUpdater.updateState(channelUID, new StringType(stringValue));
        } else {
            State state;
            if (!uom.isBlank()) {
                try {
                    Unit<?> unitFromDefinition = ChannelUtils.getUnitFromDefinition(uom);
                    if (unitFromDefinition != null) {
                        state = new QuantityType<>(convertedValue, unitFromDefinition);
                    } else {
                        logger.debug("Unable to parse unit: {}", uom);
                        state = new DecimalType(convertedValue);
                    }
                } catch (MeasurementParseException e) {
                    state = new DecimalType(convertedValue);
                }
            } else {
                state = new DecimalType(convertedValue);
            }
            logger.debug("Update Numeric state: channelUID: {}, state: {}", channelUID.getAsString(),
                    state.toFullString());
            stateUpdater.updateState(channelUID, state);
        }
    }

    private @Nullable String getStringFromLookupList(int key, List<Lookup> lookupList) {
        return lookupList.stream().filter(lookup -> key == lookup.getKey()).map(Lookup::getValue).findFirst()
                .orElse("");
    }

    private void updateChannelWithRawValue(ParameterItem parameterItem, ChannelUID channelUID, List<Integer> registers,
            Map<Integer, byte[]> readRegistersMap) {
        String hexString = String.format("[%s]",
                reversed(registers).stream().map(readRegistersMap::get).map(
                        val -> String.format("0x%02X", ByteBuffer.wrap(val).order(ByteOrder.BIG_ENDIAN).getShort()))
                        .collect(Collectors.joining(",")));
        logger.debug("Update RawValue state: channelUID: {}, state: {}", channelUID.getAsString(), hexString);
        stateUpdater.updateState(channelUID, new StringType(hexString));
    }

    private BigDecimal convertNumericValue(BigInteger value, @Nullable BigDecimal offset, @Nullable BigDecimal scale) {
        return new BigDecimal(value).subtract(offset != null ? offset : BigDecimal.ZERO)
                .multiply(scale != null ? scale : BigDecimal.ONE);
    }

    private BigInteger extractNumericValue(List<Integer> registers, Map<Integer, byte[]> readRegistersMap,
            ValueType valueType) {
        return reversed(registers)
                .stream().map(readRegistersMap::get).reduce(
                        BigInteger.ZERO, (acc,
                                val) -> acc.shiftLeft(Short.SIZE)
                                        .add(BigInteger.valueOf(ByteBuffer.wrap(val).getShort()
                                                & (valueType == ValueType.UNSIGNED ? 0xFFFF : 0xFFFFFFFF))),
                        BigInteger::add);
    }

    private enum ValueType {
        UNSIGNED,
        SIGNED
    }

    @FunctionalInterface
    public interface StateUpdater {
        void updateState(ChannelUID channelUID, State state);
    }

    private <T> List<T> reversed(List<T> initialList) {
        List<T> reversedList = new ArrayList<>(initialList);
        Collections.reverse(reversedList);
        return reversedList;
    }
}
