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
package org.openhab.persistence.influxdb2.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.*;
import org.openhab.core.library.types.*;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conversion logic between openHAB {@link State} types and InfluxDB store types
 *
 * @author Joan Pujol Espinar - Initial contribution, based on previous work from Theo Weiss and Dominik Vorreiter
 */
@NonNullByDefault
public class InfluxDBStateConvertUtils {
    static final String DIGITAL_VALUE_OFF = "false"; // Visible for testing
    static final String DIGITAL_VALUE_ON = "true"; // Visible for testing
    private static Logger logger = LoggerFactory.getLogger(InfluxDBStateConvertUtils.class);

    /**
     * Converts {@link State} to a String suitable for influxdb queries.
     *
     * @param state to be converted
     * @return {@link String} equivalent of the {@link State}
     */
    public static String stateToString(State state) {
        String value;
        if (state instanceof DecimalType) {
            value = ((DecimalType) state).toBigDecimal().toString();
        } else if (state instanceof QuantityType<?>) {
            value = ((QuantityType<?>) state).toBigDecimal().toString();
        } else if (state instanceof PointType) {
            value = point2String((PointType) state);
        } else if (state instanceof OnOffType) {
            value = state == OnOffType.ON ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof OpenClosedType) {
            value = state == OpenClosedType.OPEN ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof DateTimeType) {
            value = String.valueOf(((DateTimeType) state).getZonedDateTime().toInstant().toEpochMilli());
        } else {
            value = state.toString();
        }
        return value;
    }

    /**
     * Converts {@link State} to objects fitting into influxdb values.
     *
     * @param state to be converted
     * @return integer or double value for DecimalType, 0 or 1 for OnOffType and OpenClosedType,
     *         integer for DateTimeType, String for all others
     */
    public static Object stateToObject(State state) {
        Object value;
        if (state instanceof HSBType) {
            value = state.toString();
        } else if (state instanceof PointType) {
            value = point2String((PointType) state);
        } else if (state instanceof DecimalType) {
            value = convertBigDecimalToNum(((DecimalType) state).toBigDecimal());
        } else if (state instanceof QuantityType<?>) {
            value = convertBigDecimalToNum(((QuantityType<?>) state).toBigDecimal());
        } else if (state instanceof OnOffType) {
            value = state == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE;
        } else if (state instanceof OpenClosedType) {
            value = state == OpenClosedType.OPEN ? Boolean.TRUE : Boolean.FALSE;
        } else if (state instanceof DateTimeType) {
            value = ((DateTimeType) state).getZonedDateTime().toInstant().toEpochMilli();
        } else {
            value = state.toString();
        }
        return value;
    }

    /**
     * Converts a value to a {@link State} which is suitable for the given {@link Item}. This is
     * needed for querying a {@link InfluxDBHistoricItem}.
     *
     * @param value    to be converted to a {@link State}
     * @param itemName name of the {@link Item} to get the {@link State} for
     * @return the state of the item represented by the itemName parameter, else the string value of
     *         the Object parameter
     */
    public static State objectToState(Object value, String itemName, @Nullable ItemRegistry itemRegistry) {
        State state = null;
        if (itemRegistry != null) {
            try {
                Item item = itemRegistry.getItem(itemName);
                state = objectToState(value, item);
            } catch (ItemNotFoundException e) {
                logger.info("Could not find item '{}' in registry", itemName);
            }
        }

        if (state == null)
            state = new StringType(String.valueOf(value));

        return state;
    }

    public static State objectToState(Object value, Item itemToSetState) {
        String valueStr = String.valueOf(value);

        Item item = itemToSetState;
        if (item instanceof GroupItem) {
            item = ((GroupItem) item).getBaseItem();
        }
        if (item instanceof ColorItem) {
            return new HSBType(valueStr);
        } else if (item instanceof LocationItem) {
            return new PointType(valueStr);
        } else if (item instanceof NumberItem) {
            return new DecimalType(valueStr);
        } else if (item instanceof DimmerItem) {
            return new PercentType(valueStr);
        } else if (item instanceof SwitchItem) {
            return toBoolean(valueStr) ? OnOffType.ON : OnOffType.OFF;
        } else if (item instanceof ContactItem) {
            return toBoolean(valueStr) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        } else if (item instanceof RollershutterItem) {
            return new PercentType(valueStr);
        } else if (item instanceof DateTimeItem) {
            Instant i = Instant.ofEpochMilli(new BigDecimal(valueStr).longValue());
            ZonedDateTime z = ZonedDateTime.ofInstant(i, TimeZone.getDefault().toZoneId());
            return new DateTimeType(z);
        } else {
            return new StringType(valueStr);
        }
    }

    private static boolean toBoolean(@Nullable Object object) {
        if (object instanceof Boolean)
            return (Boolean) object;
        else if (object != null) {
            if ("1".equals(object))
                return true;
            else
                return BooleanUtils.toBoolean(String.valueOf(object));
        } else {
            return false;
        }
    }

    private static String point2String(PointType point) {
        StringBuffer buf = new StringBuffer();
        buf.append(point.getLatitude().toString());
        buf.append(",");
        buf.append(point.getLongitude().toString());
        if (!point.getAltitude().equals(DecimalType.ZERO)) {
            buf.append(",");
            buf.append(point.getAltitude().toString());
        }
        return buf.toString(); // latitude, longitude, altitude
    }

    /**
     * This method returns an integer if possible if not a double is returned. This is an optimization
     * for influxdb because integers have less overhead.
     *
     * @param value the BigDecimal to be converted
     * @return A double if possible else a double is returned.
     */
    private static Object convertBigDecimalToNum(BigDecimal value) {
        Object convertedValue;
        if (value.scale() == 0) {
            convertedValue = value.toBigInteger();
        } else {
            convertedValue = value.doubleValue();
        }
        return convertedValue;
    }
}
