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
package org.openhab.persistence.mongodb.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.measure.Unit;

import org.bson.Document;
import org.bson.types.Binary;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.GroupItem;
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
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.types.util.UnitUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the conversion of types between openHAB and MongoDB.
 * It provides methods to convert openHAB states to MongoDB compatible types and vice versa.
 * It also provides a method to convert openHAB filter operators to MongoDB query operators.
 *
 * @author René Ulbricht - Initial contribution
 */
@NonNullByDefault
public class MongoDBTypeConversions {

    /**
     * Converts a MongoDB document to an openHAB state.
     *
     * @param item The openHAB item that the state belongs to.
     * @param doc The MongoDB document to convert.
     * @return The openHAB state.
     * @throws IllegalArgumentException If the item type is not supported.
     */
    public static State getStateFromDocument(Item item, Document doc) {
        Item realItem = item instanceof GroupItem groupItem ? groupItem.getBaseItem() : item;
        BiFunction<Item, Document, State> converter = ITEM_STATE_CONVERTERS.get(realItem.getClass());
        if (converter != null) {
            return converter.apply(realItem, doc);
        } else {
            throw new IllegalArgumentException("Unsupported item type: " + realItem.getClass().getName());
        }
    }

    /**
     * Converts an openHAB filter operator to a MongoDB query operator.
     *
     * @param operator The openHAB filter operator to convert.
     * @return The MongoDB query operator, or null if the operator is not supported.
     */
    public static @Nullable String convertOperator(Operator operator) {
        return switch (operator) {
            case EQ -> "$eq";
            case GT -> "$gt";
            case GTE -> "$gte";
            case LT -> "$lt";
            case LTE -> "$lte";
            case NEQ -> "$neq";
            default -> null;
        };
    }

    /**
     * Converts an openHAB state to a MongoDB compatible type.
     *
     * @param state The openHAB state to convert.
     * @return The MongoDB compatible type.
     */
    public static Object convertValue(State state) {
        return STATE_CONVERTERS.getOrDefault(state.getClass(), State::toString).apply(state);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDBTypeConversions.class);

    /**
     * A map of converters that convert openHAB states to MongoDB compatible types.
     * Each converter is a function that takes an openHAB state and returns an object that can be stored in MongoDB.
     */
    private static final Map<Class<? extends State>, Function<State, Object>> STATE_CONVERTERS = Map.of( //
            HSBType.class, State::toString, //
            QuantityType.class, state -> ((QuantityType<?>) state).toBigDecimal().doubleValue(), //
            PercentType.class, state -> ((PercentType) state).intValue(), //
            DateTimeType.class, state -> ((DateTimeType) state).getZonedDateTime().toString(), //
            StringListType.class, State::toString, //
            DecimalType.class, state -> ((DecimalType) state).toBigDecimal().doubleValue(), //
            RawType.class, MongoDBTypeConversions::handleRawType//
    );

    private static Object handleRawType(State state) {
        RawType rawType = (RawType) state;
        Document doc = new Document();
        doc.put(MongoDBFields.FIELD_VALUE_TYPE, rawType.getMimeType());
        doc.put(MongoDBFields.FIELD_VALUE_DATA, rawType.getBytes());
        return doc;
    }

    /**
     * A map of converters that convert MongoDB documents to openHAB states.
     * Each converter is a function that takes an openHAB item and a MongoDB document and returns an openHAB state.
     */

