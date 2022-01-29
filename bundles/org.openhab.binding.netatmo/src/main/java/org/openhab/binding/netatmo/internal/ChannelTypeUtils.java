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
package org.openhab.binding.netatmo.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * This class holds various channel values conversion methods
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Rob Nielsen - Added day, week, and month measurements to the weather station and modules
 *
 */
@NonNullByDefault
public class ChannelTypeUtils {

    public static State toStringType(@Nullable String value) {
        return (value == null) ? UnDefType.NULL : new StringType(value);
    }

    public static ZonedDateTime toZonedDateTime(Integer netatmoTS, ZoneId zoneId) {
        Instant i = Instant.ofEpochSecond(netatmoTS);
        return ZonedDateTime.ofInstant(i, zoneId);
    }

    public static State toDateTimeType(@Nullable Float netatmoTS, ZoneId zoneId) {
        return netatmoTS == null ? UnDefType.NULL : toDateTimeType(toZonedDateTime(netatmoTS.intValue(), zoneId));
    }

    public static State toDateTimeType(@Nullable Integer netatmoTS, ZoneId zoneId) {
        return netatmoTS == null ? UnDefType.NULL : toDateTimeType(toZonedDateTime(netatmoTS, zoneId));
    }

    public static State toDateTimeType(@Nullable ZonedDateTime zonedDateTime) {
        return (zonedDateTime == null) ? UnDefType.NULL : new DateTimeType(zonedDateTime);
    }

    public static State toDecimalType(@Nullable Float value) {
        return (value == null) ? UnDefType.NULL : toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(@Nullable Integer value) {
        return (value == null) ? UnDefType.NULL : toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(@Nullable Double value) {
        return (value == null) ? UnDefType.NULL : toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(float value) {
        return toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(double value) {
        return toDecimalType(new BigDecimal(value));
    }

    public static State toDecimalType(@Nullable BigDecimal decimal) {
        return decimal == null ? UnDefType.NULL : new DecimalType(decimal.setScale(2, RoundingMode.HALF_UP));
    }

    public static State toDecimalType(@Nullable String textualDecimal) {
        return textualDecimal == null ? UnDefType.NULL : new DecimalType(textualDecimal);
    }

    public static State toOnOffType(@Nullable String yesno) {
        return "on".equalsIgnoreCase(yesno) ? OnOffType.ON : OnOffType.OFF;
    }

    public static State toOnOffType(@Nullable Integer value) {
        return value != null ? (value == 1 ? OnOffType.ON : OnOffType.OFF) : UnDefType.UNDEF;
    }

    public static State toOnOffType(@Nullable Boolean value) {
        return value != null ? (value ? OnOffType.ON : OnOffType.OFF) : UnDefType.UNDEF;
    }

    public static State toQuantityType(@Nullable Float value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(@Nullable Integer value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(@Nullable Double value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(float value, Unit<?> unit) {
        return toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(int value, Unit<?> unit) {
        return toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(double value, Unit<?> unit) {
        return toQuantityType(new BigDecimal(value), unit);
    }

    public static State toQuantityType(@Nullable BigDecimal value, Unit<?> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    public static State toRawType(String pictureUrl) {
        RawType picture = HttpUtil.downloadImage(pictureUrl);
        return picture == null ? UnDefType.UNDEF : picture;
    }
}
