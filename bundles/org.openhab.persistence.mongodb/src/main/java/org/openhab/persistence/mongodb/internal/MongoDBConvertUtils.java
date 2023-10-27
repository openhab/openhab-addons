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

package org.openhab.persistence.mongodb.internal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

import javax.measure.Unit;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Conversion logic between openHAB {@link State} types and MongoDB store types
 *
 * @author Konrad Zawadka - Initial contribution, based on previous work from Joan Pujol Espinar, Theo Weiss and Dominik
 *         Vorreiter
 */
@NonNullByDefault
public class MongoDBConvertUtils {
    @NonNullByDefault
    static final Number DIGITAL_VALUE_OFF = 0; // Visible for testing
    @NonNullByDefault
    static final Number DIGITAL_VALUE_ON = 1; // Visible for testing
    @NonNullByDefault
    static final String FIELD_ID = "_id";
    @NonNullByDefault
    static final String FIELD_ITEM = "item";
    @NonNullByDefault
    static final String FIELD_REALNAME = "realName";
    @NonNullByDefault
    static final String FIELD_TIMESTAMP = "timestamp";
    @NonNullByDefault
    static final String FIELD_VALUE = "value";

    protected static Object stateToObject(State state) {
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

    protected static DBObject itemToDBObject(Item item, String alias) {
        String name = (alias != null) ? alias : item.getName();
        Object value = MongoDBConvertUtils.stateToObject(item.getState());

        DBObject obj = new BasicDBObject();
        obj.put(FIELD_ID, new ObjectId());
        obj.put(FIELD_ITEM, name);
        obj.put(FIELD_REALNAME, item.getName());
        obj.put(FIELD_TIMESTAMP, new Date());
        obj.put(FIELD_VALUE, value);
        return obj;
    }

    protected static State objectToState(BasicDBObject obj, Item itemToSetState) {
        final State state;
        if (itemToSetState instanceof NumberItem numberItem) {
            double value = obj.getDouble(FIELD_VALUE);
            Unit<?> unit = numberItem.getUnit();
            if (unit == null) {
                return new DecimalType(value);
            } else {
                return new QuantityType<>(value, unit);
            }
        } else if (itemToSetState instanceof DimmerItem) {
            return new PercentType(obj.getInt(FIELD_VALUE));
        } else if (itemToSetState instanceof SwitchItem) {
            return toBoolean(obj.getString(FIELD_VALUE)) ? OnOffType.ON : OnOffType.OFF;
        } else if (itemToSetState instanceof ContactItem) {
            return toBoolean(obj.getString(FIELD_VALUE)) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
        } else if (itemToSetState instanceof RollershutterItem) {
            state = new PercentType(obj.getInt(FIELD_VALUE));
        } else if (itemToSetState instanceof DateTimeItem) {
            state = getDateTime(obj);
        } else if (itemToSetState instanceof ImageItem) {
            state = RawType.valueOf(obj.getString(FIELD_VALUE));
        } else {
            state = new StringType(obj.getString(FIELD_VALUE));
        }
        return state;
    }

    private static State getDateTime(BasicDBObject obj) {
        String valueStr = obj.getString(FIELD_VALUE);
        if (StringUtils.isNumeric(valueStr)) {
            Instant i = Instant.ofEpochMilli(new BigDecimal(valueStr).longValue());
            ZonedDateTime z = ZonedDateTime.ofInstant(i, TimeZone.getDefault().toZoneId());
            return new DateTimeType(z);
        } else {
            return new DateTimeType(
                    ZonedDateTime.ofInstant(obj.getDate(FIELD_VALUE).toInstant(), ZoneId.systemDefault()));
        }
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