    private static final Map<Class<? extends Item>, BiFunction<Item, Document, State>> ITEM_STATE_CONVERTERS = //
            Map.ofEntries( //
                    Map.entry(NumberItem.class, MongoDBTypeConversions::handleNumberItem),
                    Map.entry(ColorItem.class, MongoDBTypeConversions::handleColorItem),
                    Map.entry(DimmerItem.class, MongoDBTypeConversions::handleDimmerItem),
                    Map.entry(SwitchItem.class,
                            (Item item, Document doc) -> OnOffType.valueOf(doc.getString(MongoDBFields.FIELD_VALUE))),
                    Map.entry(ContactItem.class,
                            (Item item, Document doc) -> OpenClosedType
                                    .valueOf(doc.getString(MongoDBFields.FIELD_VALUE))),
                    Map.entry(RollershutterItem.class, MongoDBTypeConversions::handleRollershutterItem),
                    Map.entry(DateTimeItem.class, MongoDBTypeConversions::handleDateTimeItem),
                    Map.entry(LocationItem.class,
                            (Item item, Document doc) -> new PointType(doc.getString(MongoDBFields.FIELD_VALUE))),
                    Map.entry(PlayerItem.class,
                            (Item item, Document doc) -> PlayPauseType
                                    .valueOf(doc.getString(MongoDBFields.FIELD_VALUE))),
                    Map.entry(CallItem.class,
                            (Item item, Document doc) -> new StringListType(doc.getString(MongoDBFields.FIELD_VALUE))),
                    Map.entry(ImageItem.class, MongoDBTypeConversions::handleImageItem), //
                    Map.entry(StringItem.class,
                            (Item item, Document doc) -> new StringType(doc.getString(MongoDBFields.FIELD_VALUE))),
                    Map.entry(GenericItem.class,
                            (Item item, Document doc) -> new StringType(doc.getString(MongoDBFields.FIELD_VALUE)))//
            );

    private static State handleNumberItem(Item item, Document doc) {
        NumberItem numberItem = (NumberItem) item;
        Unit<?> unit = numberItem.getUnit();
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        if (value == null) {
            return UnDefType.UNDEF;
        }
        if (doc.containsKey(MongoDBFields.FIELD_UNIT)) {
            String unitString = doc.getString(MongoDBFields.FIELD_UNIT);
            Unit<?> docUnit = UnitUtils.parseUnit(unitString);
            if (docUnit != null) {
                unit = docUnit;
            }
        }
        if (value instanceof String) {
            return new QuantityType<>(value.toString());
        }
        if (unit != null) {
            return new QuantityType<>(((Number) value).doubleValue(), unit);
        } else {
            return new DecimalType(((Number) value).doubleValue());
        }
    }

    private static State handleColorItem(Item item, Document doc) {
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        if (value instanceof String) {
            return new HSBType(value.toString());
        } else {
            LOGGER.warn("HSBType ({}) value is not a valid string: {}", doc.getString(MongoDBFields.FIELD_REALNAME),
                    value);
            return new HSBType("0,0,0");
        }
    }

    private static State handleDimmerItem(Item item, Document doc) {
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        if (value == null) {
            return UnDefType.UNDEF;
        }
        if (value instanceof Integer) {
            return new PercentType((Integer) value);
        } else {
            return new PercentType(((Number) value).intValue());
        }
    }

    private static State handleRollershutterItem(Item item, Document doc) {
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        if (value == null) {
            return UnDefType.UNDEF;
        }
        if (value instanceof Integer) {
            return new PercentType((Integer) value);
        } else {
            return new PercentType(((Number) value).intValue());
        }
    }

    private static State handleDateTimeItem(Item item, Document doc) {
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        if (value == null) {
            return UnDefType.UNDEF;
        }
        if (value instanceof String) {
            return new DateTimeType(ZonedDateTime.parse(doc.getString(MongoDBFields.FIELD_VALUE)));
        } else {
            return new DateTimeType(ZonedDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault()));
        }
    }

    private static State handleImageItem(Item item, Document doc) {
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        if (value instanceof Document) {
            Document fieldValue = (Document) value;
            String type = fieldValue.getString(MongoDBFields.FIELD_VALUE_TYPE);
            Binary data = fieldValue.get(MongoDBFields.FIELD_VALUE_DATA, Binary.class);
            return new RawType(data.getData(), type);
        } else {
            LOGGER.warn("ImageItem ({}) value is not a Document: {}", doc.getString(MongoDBFields.FIELD_REALNAME),
                    value);
            return new RawType(new byte[0], "application/octet-stream");
        }
    }
}
