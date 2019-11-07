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

import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

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

    public static State toQuantityType(@Nullable Float value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(@Nullable Double value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(@Nullable BigDecimal value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
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

}
