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
package org.openhab.binding.dbquery.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dbquery.internal.domain.DBQueryJSONEncoder;
import org.openhab.binding.dbquery.internal.domain.QueryResult;
import org.openhab.binding.dbquery.internal.error.UnnexpectedCondition;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage conversion from a value to needed State target type
 *
 * @author Joan Pujol - Initial contribution
 */
@NonNullByDefault
public class Value2StateConverter {
    private final Logger logger = LoggerFactory.getLogger(Value2StateConverter.class);
    private final DBQueryJSONEncoder jsonEncoder = new DBQueryJSONEncoder();

    public State convertValue(@Nullable Object value, Class<? extends State> targetType) {
        if (value == null) {
            return UnDefType.NULL;
        } else {
            if (targetType == StringType.class) {
                return convert2String(value);
            } else if (targetType == DecimalType.class) {
                return convert2Decimal(value);
            } else if (targetType == DateTimeType.class) {
                return convert2DateTime(value);
            } else if (targetType == OnOffType.class) {
                @Nullable
                Boolean bool = convert2Boolean(value);
                return bool != null ? OnOffType.from(bool) : UnDefType.NULL;
            } else if (targetType == OpenClosedType.class) {
                @Nullable
                Boolean bool = convert2Boolean(value);
                if (bool != null) {
                    return bool ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                } else {
                    return UnDefType.NULL;
                }
            } else {
                throw new UnnexpectedCondition("Not expected targetType " + targetType);
            }
        }
    }

    private State convert2DateTime(Object value) {
        if (value instanceof Instant) {
            return new DateTimeType(ZonedDateTime.ofInstant((Instant) value, ZoneId.systemDefault()));
        } else if (value instanceof Date) {
            return new DateTimeType(ZonedDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault()));
        } else if (value instanceof String) {
            return new DateTimeType((String) value);
        } else {
            logger.warn("Can't convert {} to DateTimeType", value);
            return UnDefType.NULL;
        }
    }

    private State convert2Decimal(Object value) {
        if (value instanceof Integer) {
            return new DecimalType((Integer) value);
        } else if (value instanceof Long) {
            return new DecimalType((Long) value);
        } else if (value instanceof Float) {
            return new DecimalType((Float) value);
        } else if (value instanceof Double) {
            return new DecimalType((Double) value);
        } else if (value instanceof BigDecimal) {
            return new DecimalType((BigDecimal) value);
        } else if (value instanceof BigInteger) {
            return new DecimalType(new BigDecimal((BigInteger) value));
        } else if (value instanceof Number) {
            return new DecimalType(((Number) value).longValue());
        } else if (value instanceof String) {
            return DecimalType.valueOf((String) value);
        } else if (value instanceof Duration) {
            return new DecimalType(((Duration) value).toMillis());
        } else {
            logger.warn("Can't convert {} to DecimalType", value);
            return UnDefType.NULL;
        }
    }

    private State convert2String(Object value) {
        if (value instanceof String) {
            return new StringType((String) value);
        } else if (value instanceof byte[]) {
            return new StringType(Base64.getEncoder().encodeToString((byte[]) value));
        } else if (value instanceof QueryResult) {
            return new StringType(jsonEncoder.encode((QueryResult) value));
        } else {
            return new StringType(String.valueOf(value));
        }
    }

    private @Nullable Boolean convert2Boolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof Number) {
            return ((Number) value).doubleValue() != 0d;
        } else if (value instanceof String) {
            var svalue = (String) value;
            return Boolean.parseBoolean(svalue) || (svalue.equalsIgnoreCase("on")) || svalue.equals("1");
        } else {
            logger.warn("Can't convert {} to OnOffType or OpenClosedType", value);
            return null;
        }
    }
}
