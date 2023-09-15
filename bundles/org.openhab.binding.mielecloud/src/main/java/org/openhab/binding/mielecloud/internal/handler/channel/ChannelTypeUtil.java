/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.handler.channel;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.Quantity;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class handling type conversions from Java types to channel types.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class ChannelTypeUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChannelTypeUtil.class);

    private ChannelTypeUtil() {
        throw new IllegalStateException("ChannelTypeUtil cannot be instantiated.");
    }

    /**
     * Converts an {@link Optional} of {@link String} to {@link State}.
     */
    public static State stringToState(Optional<String> value) {
        return value.filter(v -> !v.isEmpty()).filter(v -> !"null".equals(v)).map(v -> (State) new StringType(v))
                .orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Boolean} to {@link State}.
     */
    public static State booleanToState(Optional<Boolean> value) {
        return value.map(v -> (State) OnOffType.from(v)).orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Integer} to {@link State}.
     */
    public static State intToState(Optional<Integer> value) {
        return value.map(v -> (State) new DecimalType(new BigDecimal(v))).orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Long} to {@link State}.
     */
    public static State longToState(Optional<Long> value) {
        return value.map(v -> (State) new DecimalType(new BigDecimal(v))).orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Integer} to {@link State} representing a temperature.
     */
    public static State intToTemperatureState(Optional<Integer> value) {
        // The Miele 3rd Party API always provides temperatures in °C (even if the device uses another unit).
        return value.map(v -> (State) new QuantityType<>(v, SIUnits.CELSIUS)).orElse(UnDefType.UNDEF);
    }

    /**
     * Converts an {@link Optional} of {@link Quantity} to {@link State}.
     */
    public static State quantityToState(Optional<Quantity> value) {
        return value.flatMap(ChannelTypeUtil::formatQuantity).flatMap(ChannelTypeUtil::parseQuantityType)
                .orElse(UnDefType.UNDEF);
    }

    /**
     * Formats the quantity as "value unit" with the given locale.
     *
     * @param locale The locale to format with.
     * @return An {@link Optional} containing the formatted quantity value or an empty {@link Optional} if formatting
     *         for the given locale failed.
     */
    private static Optional<String> formatQuantity(Quantity quantity) {
        double value = quantity.getValue();
        try {
            var formatted = NumberFormat.getInstance(Locale.ENGLISH).format(value);

            var unit = quantity.getUnit();
            if (unit.isPresent()) {
                formatted = formatted + " " + unit.get();
            }

            return Optional.of(formatted);
        } catch (ArithmeticException e) {
            LOGGER.warn("Failed to format {}", value, e);
            return Optional.empty();
        }
    }

    /**
     * Parses a previously formatted {@link Quantity} into a {@link State}.
     * 
     * @param value The quantity value formatted as "value unit".
     * @return An {@link Optional} containing the parsed {@link State} or an empty {@link Optional} if the quantity
     *         including unit could not be parsed.
     */
    private static Optional<State> parseQuantityType(String value) {
        try {
            return Optional.of((State) new QuantityType<>(value));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to convert {} to quantity: {}", value, e.getMessage(), e);
            return Optional.empty();
        }
    }
}
