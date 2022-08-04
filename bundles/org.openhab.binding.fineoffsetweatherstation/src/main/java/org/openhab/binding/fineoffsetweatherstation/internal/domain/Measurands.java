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
package org.openhab.binding.fineoffsetweatherstation.internal.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetWeatherStationBindingConstants;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

/**
 * Holds all the measurands supported by the gateway.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class Measurands {

    private static final Map<Protocol, Measurands> INSTANCES = new HashMap<>();
    private final Map<Byte, List<Parser>> parsersPerCode = new HashMap<>();

    private Measurands(Protocol protocol) {
        try (InputStream data = Measurands.class.getResourceAsStream("/measurands.csv")) {
            if (data == null) {
                throw new IllegalStateException("Missing measurands.csv");
            }
            CSVFormat csvFormat = CSVFormat.Builder.create().setHeader().setSkipHeaderRecord(true).build();
            CSVParser.parse(new InputStreamReader(data), csvFormat).forEach(row -> {

                byte code = Byte.valueOf(row.get("Code").replace("0x", ""), 16);
                Optional<Integer> skip = Optional.ofNullable(row.get("Skip")).filter(Predicate.not(String::isBlank))
                        .map(Integer::valueOf);
                int index = Optional.ofNullable(row.get("Index")).filter(Predicate.not(String::isBlank))
                        .map(Integer::valueOf).orElse(0);

                Parser parser;
                if (skip.isPresent()) {
                    parser = new Skip(skip.get(), index);
                } else {
                    String name = row.get("Name");
                    String channel = row.get("Channel");

                    ChannelTypeUID channelType = Optional.ofNullable(row.get("ChannelType"))
                            .filter(Predicate.not(String::isBlank)).map(s -> {
                                if (s.contains(":")) {
                                    return new ChannelTypeUID(s);
                                } else {
                                    return new ChannelTypeUID(FineOffsetWeatherStationBindingConstants.BINDING_ID, s);
                                }
                            }).orElse(null);
                    String measurandString = protocol == Protocol.DEFAULT ? row.get("MeasureType_DEFAULT")
                            : Optional.ofNullable(row.get("MeasureType_" + protocol.name()))
                                    .filter(Predicate.not(String::isBlank))
                                    .orElseGet(() -> row.get("MeasureType_DEFAULT"));
                    parser = new MeasurandParser(channel, name, MeasureType.valueOf(measurandString), index,
                            channelType);
                }

                List<Parser> parsers = parsersPerCode.computeIfAbsent(code, aByte -> new ArrayList<>());
                // noinspection ConstantConditions
                if (parsers != null) {
                    parsers.add(parser);
                }
            });
            for (List<Parser> parsers : parsersPerCode.values()) {
                parsers.sort(Comparator.comparing(Parser::getIndex));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read measurands.csv", e);
        }
    }

    public static Measurands getInstance(Protocol protocol) {
        synchronized (INSTANCES) {
            return Objects.requireNonNull(INSTANCES.computeIfAbsent(protocol, Measurands::new));
        }
    }

    private abstract static class Parser {
        private final int index;

        public Parser(int index) {
            this.index = index;
        }

        public abstract int extractMeasuredValues(byte[] data, int offset, ConversionContext context,
                List<MeasuredValue> result);

        public int getIndex() {
            return index;
        }
    }

    private static class Skip extends Parser {
        private final int skip;

        public Skip(int skip, int index) {
            super(index);
            this.skip = skip;
        }

        @Override
        public int extractMeasuredValues(byte[] data, int offset, ConversionContext context,
                List<MeasuredValue> result) {
            return skip;
        }
    }

    private static class MeasurandParser extends Parser {
        private final String name;
        private final String channelId;
        private final MeasureType measureType;
        private final @Nullable ChannelTypeUID channelTypeUID;

        MeasurandParser(String channelId, String name, MeasureType measureType, int index,
                @Nullable ChannelTypeUID channelTypeUID) {
            super(index);
            this.channelId = channelId;
            this.name = name;
            this.measureType = measureType;
            this.channelTypeUID = channelTypeUID == null ? measureType.getChannelTypeId() : channelTypeUID;
        }

        public int extractMeasuredValues(byte[] data, int offset, ConversionContext context,
                List<MeasuredValue> result) {
            State state = measureType.toState(data, offset, context);
            if (state != null) {
                result.add(new MeasuredValue(measureType, channelId, channelTypeUID, state, name));
            }
            return measureType.getByteSize();
        }
    }

    public int extractMeasuredValues(byte code, byte[] data, int offset, ConversionContext context,
            List<MeasuredValue> result) {
        List<Parser> parsers = parsersPerCode.get(code);
        if (parsers == null) {
            throw new IllegalArgumentException("No measurement for code 0x" + Integer.toHexString(code) + " defined");
        }
        int subOffset = 0;
        for (Parser parser : parsers) {
            subOffset += parser.extractMeasuredValues(data, offset + subOffset, context, result);
        }
        return subOffset;
    }
}
