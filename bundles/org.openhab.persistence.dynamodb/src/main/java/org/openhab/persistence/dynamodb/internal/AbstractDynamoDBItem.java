/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.CallItem;
import org.openhab.core.library.items.ColorItem;
import org.openhab.core.library.items.ContactItem;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.ImageItem;
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
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.HistoricItem;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema.Builder;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Base class for all DynamoDBItem. Represents openHAB Item serialized in a suitable format for the database
 *
 * @param <T> Type of the state as accepted by the AWS SDK.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractDynamoDBItem<T> implements DynamoDBItem<T> {

    private static final BigDecimal REWIND_BIGDECIMAL = new BigDecimal("-1");
    private static final BigDecimal PAUSE_BIGDECIMAL = new BigDecimal("0");
    private static final BigDecimal PLAY_BIGDECIMAL = new BigDecimal("1");
    private static final BigDecimal FAST_FORWARD_BIGDECIMAL = new BigDecimal("2");

    private static final ZoneId UTC = ZoneId.of("UTC");
    public static final ZonedDateTimeStringConverter ZONED_DATE_TIME_CONVERTER_STRING = new ZonedDateTimeStringConverter();
    public static final ZonedDateTimeMilliEpochConverter ZONED_DATE_TIME_CONVERTER_MILLIEPOCH = new ZonedDateTimeMilliEpochConverter();
    public static final DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(UTC);
    protected static final Class<@Nullable Long> NULLABLE_LONG = (Class<@Nullable Long>) Long.class;

    public static AttributeConverter<ZonedDateTime> getTimestampConverter(boolean legacy) {
        return legacy ? ZONED_DATE_TIME_CONVERTER_STRING : ZONED_DATE_TIME_CONVERTER_MILLIEPOCH;
    }

    protected static <C extends AbstractDynamoDBItem<?>> Builder<C> getBaseSchemaBuilder(Class<C> clz, boolean legacy) {
        return TableSchema.builder(clz).addAttribute(String.class,
                a -> a.name(legacy ? DynamoDBItem.ATTRIBUTE_NAME_ITEMNAME_LEGACY : DynamoDBItem.ATTRIBUTE_NAME_ITEMNAME)
                        .getter(AbstractDynamoDBItem::getName).setter(AbstractDynamoDBItem::setName)
                        .tags(StaticAttributeTags.primaryPartitionKey()))
                .addAttribute(ZonedDateTime.class, a -> a
                        .name(legacy ? DynamoDBItem.ATTRIBUTE_NAME_TIMEUTC_LEGACY : DynamoDBItem.ATTRIBUTE_NAME_TIMEUTC)
                        .getter(AbstractDynamoDBItem::getTime).setter(AbstractDynamoDBItem::setTime)
                        .tags(StaticAttributeTags.primarySortKey()).attributeConverter(getTimestampConverter(legacy)));
    }

    private static final Map<Class<? extends Item>, Class<? extends DynamoDBItem<?>>> ITEM_CLASS_MAP_LEGACY = new HashMap<>();

    static {
        ITEM_CLASS_MAP_LEGACY.put(CallItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_LEGACY.put(ContactItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_LEGACY.put(DateTimeItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_LEGACY.put(LocationItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_LEGACY.put(NumberItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_LEGACY.put(RollershutterItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_LEGACY.put(StringItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_LEGACY.put(SwitchItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_LEGACY.put(DimmerItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_LEGACY.put(ColorItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_LEGACY.put(PlayerItem.class, DynamoDBStringItem.class);
    }

    private static final Map<Class<? extends Item>, Class<? extends DynamoDBItem<?>>> ITEM_CLASS_MAP_NEW = new HashMap<>();

    static {
        ITEM_CLASS_MAP_NEW.put(CallItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_NEW.put(ContactItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_NEW.put(DateTimeItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_NEW.put(LocationItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_NEW.put(NumberItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_NEW.put(RollershutterItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_NEW.put(StringItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_NEW.put(SwitchItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_NEW.put(DimmerItem.class, DynamoDBBigDecimalItem.class);
        ITEM_CLASS_MAP_NEW.put(ColorItem.class, DynamoDBStringItem.class);
        ITEM_CLASS_MAP_NEW.put(PlayerItem.class, DynamoDBBigDecimalItem.class); // Different from LEGACY
    }

    public static Class<? extends DynamoDBItem<?>> getDynamoItemClass(Class<? extends Item> itemClass, boolean legacy)
            throws NullPointerException {
        Class<? extends DynamoDBItem<?>> dtoclass = (legacy ? ITEM_CLASS_MAP_LEGACY : ITEM_CLASS_MAP_NEW)
                .get(itemClass);
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
    public static final class ZonedDateTimeStringConverter implements AttributeConverter<ZonedDateTime> {

        @Override
        public AttributeValue transformFrom(ZonedDateTime time) {
            return AttributeValue.builder().s(toString(time)).build();
        }

        @Override
        public ZonedDateTime transformTo(@NonNullByDefault({}) AttributeValue serialized) {
            return transformTo(serialized.s());
        }

        @Override
        public EnhancedType<ZonedDateTime> type() {
            return EnhancedType.of(ZonedDateTime.class);
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.S;
        }

        public String toString(ZonedDateTime time) {
            return DATEFORMATTER.format(time.withZoneSameInstant(UTC));
        }

        public ZonedDateTime transformTo(String serialized) {
            return ZonedDateTime.parse(serialized, DATEFORMATTER);
        }
    }

    /**
     * Custom converter for serialization/deserialization of ZonedDateTime.
     *
     * Serialization: ZonedDateTime is first converted to UTC and then stored as milliepochs
     *
     * @author Sami Salonen - Initial contribution
     *
     */
    public static final class ZonedDateTimeMilliEpochConverter implements AttributeConverter<ZonedDateTime> {

        @Override
        public AttributeValue transformFrom(ZonedDateTime time) {
            return AttributeValue.builder().n(toEpochMilliString(time)).build();
        }

        @Override
        public ZonedDateTime transformTo(@NonNullByDefault({}) AttributeValue serialized) {
            return transformTo(serialized.n());
        }

        @Override
        public EnhancedType<ZonedDateTime> type() {
            return EnhancedType.of(ZonedDateTime.class);
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.N;
        }

        public static String toEpochMilliString(ZonedDateTime time) {
            return String.valueOf(time.toInstant().toEpochMilli());
        }

        public static BigDecimal toBigDecimal(ZonedDateTime time) {
            return new BigDecimal(toEpochMilliString(time));
        }

        public ZonedDateTime transformTo(String serialized) {
            return transformTo(Long.valueOf(serialized));
        }

        public ZonedDateTime transformTo(Long epochMillis) {
            return Instant.ofEpochMilli(epochMillis).atZone(UTC);
        }
    }

    private final Logger logger = LoggerFactory.getLogger(AbstractDynamoDBItem.class);

    protected String name;
    protected @Nullable T state;
    protected ZonedDateTime time;
    private @Nullable Integer expireDays;
    private @Nullable Long expiry;

    public AbstractDynamoDBItem(String name, @Nullable T state, ZonedDateTime time, @Nullable Integer expireDays) {
        this.name = name;
        this.state = state;
        this.time = time;
        if (expireDays != null && expireDays <= 0) {
            throw new IllegalArgumentException();
        }
        this.expireDays = expireDays;
        this.expiry = expireDays == null ? null : time.toInstant().plus(Duration.ofDays(expireDays)).getEpochSecond();
    }

    /**
     * Convert given state to target state.
     *
     * If conversion fails, IllegalStateException is raised.
     * Use this method you do not expect conversion to fail.
     *
     * @param <T> state type to convert to
     * @param state state to convert
     * @param clz class of the resulting state
     * @return state as type T
     * @throws IllegalStateException on failing conversion
     */
    private static <T extends State> T convert(State state, Class<T> clz) {
        @Nullable
        T converted = state.as(clz);
        if (converted == null) {
            throw new IllegalStateException(String.format("Could not convert %s '%s' into %s",
                    state.getClass().getSimpleName(), state, clz.getClass().getSimpleName()));
        }
        return converted;
    }

    public static DynamoDBItem<?> fromStateLegacy(Item item, ZonedDateTime time) {
        String name = item.getName();
        State state = item.getState();
        if (item instanceof PlayerItem) {
            return new DynamoDBStringItem(name, state.toFullString(), time, null);
        } else {
            // Apart from PlayerItem, the values are serialized to dynamodb number/strings in the same way in legacy
            // delegate to fromStateNew
            return fromStateNew(item, time, null);
        }
    }

    public static DynamoDBItem<?> fromStateNew(Item item, ZonedDateTime time, @Nullable Integer expireDays) {
        String name = item.getName();
        State state = item.getState();
        if (item instanceof CallItem) {
            return new DynamoDBStringItem(name, convert(state, StringListType.class).toFullString(), time, expireDays);
        } else if (item instanceof ContactItem) {
            return new DynamoDBBigDecimalItem(name, convert(state, DecimalType.class).toBigDecimal(), time, expireDays);
        } else if (item instanceof DateTimeItem) {
            return new DynamoDBStringItem(name, ZONED_DATE_TIME_CONVERTER_STRING
                    .toString(((DateTimeType) state).getZonedDateTime(ZoneId.systemDefault())), time, expireDays);
        } else if (item instanceof ImageItem) {
            throw new IllegalArgumentException("Unsupported item " + item.getClass().getSimpleName());
        } else if (item instanceof LocationItem) {
            return new DynamoDBStringItem(name, state.toFullString(), time, expireDays);
        } else if (item instanceof NumberItem) {
            return new DynamoDBBigDecimalItem(name, convert(state, DecimalType.class).toBigDecimal(), time, expireDays);
        } else if (item instanceof PlayerItem) {
            if (state instanceof PlayPauseType pauseType) {
                switch (pauseType) {
                    case PLAY:
                        return new DynamoDBBigDecimalItem(name, PLAY_BIGDECIMAL, time, expireDays);
                    case PAUSE:
                        return new DynamoDBBigDecimalItem(name, PAUSE_BIGDECIMAL, time, expireDays);
                    default:
                        throw new IllegalArgumentException("Unexpected enum with PlayPauseType: " + state.toString());
                }
            } else if (state instanceof RewindFastforwardType rewindType) {
                switch (rewindType) {
                    case FASTFORWARD:
                        return new DynamoDBBigDecimalItem(name, FAST_FORWARD_BIGDECIMAL, time, expireDays);
                    case REWIND:
                        return new DynamoDBBigDecimalItem(name, REWIND_BIGDECIMAL, time, expireDays);
                    default:
                        throw new IllegalArgumentException(
                                "Unexpected enum with RewindFastforwardType: " + state.toString());
                }
            } else {
                throw new IllegalStateException(
                        String.format("Unexpected state type %s with PlayerItem", state.getClass().getSimpleName()));
            }
        } else if (item instanceof RollershutterItem) {
            // Normalize UP/DOWN to %
            return new DynamoDBBigDecimalItem(name, convert(state, PercentType.class).toBigDecimal(), time, expireDays);
        } else if (item instanceof StringItem) {
            if (state instanceof StringType stringType) {
                return new DynamoDBStringItem(name, stringType.toString(), time, expireDays);
            } else if (state instanceof DateTimeType dateType) {
                return new DynamoDBStringItem(name,
                        ZONED_DATE_TIME_CONVERTER_STRING.toString(dateType.getZonedDateTime(ZoneId.systemDefault())),
                        time, expireDays);
            } else {
                throw new IllegalStateException(
                        String.format("Unexpected state type %s with StringItem", state.getClass().getSimpleName()));
            }
        } else if (item instanceof ColorItem) { // Note: needs to be before parent class DimmerItem
            return new DynamoDBStringItem(name, convert(state, HSBType.class).toFullString(), time, expireDays);
        } else if (item instanceof DimmerItem) {// Note: needs to be before parent class SwitchItem
            // Normalize ON/OFF to %
            return new DynamoDBBigDecimalItem(name, convert(state, PercentType.class).toBigDecimal(), time, expireDays);
        } else if (item instanceof SwitchItem) {
            // Normalize ON/OFF to 1/0
            return new DynamoDBBigDecimalItem(name, convert(state, DecimalType.class).toBigDecimal(), time, expireDays);
        } else {
            throw new IllegalArgumentException("Unsupported item " + item.getClass().getSimpleName());
        }
    }

    @Override
    public @Nullable HistoricItem asHistoricItem(final Item item) {
        return asHistoricItem(item, null);
    }

    @Override
    public @Nullable HistoricItem asHistoricItem(final Item item, @Nullable Unit<?> targetUnit) {
        final State deserializedState;
        if (this.getState() == null) {
            return null;
        }
        try {
            deserializedState = accept(new DynamoDBItemVisitor<@Nullable State>() {

                @Override
                public @Nullable State visit(DynamoDBStringItem dynamoStringItem) {
                    String stringState = dynamoStringItem.getState();
                    if (stringState == null) {
                        return null;
                    }
                    if (item instanceof ColorItem) {
                        return new HSBType(stringState);
                    } else if (item instanceof LocationItem) {
                        return new PointType(stringState);
                    } else if (item instanceof PlayerItem) {
                        // Backwards-compatibility with legacy schema. New schema uses DynamoDBBigDecimalItem
                        try {
                            return PlayPauseType.valueOf(stringState);
                        } catch (IllegalArgumentException e) {
                            return RewindFastforwardType.valueOf(stringState);
                        }
                    } else if (item instanceof DateTimeItem) {
                        try {
                            // Parse ZoneDateTime from string. DATEFORMATTER assumes UTC in case it is not clear
                            // from the string (should be).
                            return new DateTimeType(ZONED_DATE_TIME_CONVERTER_STRING.transformTo(stringState));
                        } catch (DateTimeParseException e) {
                            logger.warn("Failed to parse {} as date. Outputting UNDEF instead", stringState);
                            return UnDefType.UNDEF;
                        }
                    } else if (item instanceof CallItem) {
                        String parts = stringState;
                        String[] strings = parts.split(",");
                        String orig = strings[0];
                        String dest = strings[1];
                        return new StringListType(orig, dest);
                    } else {
                        return new StringType(dynamoStringItem.getState());
                    }
                }

                @Override
                public @Nullable State visit(DynamoDBBigDecimalItem dynamoBigDecimalItem) {
                    BigDecimal numberState = dynamoBigDecimalItem.getState();
                    if (numberState == null) {
                        return null;
                    }
                    if (item instanceof NumberItem numberItem) {
                        Unit<? extends Quantity<?>> unit = targetUnit == null ? numberItem.getUnit() : targetUnit;
                        if (unit != null) {
                            return new QuantityType<>(numberState, unit);
                        } else {
                            return new DecimalType(numberState);
                        }
                    } else if (item instanceof DimmerItem) {
                        // % values have been stored as-is
                        return new PercentType(numberState);
                    } else if (item instanceof SwitchItem) {
                        return OnOffType.from(numberState.compareTo(BigDecimal.ZERO) != 0);
                    } else if (item instanceof ContactItem) {
                        return numberState.compareTo(BigDecimal.ZERO) != 0 ? OpenClosedType.OPEN
                                : OpenClosedType.CLOSED;
                    } else if (item instanceof RollershutterItem) {
                        // Percents and UP/DOWN have been stored % values (not fractional)
                        return new PercentType(numberState);
                    } else if (item instanceof PlayerItem) {
                        if (numberState.equals(PLAY_BIGDECIMAL)) {
                            return PlayPauseType.PLAY;
                        } else if (numberState.equals(PAUSE_BIGDECIMAL)) {
                            return PlayPauseType.PAUSE;
                        } else if (numberState.equals(FAST_FORWARD_BIGDECIMAL)) {
                            return RewindFastforwardType.FASTFORWARD;
                        } else if (numberState.equals(REWIND_BIGDECIMAL)) {
                            return RewindFastforwardType.REWIND;
                        } else {
                            throw new IllegalArgumentException("Unknown serialized value");
                        }
                    } else {
                        logger.warn(
                                "Not sure how to convert big decimal item {} to type {}. Using StringType as fallback",
                                dynamoBigDecimalItem.getName(), item.getClass());
                        return new StringType(numberState.toString());
                    }
                }
            });
            if (deserializedState == null) {
                return null;
            }
            return new DynamoDBHistoricItem(item.getName(), deserializedState, getTime().toInstant());
        } catch (Exception e) {
            logger.trace("Failed to convert state '{}' to item {} {}: {} {}. Data persisted with incompatible item.",
                    this.state, item.getClass().getSimpleName(), item.getName(), e.getClass().getSimpleName(),
                    e.getMessage());
            return null;
        }
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
    public abstract <R> R accept(DynamoDBItemVisitor<R> visitor);

    @Override
    public String toString() {
        @Nullable
        T localState = state;
        return DATEFORMATTER.format(time) + ": " + name + " -> "
                + (localState == null ? "<null>" : localState.toString());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ZonedDateTime getTime() {
        return time;
    }

    @Override
    @Nullable
    public Long getExpiryDate() {
        return expiry;
    }

    @Override
    public void setTime(ZonedDateTime time) {
        this.time = time;
    }

    @Override
    public @Nullable Integer getExpireDays() {
        return expireDays;
    }

    @Override
    public void setExpireDays(@Nullable Integer expireDays) {
        this.expireDays = expireDays;
    }

    public void setExpiry(@Nullable Long expiry) {
        this.expiry = expiry;
    }
}
