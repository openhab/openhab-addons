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

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Validates and creates one coil or discrete-input poller-owned channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class Poller2BitChannelConfiguration {

    private final @Nullable Poller2BitChannel channel;
    private final @Nullable String error;

    private Poller2BitChannelConfiguration(@Nullable Poller2BitChannel channel, @Nullable String error) {
        this.channel = channel;
        this.error = error;
    }

    public static Poller2BitChannelConfiguration create(String channelId, Map<String, Object> configuration,
            int pollStart, int pollLength) {
        Object address = configuration.get("address");
        if (!(address instanceof String addressText) || addressText.isBlank()) {
            return error("Channel '%s' is missing required configuration 'address'".formatted(channelId));
        }
        if (addressText.contains(".")) {
            return error("Channel '%s' has invalid bit address '%s'; expected X".formatted(channelId, addressText));
        }
        Object valueType = configuration.get("valueType");
        if (valueType != null && !"bit".equals(valueType)) {
            return error("Channel '%s' has invalid valueType '%s'; coils and discrete inputs are bits"
                    .formatted(channelId, valueType));
        }

        final int parsedAddress;
        try {
            parsedAddress = Integer.parseInt(addressText);
        } catch (NumberFormatException e) {
            return error("Channel '%s' has invalid bit address '%s'; expected X".formatted(channelId, addressText));
        }
        int pollEnd = pollStart + pollLength - 1;
        if (parsedAddress < pollStart) {
            return error("Channel '%s': Address %d is before poll window %d..%d".formatted(channelId, parsedAddress,
                    pollStart, pollEnd));
        }
        if (parsedAddress > pollEnd) {
            return error("Channel '%s': Address %d is outside poll window %d..%d".formatted(channelId, parsedAddress,
                    pollStart, pollEnd));
        }
        return new Poller2BitChannelConfiguration(new Poller2BitChannel(parsedAddress, PollerReadFailurePolicy.UNDEF),
                null);
    }

    private static Poller2BitChannelConfiguration error(String message) {
        return new Poller2BitChannelConfiguration(null, message);
    }

    public Optional<Poller2BitChannel> channel() {
        return Optional.ofNullable(channel);
    }

    public Optional<String> error() {
        return Optional.ofNullable(error);
    }
}
