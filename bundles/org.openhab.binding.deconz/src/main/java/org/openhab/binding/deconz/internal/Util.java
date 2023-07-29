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
package org.openhab.binding.deconz.internal;

import static org.openhab.binding.deconz.internal.BindingConstants.BRIGHTNESS_FACTOR;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link Util} class defines common utility methods
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class Util {

    public static String buildUrl(String host, int port, String... urlParts) {
        StringBuilder url = new StringBuilder();
        url.append("http://");
        url.append(host).append(":").append(port);
        url.append("/api/");
        if (urlParts.length > 0) {
            url.append(Stream.of(urlParts).filter(s -> s != null && !s.isEmpty()).collect(Collectors.joining("/")));
        }

        return url.toString();
    }

    public static int miredToKelvin(int miredValue) {
        return (int) (1000000.0 / miredValue);
    }

    public static int kelvinToMired(int kelvinValue) {
        return (int) (1000000.0 / kelvinValue);
    }

    public static int constrainToRange(int intValue, int min, int max) {
        return Math.max(min, Math.min(intValue, max));
    }

    /**
     * Convert a brightness value from int to PercentType
     *
     * @param val the value
     * @return the corresponding PercentType value
     */
    public static PercentType toPercentType(int val) {
        int scaledValue = (int) Math.ceil(val / BRIGHTNESS_FACTOR);
        return new PercentType(
                constrainToRange(scaledValue, PercentType.ZERO.intValue(), PercentType.HUNDRED.intValue()));
    }

    /**
     * Convert a brightness value from PercentType to int
     *
     * @param val the value
     * @return the corresponding int value
     */
    public static int fromPercentType(PercentType val) {
        return (int) Math.floor(val.doubleValue() * BRIGHTNESS_FACTOR);
    }

    /**
     * Convert a timestamp string to a DateTimeType
     *
     * @param timestamp either in zoned date time or local date time format
     * @return the corresponding DateTimeType
     */
    public static DateTimeType convertTimestampToDateTime(String timestamp) {
        if (timestamp.endsWith("Z")) {
            return new DateTimeType(ZonedDateTime.parse(timestamp));
        } else {
            return new DateTimeType(
                    ZonedDateTime.ofInstant(LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            ZoneOffset.UTC, ZoneId.systemDefault()));
        }
    }

    /**
     * Get all keys corresponding to a given value of a map
     *
     * @param map a map
     * @param value the value to find in the map
     * @return Stream of all keys for the value
     */
    public static <@NonNull K, @NonNull V> Stream<K> getKeysFromValue(Map<K, V> map, V value) {
        return map.entrySet().stream().filter(e -> e.getValue().equals(value)).map(Map.Entry::getKey);
    }
}
