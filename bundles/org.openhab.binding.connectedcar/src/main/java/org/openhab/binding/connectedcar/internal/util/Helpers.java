/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.connectedcar.internal.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;

import javax.measure.Unit;
import javax.xml.bind.DatatypeConverter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.UrlEncoded;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Helperfunctions
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class Helpers {
    private static final String PRE = "Can't create object of type ";

    public static <T> T fromJson(Gson gson, @Nullable String json, Class<T> classOfT) throws ApiException {
        String className = Helpers.substringAfter(classOfT.getName(), "$");

        if (json == null) {
            throw new ApiException(PRE + className + ": json is null!");
        }

        if (classOfT.isInstance(json)) {
            return wrap(classOfT).cast(json);
        } else if (json.isEmpty()) { // update GSON might return null
            throw new ApiException(PRE + className + "from empty JSON");
        } else {
            try {
                @Nullable
                T obj = gson.fromJson(json, classOfT);
                if (obj == null) { // new in OH3: fromJson may return null
                    throw new IllegalArgumentException(PRE + className + ": json is null!");
                }
                return obj;
            } catch (JsonSyntaxException e) {
                throw new ApiException(PRE + className + "from JSON (syntax/format error): " + getString(e.getMessage())
                        + ",JSON=" + json, e);
            } catch (RuntimeException e) {
                throw new ApiException(PRE + className + "from JSON: " + json, e);
            }
        }
    }

    public static String getString(@Nullable String value) {
        return value != null ? value : "";
    }

    public static String substringBefore(@Nullable String string, String pattern) {
        if (string != null) {
            int pos = string.indexOf(pattern);
            if (pos > 0) {
                return string.substring(0, pos);
            }
        }
        return "";
    }

    public static String substringBeforeLast(@Nullable String string, String pattern) {
        if (string != null) {
            int pos = string.lastIndexOf(pattern);
            if (pos > 0) {
                return string.substring(0, pos);
            }
        }
        return "";
    }

    public static String substringAfter(@Nullable String string, String pattern) {
        if (string != null) {
            int pos = string.indexOf(pattern);
            if (pos != -1) {
                return string.substring(pos + pattern.length());
            }
        }
        return "";
    }

    public static String substringAfterLast(@Nullable String string, String pattern) {
        if (string != null) {
            int pos = string.lastIndexOf(pattern);
            if (pos != -1) {
                return string.substring(pos + pattern.length());
            }
        }
        return "";
    }

    public static String substringBetween(@Nullable String string, String begin, String end) {
        if (string != null) {
            int s = string.indexOf(begin);
            if (s != -1) {
                // The end tag might be included before the start tag, e.g.
                // when using "http://" and ":" to get the IP from http://192.168.1.1:8081/xxx
                // therefore make it 2 steps
                String result = string.substring(s + begin.length());
                return substringBefore(result, end);
            }
        }
        return "";
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

    public static State getStringType(@Nullable String value) {
        return value != null ? new StringType(value) : UnDefType.UNDEF;
    }

    public static State getStringTypeNonEmpty(@Nullable String value) {
        return value != null && !value.isEmpty() ? new StringType(value) : UnDefType.UNDEF;
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

    public static DecimalType getDecimal(@Nullable String value) {
        return new DecimalType((value != null && !value.isEmpty() ? Integer.parseInt(value) : 0));
    }

    public static OnOffType getOnOff(@Nullable Boolean value) {
        return (value != null ? value ? OnOffType.ON : OnOffType.OFF : OnOffType.OFF);
    }

    public static OnOffType getOnOffType(@Nullable String value) {
        if (value == null) {
            return OnOffType.OFF;
        }
        String str = value.toLowerCase();
        return "on".equals(str) || "true".equals(str) || "1".equals(str) || "yes".equals(str) || "locked".equals(str)
                || "open".equals(str) || "enabled".equals(str) ? OnOffType.ON : OnOffType.OFF;
    }

    public static OnOffType getOnOff(int value) {
        return value == 0 ? OnOffType.OFF : OnOffType.ON;
    }

    public static OnOffType getOnOff(boolean value) {
        return value ? OnOffType.ON : OnOffType.OFF;
    }

    public static State toQuantityType(@Nullable Double value, int digits, Unit<?> unit) {
        if (value == null) {
            return UnDefType.NULL;
        }
        BigDecimal bd = new BigDecimal(value.doubleValue());
        if (digits >= 1) {
            bd = bd.setScale(digits, RoundingMode.HALF_EVEN);
        }
        return toQuantityType(bd, unit);
    }

    public static State toQuantityType(@Nullable Integer value, int digits, Unit<?> unit) {
        return toQuantityType(value.doubleValue(), digits, unit);
    }

    public static State toQuantityType(@Nullable Number value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    public static State toQuantityType(@Nullable PercentType value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(value.toBigDecimal(), unit);
    }

    public static long now() {
        return System.currentTimeMillis() / 1000L;
    }

    public static DateTimeType getTimestamp(ZoneId zoneId) {
        return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(now()), zoneId));
    }

    public static DateTimeType getTimestamp(ZoneId zoneId, long timestamp) {
        try {
            if (timestamp == 0) {
                return getTimestamp(zoneId);
            }
            ZonedDateTime zdt = LocalDateTime.now().atZone(zoneId);
            int delta = zdt.getOffset().getTotalSeconds();
            return new DateTimeType(ZonedDateTime.ofInstant(Instant.ofEpochSecond(timestamp - delta), zoneId));
        } catch (DateTimeException e) {
            // Unable to convert device's timezone, use system one
            return getTimestamp(zoneId);
        }
    }

    public static State getDateTime(String timestamp, ZoneId zoneId) {
        if (timestamp.length() < 2) {
            return UnDefType.UNDEF;
        }
        return new DateTimeType(ZonedDateTime.ofInstant(convertDateTime(timestamp).toInstant(), zoneId));
    }

    public static Date convertDateTime(String timestamp) {
        try {
            if (timestamp.length() > 13 && timestamp.charAt(2) == '-' && timestamp.charAt(5) == '-'
                    && timestamp.charAt(13) == ':') {
                // format: 07-30-2021 15:28:38, e.g. Ford
                return new SimpleDateFormat("MM-dd-uuuu HH:mm:ss").parse(timestamp);
            } else {
                DateTimeFormatter fmt = timestamp.contains("+") ? DateTimeFormatter.ISO_ZONED_DATE_TIME
                        : DateTimeFormatter.ISO_INSTANT;
                return Date.from(Instant.from(fmt.parse(timestamp)));
            }
        } catch (DateTimeException | ParseException e) {
            return new Date();
        }
    }

    public static String sha512(String pin, String challenge) throws ApiException {
        try {
            MessageDigest hash = MessageDigest.getInstance("SHA-512");
            byte[] pinBytes = DatatypeConverter.parseHexBinary(pin);
            byte[] challengeBytes = DatatypeConverter.parseHexBinary(challenge);
            ByteBuffer input = ByteBuffer.allocate(pinBytes.length + challengeBytes.length);
            input.put(pinBytes);
            input.put(challengeBytes);
            byte[] digest = hash.digest(input.array());
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; ++i) {
                sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new ApiException("sha512() failed", e);
        }
    }

    public static String mkChannelId(String group, String channel) {
        return group + "#" + channel;
    }

    public static <T> Class<T> wrap(Class<T> type) {
        return type;
    }

    /**
     * Generate a NONCE value
     *
     * @return new NONCE
     */
    public static String generateNonce() {
        String dateTimeString = Long.toString(new Date().getTime());
        byte[] nonceBytes = dateTimeString.getBytes();
        return Base64.getEncoder().encodeToString(nonceBytes);
    }

    /**
     * Encode fields for URL string
     *
     * @param s Field value
     * @return URL encoded value
     */
    public static String urlEncode(String s) {
        String url = UrlEncoded.encodeString(s, StandardCharsets.UTF_8); // returns forms data format
        url = url.replace("+", "%20").replace("%2D", "-").replace("%40", "@").replace("%5F", "_");
        return url;
    }

    public static boolean isNumeric(@Nullable String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
