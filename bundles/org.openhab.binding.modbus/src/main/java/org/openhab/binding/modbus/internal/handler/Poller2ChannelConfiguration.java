/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.internal.handler;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusConstants.ValueType;

/**
 * Validates and creates one configured poller-owned numeric channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class Poller2ChannelConfiguration {

    private final Optional<Poller2Channel> channel;
    private final Optional<String> error;

    private Poller2ChannelConfiguration(Optional<Poller2Channel> channel, Optional<String> error) {
        this.channel = channel;
        this.error = error;
    }

    public static Poller2ChannelConfiguration create(String channelId, Map<String, Object> configuration, int pollStart,
            int pollLength) {
        Object address = configuration.get("address");
        if (!(address instanceof String addressText) || addressText.isBlank()) {
            return error("Channel '%s' is missing required configuration 'address'".formatted(channelId));
        }
        Object valueType = configuration.get("valueType");
        if (!(valueType instanceof String valueTypeText) || valueTypeText.isBlank()) {
            return error("Channel '%s' is missing required configuration 'valueType'".formatted(channelId));
        }

        final ValueType parsedValueType;
        try {
            parsedValueType = ValueType.fromConfigValue(valueTypeText);
        } catch (IllegalArgumentException e) {
            return error("Channel '%s' has invalid valueType '%s'".formatted(channelId, valueTypeText));
        }

        final Poller2Channel channel;
        try {
            channel = new Poller2Channel(addressText, parsedValueType, PollerReadFailurePolicy.UNDEF);
        } catch (IllegalArgumentException e) {
            return error("Channel '%s' has invalid address '%s'; expected X or X.Y".formatted(channelId, addressText));
        }
        Optional<String> rangeError = channel.validateReadRange(pollStart, pollLength);
        return rangeError.map(message -> error("Channel '%s': %s".formatted(channelId, message)))
                .orElseGet(() -> new Poller2ChannelConfiguration(Optional.of(channel), Optional.empty()));
    }

    private static Poller2ChannelConfiguration error(String message) {
        return new Poller2ChannelConfiguration(Optional.empty(), Optional.of(message));
    }

    public Optional<Poller2Channel> channel() {
        return channel;
    }

    public Optional<String> error() {
        return error;
    }
}
