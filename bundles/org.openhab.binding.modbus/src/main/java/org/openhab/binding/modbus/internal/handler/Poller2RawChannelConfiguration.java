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
 * Validates and creates one configured poller-owned raw register channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public final class Poller2RawChannelConfiguration {
    private final @Nullable Poller2RawChannel channel;
    private final @Nullable String error;

    private Poller2RawChannelConfiguration(@Nullable Poller2RawChannel channel, @Nullable String error) {
        this.channel = channel;
        this.error = error;
    }

    public static Poller2RawChannelConfiguration create(String channelId, Map<String, Object> configuration,
            int pollStart, int pollLength) {
        Object addressValue = configuration.get("address");
        if (!(addressValue instanceof String addressText) || addressText.isBlank()) {
            return error("Channel '%s' is missing required configuration 'address'".formatted(channelId));
        }
        Object lengthValue = configuration.get("length");
        if (!(lengthValue instanceof Number number) || number.longValue() != number.doubleValue()
                || number.longValue() < 1 || number.longValue() > Integer.MAX_VALUE) {
            return error("Channel '%s' has invalid length '%s'; expected a positive integer".formatted(channelId,
                    lengthValue));
        }
        final int address;
        try {
            address = Integer.parseInt(addressText);
        } catch (NumberFormatException e) {
            return error("Channel '%s' has invalid raw address '%s'; expected X".formatted(channelId, addressText));
        }
        final PollerReadFailurePolicy failurePolicy;
        Object errorPolicy = configuration.getOrDefault("errorPolicy", "undef");
        if ("undef".equals(errorPolicy)) {
            failurePolicy = PollerReadFailurePolicy.UNDEF;
        } else if ("keepLast".equals(errorPolicy)) {
            failurePolicy = PollerReadFailurePolicy.KEEP_LAST;
        } else {
            return error("Channel '%s' has invalid errorPolicy '%s'; expected 'undef' or 'keepLast'"
                    .formatted(channelId, errorPolicy));
        }
        Poller2RawChannel channel = new Poller2RawChannel(address, number.intValue(), failurePolicy);
        Optional<String> rangeError = channel.validateReadRange(pollStart, pollLength);
        return rangeError.map(message -> error("Channel '%s': %s".formatted(channelId, message)))
                .orElseGet(() -> new Poller2RawChannelConfiguration(channel, null));
    }

    private static Poller2RawChannelConfiguration error(String message) {
        return new Poller2RawChannelConfiguration(null, message);
    }

    public Optional<Poller2RawChannel> channel() {
        return Optional.ofNullable(channel);
    }

    public Optional<String> error() {
        return Optional.ofNullable(error);
    }
}
