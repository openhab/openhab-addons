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
package org.openhab.binding.modbus.internal.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;

/**
 * Validates an optional scalar write endpoint for a poller-owned channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class Poller2WriteChannelConfiguration {
    private final @Nullable Poller2WriteChannel channel;
    private final @Nullable ValueType valueType;
    private final @Nullable String error;

    private Poller2WriteChannelConfiguration(@Nullable Poller2WriteChannel channel, @Nullable ValueType valueType,
            @Nullable String error) {
        this.channel = channel;
        this.valueType = valueType;
        this.error = error;
    }

    public static Poller2WriteChannelConfiguration create(String channelId, Map<String, Object> configuration,
            String pollType) {
        Object writeStart = configuration.get("writeStart");
        if (writeStart == null || writeStart instanceof String text && text.isBlank()) {
            return new Poller2WriteChannelConfiguration(null, null, null);
        }
        if (!(writeStart instanceof String addressText)) {
            return error("Channel '%s' has invalid writeStart '%s'; expected a non-negative register or coil address"
                    .formatted(channelId, writeStart));
        }
        final int address;
        int bit = -1;
        try {
            String[] addressAndBit = addressText.split("\\.", -1);
            if (addressAndBit.length == 1 && addressAndBit[0].matches("[0-9]+")) {
                address = Integer.parseInt(addressAndBit[0]);
            } else if (addressAndBit.length == 2 && addressAndBit[0].matches("[0-9]+")
                    && addressAndBit[1].matches("[0-9]+")) {
                address = Integer.parseInt(addressAndBit[0]);
                bit = Integer.parseInt(addressAndBit[1]);
            } else {
                return error(
                        "Channel '%s' has invalid writeStart '%s'; expected a non-negative register or coil address"
                                .formatted(channelId, addressText));
            }
        } catch (IllegalArgumentException e) {
            return error("Channel '%s' has invalid writeStart '%s'; expected a non-negative register or coil address"
                    .formatted(channelId, addressText));
        }
        boolean coil = ModbusBindingConstantsInternal.READ_TYPE_COIL.equals(pollType);
        boolean holding = ModbusBindingConstantsInternal.READ_TYPE_HOLDING_REGISTER.equals(pollType);
        if (!coil && !holding) {
            return error("Channel '%s' writeStart is supported only by holding-register or coil pollers"
                    .formatted(channelId));
        }
        final ValueType valueType;
        if (coil) {
            if (bit >= 0) {
                return error("Channel '%s' coil writes do not support a bit sub-index".formatted(channelId));
            }
            Object configuredValueType = configuration.get("writeValueType");
            if (configuredValueType != null && !"bit".equals(configuredValueType)) {
                return error("Channel '%s' coil writes only support writeValueType 'bit'".formatted(channelId));
            }
            valueType = ValueType.BIT;
        } else {
            Object configuredValueType = configuration.get("writeValueType");
            if (!(configuredValueType instanceof String valueTypeText) || valueTypeText.isBlank()) {
                return error("Channel '%s' is missing required configuration 'writeValueType'".formatted(channelId));
            }
            if (bit >= 0) {
                if (bit >= Short.SIZE || !"bit".equals(valueTypeText)) {
                    return error(
                            "Channel '%s' holding-register bit writes require writeStart X.Y with Y from 0 to 15 and writeValueType 'bit'"
                                    .formatted(channelId));
                }
                valueType = ValueType.BIT;
            } else {
                try {
                    valueType = ValueType.fromConfigValue(valueTypeText);
                } catch (IllegalArgumentException e) {
                    return error("Channel '%s' has invalid writeValueType '%s'".formatted(channelId, valueTypeText));
                }
                if (valueType.getBits() < Short.SIZE) {
                    return error("Channel '%s' holding-register writes require writeValueType of at least 16 bits"
                            .formatted(channelId));
                }
            }
        }
        int maxTries = intConfiguration(configuration, "writeMaxTries", 3);
        if (maxTries < 1) {
            return error("Channel '%s' writeMaxTries must be at least 1".formatted(channelId));
        }
        boolean writeMultiple = Boolean.TRUE.equals(configuration.get("writeMultipleEvenWithSingleRegisterOrCoil"));
        List<String> transformation = transformation(configuration.get("writeTransform"));
        Poller2WriteChannel channel = bit >= 0
                ? new Poller2WriteChannel(address, bit, transformation, maxTries, writeMultiple)
                : new Poller2WriteChannel(address, valueType, transformation, maxTries, writeMultiple, coil);
        return new Poller2WriteChannelConfiguration(channel, valueType, null);
    }

    private static int intConfiguration(Map<String, Object> configuration, String key, int defaultValue) {
        Object value = configuration.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static List<String> transformation(@Nullable Object value) {
        if (value instanceof List<?> list && list.stream().allMatch(String.class::isInstance)) {
            return (List<String>) list;
        }
        return value instanceof String text ? List.of(text) : List.of("default");
    }

    private static Poller2WriteChannelConfiguration error(String message) {
        return new Poller2WriteChannelConfiguration(null, null, message);
    }

    public Optional<Poller2WriteChannel> channel() {
        return Optional.ofNullable(channel);
    }

    public Optional<ValueType> valueType() {
        return Optional.ofNullable(valueType);
    }

    public Optional<String> error() {
        return Optional.ofNullable(error);
    }
}
