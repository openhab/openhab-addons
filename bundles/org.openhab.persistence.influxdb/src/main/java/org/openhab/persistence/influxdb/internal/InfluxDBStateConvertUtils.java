/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.persistence.influxdb.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.TimeZone;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GroupItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conversion logic between openHAB {@link State} types and InfluxDB store types
 *
 * @author Joan Pujol Espinar - Initial contribution, based on previous work from Theo Weiss and Dominik Vorreiter
 */
@NonNullByDefault
public class InfluxDBStateConvertUtils {
    static final Number DIGITAL_VALUE_OFF = 0; // Visible for testing
    static final Number DIGITAL_VALUE_ON = 1; // Visible for testing
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBStateConvertUtils.class);

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
            value = state.toString();
        } else if (state instanceof DecimalType type) {
            value = type.toBigDecimal();
        } else if (state instanceof QuantityType<?> type) {
            value = type.toBigDecimal();
        } else if (state instanceof OnOffType) {
            value = state == OnOffType.ON ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof OpenClosedType) {
            value = state == OpenClosedType.OPEN ? DIGITAL_VALUE_ON : DIGITAL_VALUE_OFF;
        } else if (state instanceof DateTimeType type) {
            value = type.getZonedDateTime().toInstant().toEpochMilli();
        } else {
            value = state.toFullString();
        }
        return value;
    }

    /**
     * Converts a value to a {@link State} which is suitable for the given {@link Item}. This is
     * needed for querying an {@link InfluxDBHistoricItem}.
     *
     * @param value to be converted to a {@link State}
     * @param itemName name of the {@link Item} to get the {@link State} for
     * @return the state of the item represented by the itemName parameter, else the string value of
     *         the Object parameter
     */
    public static State objectToState(Object value, String itemName, ItemRegistry itemRegistry) {
        try {
            Item item = itemRegistry.getItem(itemName);
            return objectToState(value, item);
        } catch (ItemNotFoundException e) {
            LOGGER.info("Could not find item '{}' in registry", itemName);
        }

        return new StringType(String.valueOf(value));
    }

    public static State objectToState(@Nullable Object value, Item itemToSetState) {
        String valueStr = String.valueOf(value);

        @Nullable
        Item item = itemToSetState;
        if (item instanceof GroupItem groupItem) {
            item = groupItem.getBaseItem();
        }
        if (item instanceof ColorItem) {
            return new HSBType(valueStr);
        } else if (item instanceof LocationItem) {
            return new PointType(valueStr);
        } else if (item instanceof NumberItem numberItem) {
            Unit<?> unit = numberItem.getUnit();
            if (unit == null) {
                return new DecimalType(valueStr);
            } else {
                return new QuantityType<>(new BigDecimal(valueStr), unit);
            }
        } else if (item instanceof DimmerItem) {
            return new PercentType(valueStr);
        } else if (item instanceof SwitchItem) {
            return OnOffType.from(toBoolean(valueStr));
        } else if (item instanceof ContactItem) {
            return toBoolean(valueStr) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        } else if (item instanceof RollershutterItem) {
            return new PercentType(valueStr);
        } else if (item instanceof DateTimeItem) {
            Instant i = Instant.ofEpochMilli(new BigDecimal(valueStr).longValue());
            ZonedDateTime z = ZonedDateTime.ofInstant(i, TimeZone.getDefault().toZoneId());
            return new DateTimeType(z);
        } else if (item instanceof PlayerItem) {
            try {
                return PlayPauseType.valueOf(valueStr);
            } catch (IllegalArgumentException ignored) {
            }
            try {
                return RewindFastforwardType.valueOf(valueStr);
            } catch (IllegalArgumentException ignored) {
            }
        } else if (item instanceof ImageItem) {
            return RawType.valueOf(valueStr);
        } else {
            return new StringType(valueStr);
        }
        return UnDefType.UNDEF;
    }

    private static boolean toBoolean(@Nullable Object object) {
        if (object instanceof Boolean boolean1) {
            return boolean1;
        } else if (object != null) {
            if ("1".equals(object) || "1.0".equals(object)) {
                return true;
            } else {
                return Boolean.parseBoolean(String.valueOf(object));
            }
        } else {
            return false;
        }
    }
}
