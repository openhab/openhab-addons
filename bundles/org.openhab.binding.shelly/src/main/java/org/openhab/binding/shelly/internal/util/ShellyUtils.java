/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.util;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;

/**
 * {@link ShellyUtils} provides general utility functions
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyUtils {
    public static String mkChannelId(String group, String channel) {
        return group + "#" + channel;
    }

    public static String getString(@Nullable String value) {
        return value != null ? value : "";
    }

    public static String getMessage(Exception e) {
        String message = e.getMessage();
        return message != null ? message : "";
    }

    public static Integer getInteger(@Nullable Integer value) {
        return (value != null ? (Integer) value : 0);
    }

    public static Long getLong(@Nullable Long value) {
        return (value != null ? (Long) value : 0);
    }

    public static Double getDouble(@Nullable Double value) {
        return (value != null ? (Double) value : 0);
    }

    public static Boolean getBool(@Nullable Boolean value) {
        return (value != null ? (Boolean) value : false);
    }

    // as State

    public static StringType getStringType(@Nullable String value) {
        return new StringType(value != null ? value : "");
    }

    public static DecimalType getDecimal(@Nullable Double value) {
        return new DecimalType((value != null ? value : 0));
    }

    public static DecimalType getDecimal(@Nullable Integer value) {
        return new DecimalType((value != null ? value : 0));
    }

    public static DecimalType getDecimal(@Nullable Long value) {
        return new DecimalType((value != null ? value : 0));
    }

    public static OnOffType getOnOff(@Nullable Boolean value) {
        return (value != null ? value ? OnOffType.ON : OnOffType.OFF : OnOffType.OFF);
    }

    public static OnOffType getOnOff(int value) {
        return value == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    public static State toQuantityType(@Nullable Double value, int digits, Unit<?> unit) {
        if (value == null) {
            return UnDefType.NULL;
        }
        BigDecimal bd = new BigDecimal(value.doubleValue());
        return toQuantityType(bd.setScale(digits, BigDecimal.ROUND_HALF_UP), unit);
    }

    public static State toQuantityType(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    public static State toQuantityType(@Nullable PercentType value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(value.toBigDecimal(), unit);
    }

    public static void validateRange(String name, Integer value, int min, int max) {
        if ((value < min) || (value > max)) {
            throw new IllegalArgumentException("Value " + name + " is out of range (" + min + "-" + max + ")");
        }
    }

    public static String urlEncode(String input) throws ShellyApiException {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new ShellyApiException(e,
                    "Unsupported encoding format: " + StandardCharsets.UTF_8.toString() + ", input=" + input);
        }
    }

    public static Long now() {
        return System.currentTimeMillis() / 1000L;
    }

    public static DateTimeType getTimestamp() {
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(now()), ZoneId.systemDefault()));
    }

    public static DateTimeType getTimestamp(String zone, long timestamp) {
        try {
            if (timestamp == 0) {
                return getTimestamp();
            }
            ZoneId zoneId = !zone.isEmpty() ? ZoneId.of(zone) : ZoneId.systemDefault();
            ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
            int delta = zdt.getOffset().getTotalSeconds();
            return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp - delta), zoneId));
        } catch (DateTimeException e) {
            // Unable to convert device's timezone, use system one
            return getTimestamp();
        }
    }

    public static Integer getLightIdFromGroup(String groupName) {
        if (groupName.startsWith(CHANNEL_GROUP_LIGHT_CHANNEL)) {
            return Integer.parseInt(StringUtils.substringAfter(groupName, CHANNEL_GROUP_LIGHT_CHANNEL)) - 1;
        }
        return 0; // only 1 light, e.g. bulb or rgbw2 in color mode
    }

    public static String buildControlGroupName(ShellyDeviceProfile profile, Integer channelId) {
        return profile.isBulb || profile.isDuo || profile.inColor ? CHANNEL_GROUP_LIGHT_CONTROL
                : CHANNEL_GROUP_LIGHT_CHANNEL + channelId.toString();
    }

    public static String buildWhiteGroupName(ShellyDeviceProfile profile, Integer channelId) {
        return profile.isBulb || profile.isDuo && !profile.inColor ? CHANNEL_GROUP_WHITE_CONTROL
                : CHANNEL_GROUP_LIGHT_CHANNEL + channelId.toString();
    }

    public static DecimalType mapSignalStrength(int dbm) {
        int strength = -1;
        if (dbm > -60) {
            strength = 4;
        } else if (dbm > -70) {
            strength = 3;
        } else if (dbm > -80) {
            strength = 2;
        } else if (dbm > -90) {
            strength = 1;
        } else {
            strength = 0;
        }
        return new DecimalType(strength);
    }
}
