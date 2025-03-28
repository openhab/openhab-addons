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
package org.openhab.binding.bambulab.internal;

import static java.lang.Integer.parseInt;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;
import static org.openhab.core.types.UnDefType.UNDEF;
import static tech.units.indriya.unit.Units.PERCENT;

import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class StateParserHelper {
    private static final Pattern DBM_PATTERN = Pattern.compile("^(-?\\d+)dBm$");
    private static final Logger logger = LoggerFactory.getLogger(StateParserHelper.class);

    public static Optional<State> parseTimeMinutes(@Nullable Integer integer) {
        return Optional.ofNullable(integer)//
                .map(time -> new QuantityType<>(time, MINUTE));
    }

    public static Optional<State> parseTemperatureType(@Nullable String value) {
        try {
            return Optional.ofNullable(value).map(Double::parseDouble).map(d -> new QuantityType<>(d, CELSIUS));
        } catch (NumberFormatException ex) {
            logger.debug("Cannot parse: {}", value, ex);
            return Optional.of(UNDEF);
        }
    }

    public static Optional<State> parseDecimalType(@Nullable Number number) {
        return Optional.ofNullable(number).map(DecimalType::new);
    }

    public static Optional<State> parseDecimalType(@Nullable String value) {
        try {
            return Optional.ofNullable(value).map(DecimalType::new);
        } catch (NumberFormatException ex) {
            logger.debug("Cannot parse {}", value, ex);
            return Optional.of(UNDEF);
        }
    }

    public static Optional<State> parsePercentType(@Nullable Number percent) {
        return Optional.ofNullable(percent).map(d -> new QuantityType<>(d, PERCENT));
    }

    public static Optional<State> parsePercentType(@Nullable String percent) {
        try {
            return Optional.ofNullable(percent)//
                    .map(Double::parseDouble)//
                    .map(d -> new QuantityType<>(d, PERCENT));
        } catch (NumberFormatException ex) {
            logger.debug("Cannot parse {}", percent, ex);
            return Optional.of(UNDEF);
        }
    }

    public static Optional<State> parseStringType(@Nullable String string) {
        return Optional.ofNullable(string).map(StringType::new);
    }

    public static State parseWifiChannel(String wifi) {
        var matcher = DBM_PATTERN.matcher(wifi);
        if (!matcher.matches()) {
            logger.debug("Cannot match {} to {}", wifi, DBM_PATTERN);
            return UNDEF;
        }

        var integer = matcher.group(1);
        try {
            var value = parseInt(integer);
            return new QuantityType<>(value, DECIBEL_MILLIWATTS);
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse integer {} from wifi {}", integer, wifi, e);
            return UNDEF;
        }
    }

    public static State parseColor(String colorHex) {
        if (colorHex.length() < 6) {
            return UNDEF;
        }
        int r = parseInt(colorHex.substring(0, 2), 16);
        int g = parseInt(colorHex.substring(2, 4), 16);
        int b = parseInt(colorHex.substring(4, 6), 16);
        return ColorUtil.rgbToHsb(new int[] { r, g, b });
    }

    public static Optional<State> undef() {
        return Optional.of(UNDEF);
    }
}
