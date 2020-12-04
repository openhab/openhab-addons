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
package org.openhab.persistence.dynamodb.internal;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.items.Item;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.LocationItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.PlayerItem;
import org.openhab.core.library.items.RollershutterItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

/**
 * Base class for all DynamoDBItem. Represents openHAB Item serialized in a suitable format for the database
 *
 * @param <T> Type of the state as accepted by the AWS SDK.
 *
 * @author Sami Salonen - Initial contribution
 */
public abstract class AbstractDynamoDBItem<T> implements DynamoDBItem<T> {

    private static final ZoneId UTC = ZoneId.of("UTC");
    public static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(UTC);

    private static final String UNDEFINED_PLACEHOLDER = "<org.openhab.core.types.UnDefType.UNDEF>";

    private static final Map<Class<? extends Item>, Class<? extends DynamoDBItem<?>>> ITEM_CLASS_MAP = new HashMap<>();

    static {
        ITEM_CLASS_MAP.put(CallItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP.put(ContactItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP.put(DateTimeItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP.put(LocationItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP.put(NumberItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP.put(RollershutterItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP.put(StringItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP.put(SwitchItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP.put(DimmerItem.class, DynamoDBBigDecimalItem.class); // inherited from SwitchItem (!)
        ITEM_CLASS_MAP.put(ColorItem.class, DynamoDBStringItem.class); // inherited from DimmerItem
        ITEM_CLASS_MAP.put(PlayerItem.class, DynamoDBStringItem.class);
    }

    public static final Class<DynamoDBItem<?>> getDynamoItemClass(Class<? extends Item> itemClass)
            throws NullPointerException {
        @SuppressWarnings("unchecked")
        Class<DynamoDBItem<?>> dtoclass = (Class<DynamoDBItem<?>>) ITEM_CLASS_MAP.get(itemClass);
        if (dtoclass == null) {
            throw new IllegalArgumentException(String.format("Unknown item class %s", itemClass));
        }
        return dtoclass;
    }

    /**
     * Custom converter for serialization/deserialization of ZonedDateTime.
     *
     * Serialization: ZonedDateTime is first converted to UTC and then stored with format yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     * This allows easy sorting of values since all timestamps are in UTC and string ordering can be used.
     *
     * @author Sami Salonen - Initial contribution
     *
     */
    public static final class ZonedDateTimeConverter implements DynamoDBTypeConverter<String, ZonedDateTime> {

        @Override
        public String convert(ZonedDateTime time) {
            return DATEFORMATTER.format(time.withZoneSameInstant(UTC));
        }

        @Override
        public ZonedDateTime unconvert(String serialized) {
            return ZonedDateTime.parse(serialized, DATEFORMATTER);
        }
    }

    private static final ZonedDateTimeConverter zonedDateTimeConverter = new ZonedDateTimeConverter();
    private final Logger logger = LoggerFactory.getLogger(AbstractDynamoDBItem.class);

    protected String name;
    protected T state;
    protected ZonedDateTime time;

    public AbstractDynamoDBItem(String name, T state, ZonedDateTime time) {
        this.name = name;
        this.state = state;
        this.time = time;
    }

    public static DynamoDBItem<?> fromState(String name, State state, ZonedDateTime time) {
        if (state instanceof DecimalType && !(state instanceof HSBType)) {
            // also covers PercentType which is inherited from DecimalType
            return new DynamoDBBigDecimalItem(name, ((DecimalType) state).toBigDecimal(), time);
        } else if (state instanceof OnOffType) {
            return new DynamoDBBigDecimalItem(name,
                    ((OnOffType) state) == OnOffType.ON ? BigDecimal.ONE : BigDecimal.ZERO, time);
        } else if (state instanceof OpenClosedType) {
            return new DynamoDBBigDecimalItem(name,
                    ((OpenClosedType) state) == OpenClosedType.OPEN ? BigDecimal.ONE : BigDecimal.ZERO, time);
        } else if (state instanceof UpDownType) {
            return new DynamoDBBigDecimalItem(name,
                    ((UpDownType) state) == UpDownType.UP ? BigDecimal.ONE : BigDecimal.ZERO, time);
        } else if (state instanceof DateTimeType) {
            return new DynamoDBStringItem(name,
                    zonedDateTimeConverter.convert(((DateTimeType) state).getZonedDateTime()), time);
        } else if (state instanceof UnDefType) {
            return new DynamoDBStringItem(name, UNDEFINED_PLACEHOLDER, time);
        } else if (state instanceof StringListType) {
            return new DynamoDBStringItem(name, state.toFullString(), time);
        } else {
            // HSBType, PointType, PlayPauseType and StringType
            return new DynamoDBStringItem(name, state.toFullString(), time);
        }
    }

    @Override
    public HistoricItem asHistoricItem(final Item item) {
        final State[] state = new State[1];
        accept(new DynamoDBItemVisitor() {

            @Override
            public void visit(DynamoDBStringItem dynamoStringItem) {
                if (item instanceof ColorItem) {
                    state[0] = new HSBType(dynamoStringItem.getState());
                } else if (item instanceof LocationItem) {
                    state[0] = new PointType(dynamoStringItem.getState());
                } else if (item instanceof PlayerItem) {
                    String value = dynamoStringItem.getState();
                    try {
                        state[0] = PlayPauseType.valueOf(value);
                    } catch (IllegalArgumentException e) {
                        state[0] = RewindFastforwardType.valueOf(value);
                    }
                } else if (item instanceof DateTimeItem) {
                    try {
                        // Parse ZoneDateTime from string. DATEFORMATTER assumes UTC in case it is not clear
                        // from the string (should be).
                        // We convert to default/local timezone for user convenience (e.g. display)
                        state[0] = new DateTimeType(zonedDateTimeConverter.unconvert(dynamoStringItem.getState())
                                .withZoneSameInstant(ZoneId.systemDefault()));
                    } catch (DateTimeParseException e) {
                        logger.warn("Failed to parse {} as date. Outputting UNDEF instead",
                                dynamoStringItem.getState());
                        state[0] = UnDefType.UNDEF;
                    }
                } else if (dynamoStringItem.getState().equals(UNDEFINED_PLACEHOLDER)) {
                    state[0] = UnDefType.UNDEF;
                } else if (item instanceof CallItem) {
                    String parts = dynamoStringItem.getState();
                    String[] strings = parts.split(",");
                    String orig = strings[0];
                    String dest = strings[1];
                    state[0] = new StringListType(orig, dest);
                } else {
                    state[0] = new StringType(dynamoStringItem.getState());
                }
            }

            @Override
            public void visit(DynamoDBBigDecimalItem dynamoBigDecimalItem) {
                if (item instanceof NumberItem) {
                    state[0] = new DecimalType(dynamoBigDecimalItem.getState());
                } else if (item instanceof DimmerItem) {
                    state[0] = new PercentType(dynamoBigDecimalItem.getState());
                } else if (item instanceof SwitchItem) {
                    state[0] = dynamoBigDecimalItem.getState().compareTo(BigDecimal.ONE) == 0 ? OnOffType.ON
                            : OnOffType.OFF;
                } else if (item instanceof ContactItem) {
                    state[0] = dynamoBigDecimalItem.getState().compareTo(BigDecimal.ONE) == 0 ? OpenClosedType.OPEN
                            : OpenClosedType.CLOSED;
                } else if (item instanceof RollershutterItem) {
                    state[0] = new PercentType(dynamoBigDecimalItem.getState());
                } else {
                    logger.warn("Not sure how to convert big decimal item {} to type {}. Using StringType as fallback",
                            dynamoBigDecimalItem.getName(), item.getClass());
                    state[0] = new StringType(dynamoBigDecimalItem.getState().toString());
                }
            }
        });
        return new DynamoDBHistoricItem(getName(), state[0], getTime());
    }

    /**
     * We define all getter and setters in the child class implement those. Having the getter
     * and setter implementations here in the parent class does not work with introspection done by AWS SDK (1.11.56).
     */

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.persistence.dynamodb.internal.DynamoItem#accept(org.openhab.persistence.dynamodb.internal.
     * DynamoItemVisitor)
     */
    @Override
    public abstract void accept(DynamoDBItemVisitor visitor);

    @Override
    public String toString() {
        return DATEFORMATTER.format(time) + ": " + name + " -> " + state.toString();
    }
}
