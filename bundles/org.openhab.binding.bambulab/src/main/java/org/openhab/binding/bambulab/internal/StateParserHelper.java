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

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.util.ColorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class StateParserHelper {
    private static final Pattern DBM_PATTERN = Pattern.compile("^(-?\\d+)dBm$");
    private static final Logger LOGGER = LoggerFactory.getLogger(StateParserHelper.class);

    public static Optional<State> parseTimeMinutes(@Nullable Integer integer) {
        return Optional.ofNullable(integer)//
                .map(time -> new QuantityType<>(time, MINUTE));
    }

    public static Optional<State> parseTemperatureType(@Nullable Number value) {
        return Optional.ofNullable(value).map(d -> new QuantityType<>(d, CELSIUS));
    }

    public static Optional<State> parseTemperatureType(@Nullable String value) {
        try {
            return Optional.ofNullable(value)//
                    .map(Double::parseDouble)//
                    .flatMap(StateParserHelper::parseTemperatureType);
        } catch (NumberFormatException ex) {
            LOGGER.debug("Cannot parse: {}", value, ex);
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
            LOGGER.debug("Cannot parse {}", value, ex);
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
            LOGGER.debug("Cannot parse {}", percent, ex);
            return Optional.of(UNDEF);
        }
    }

    public static Optional<State> parseStringType(@Nullable String string) {
        return Optional.ofNullable(string).map(StringType::new);
    }

    public static Optional<State> parseWifiChannel(@Nullable String wifi) {
        if (wifi == null) {
            return Optional.empty();
        }
        var matcher = DBM_PATTERN.matcher(wifi);
        if (!matcher.matches()) {
            LOGGER.debug("Cannot match {} to {}", wifi, DBM_PATTERN);
            return Optional.of(UNDEF);
        }

        var integer = matcher.group(1);
        try {
            var value = parseInt(integer);
            return Optional.of(new QuantityType<>(value, DECIBEL_MILLIWATTS));
        } catch (NumberFormatException e) {
            LOGGER.debug("Cannot parse integer {} from wifi {}", integer, wifi, e);
            return Optional.of(UNDEF);
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

    public static Optional<State> parseOnOffType(@Nullable Boolean bool) {
        return Optional.ofNullable(bool).map(OnOffType::from);
    }

    public static Optional<State> parseSpeedLevel(@Nullable Integer speedLvl) {
        return Optional.ofNullable(speedLvl)//
                .map(PrintSpeedCommand::findByLevel)//
                .map(lvl -> {
                    if (!lvl.canSend()) {
                        return UNDEF;
                    }
                    return new StringType(lvl.getName());
                });
    }

    public static Optional<State> parseTrayType(@Nullable String trayType) {
        return Optional.ofNullable(trayType).map(Object::toString)//
                .flatMap(BambuLabBindingConstants.AmsChannel.TrayType::findTrayType)//
                .map(Enum::name)//
                .flatMap(StateParserHelper::parseStringType);
    }

    public static Optional<State> parseChamberLightType(@Nullable List<Map<String, String>> lights) {
        return parseLightType("chamber_light", lights);
    }

    public static Optional<State> parseWorkLightType(@Nullable List<Map<String, String>> lights) {
        return parseLightType("work_light", lights);
    }

    private static Optional<State> parseLightType(String lightName, @Nullable List<Map<String, String>> lights) {
        return Optional.ofNullable(lights)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(map -> lightName.equalsIgnoreCase(map.get("node")))//
                .map(map -> map.get("mode"))//
                .filter(Objects::nonNull)//
                .map(OnOffType::from)//
                .map(state -> (State) state)//
                .findAny();
    }

    public static State parseDateTimeType(ZonedDateTime dateTime) {
        return new DateTimeType(dateTime);
    }
}
