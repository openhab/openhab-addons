/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
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

    @SuppressWarnings("null")
    public static State toQuantityType(@Nullable Double value, int digits, Unit<?> unit) {
        BigDecimal bd = new BigDecimal(value.doubleValue());
        return value == null ? UnDefType.NULL : toQuantityType(bd.setScale(digits, BigDecimal.ROUND_HALF_DOWN), unit);
    }

    public static State toQuantityType(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    public static State toQuantityType(@Nullable PercentType value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(value.toBigDecimal(), unit);
    }

    public static void validateRange(String name, Integer value, Integer min, Integer max) {
        Validate.isTrue((value >= min) && (value <= max),
                "Value " + name + " is out of range (" + min.toString() + "-" + max.toString() + ")");
    }

    public static String buildSetEventUrl(String localIp, String localPort, String deviceName, Integer index,
            String deviceType, String urlParm) throws IOException {
        return SHELLY_URL_SETTINGS + "/" + deviceType + "/" + index + "?" + urlParm + "="
                + buildCallbackUrl(localIp, localPort, deviceName, index, deviceType, urlParm);
    }

    public static String buildCallbackUrl(String localIp, String localPort, String deviceName, Integer index,
            String type, String parameter) throws IOException {
        String url = "http://" + localIp + ":" + localPort + SHELLY_CALLBACK_URI + "/" + deviceName + "/" + type + "/"
                + index + "?type=" + StringUtils.substringBefore(parameter, "_url");
        return urlEncode(url);
    }

    public static String urlEncode(String input) throws IOException {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new IOException(
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
        ZoneId zoneId = ZoneId.of(zone);
        ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
        int delta = zdt.getOffset().getTotalSeconds();
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp - delta), zoneId));
    }

    public static Integer getLightIdFromGroup(@Nullable String groupName) {
        Validate.notNull(groupName);
        if (groupName.startsWith(CHANNEL_GROUP_LIGHT_CHANNEL)) {
            return Integer.parseInt(StringUtils.substringAfter(groupName, CHANNEL_GROUP_LIGHT_CHANNEL)) - 1;
        }
        return 0; // only 1 light, e.g. bulb or rgbw2 in color mode
    }

    public static String buildControlGroupName(@Nullable ShellyDeviceProfile profile, Integer channelId) {
        Validate.notNull(profile);
        return profile.isBulb || profile.inColor ? CHANNEL_GROUP_LIGHT_CONTROL
                : CHANNEL_GROUP_LIGHT_CHANNEL + channelId.toString();
    }

    public static String buildWhiteGroupName(@Nullable ShellyDeviceProfile profile, Integer channelId) {
        Validate.notNull(profile);
        return profile.isBulb && !profile.inColor ? CHANNEL_GROUP_WHITE_CONTROL
                : CHANNEL_GROUP_LIGHT_CHANNEL + channelId.toString();
    }
}
