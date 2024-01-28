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

import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.measure.Unit;

import org.bson.Document;
import org.bson.types.Binary;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.*;
import org.openhab.core.library.types.*;
import org.openhab.core.persistence.FilterCriteria.Operator;
import org.openhab.core.types.*;
import org.openhab.core.types.State;

/**
 * This class handles the conversion of types between openHAB and MongoDB.
 * It provides methods to convert openHAB states to MongoDB compatible types and vice versa.
 * It also provides a method to convert openHAB filter operators to MongoDB query operators.
 *
 * @author René Ulbricht - Initial contribution
 */
public class MongoDBTypeConversions {
    /**
     * A map of converters that convert openHAB states to MongoDB compatible types.
     * Each converter is a function that takes an openHAB state and returns an object that can be stored in MongoDB.
     */
    private static final Map<Class<? extends State>, Function<State, Object>> STATE_CONVERTERS = Map.of(HSBType.class,
            state -> state.toString(), QuantityType.class,
            state -> ((QuantityType<?>) state).toBigDecimal().doubleValue(), PercentType.class,
            state -> ((PercentType) state).intValue(), DateTimeType.class,
            state -> ((DateTimeType) state).getZonedDateTime().toString(), StringListType.class,
            state -> ((StringListType) state).toString(), DecimalType.class,
            state -> ((DecimalType) state).toBigDecimal().doubleValue(), RawType.class, state -> {
                RawType rawType = (RawType) state;
                Document doc = new Document();
                doc.put(MongoDBFields.FIELD_VALUE_TYPE, rawType.getMimeType());
                doc.put(MongoDBFields.FIELD_VALUE_DATA, rawType.getBytes());
                return doc;
            });

    /**
     * Converts an openHAB state to a MongoDB compatible type.
     *
     * @param state The openHAB state to convert.
     * @return The MongoDB compatible type.
     */
    public static Object convertValue(State state) {
        return STATE_CONVERTERS.getOrDefault(state.getClass(), State::toString).apply(state);
    }

    /**
     * A map of converters that convert MongoDB documents to openHAB states.
     * Each converter is a function that takes an openHAB item and a MongoDB document and returns an openHAB state.
     */
    private static final Map<Class<? extends Item>, BiFunction<Item, Document, State>> ITEM_STATE_CONVERTERS;

    static {
        ITEM_STATE_CONVERTERS = new LinkedHashMap<>();
        ITEM_STATE_CONVERTERS.put(NumberItem.class, (item, doc) -> {
            NumberItem numberItem = (NumberItem) item;
            Unit<?> unit = numberItem.getUnit();
            if (unit != null) {
                return new QuantityType<>(doc.getDouble(MongoDBFields.FIELD_VALUE), unit);
            } else {
                return new DecimalType(doc.getDouble(MongoDBFields.FIELD_VALUE));
            }
        });
        ITEM_STATE_CONVERTERS.put(ColorItem.class,
                (item, doc) -> new HSBType(doc.getString(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(DimmerItem.class,
                (item, doc) -> new PercentType(doc.getInteger(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(SwitchItem.class,
                (item, doc) -> OnOffType.valueOf(doc.getString(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(ContactItem.class,
                (item, doc) -> OpenClosedType.valueOf(doc.getString(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(RollershutterItem.class,
                (item, doc) -> new PercentType(doc.getInteger(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(DateTimeItem.class,
                (item, doc) -> new DateTimeType(ZonedDateTime.parse(doc.getString(MongoDBFields.FIELD_VALUE))));
        ITEM_STATE_CONVERTERS.put(LocationItem.class,
                (item, doc) -> new PointType(doc.getString(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(PlayerItem.class,
                (item, doc) -> PlayPauseType.valueOf(doc.getString(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(CallItem.class,
                (item, doc) -> new StringListType(doc.getString(MongoDBFields.FIELD_VALUE)));
        ITEM_STATE_CONVERTERS.put(ImageItem.class, (item, doc) -> {
            Document fieldValue = (Document) doc.get(MongoDBFields.FIELD_VALUE);
            String type = fieldValue.getString(MongoDBFields.FIELD_VALUE_TYPE);
            Binary data = fieldValue.get(MongoDBFields.FIELD_VALUE_DATA, Binary.class);
            return new RawType(data.getData(), type);
        });
        ITEM_STATE_CONVERTERS.put(GenericItem.class,
                (item, doc) -> new StringType(doc.getString(MongoDBFields.FIELD_VALUE)));
    }

    /**
     * Converts a MongoDB document to an openHAB state.
     *
     * @param item The openHAB item that the state belongs to.
     * @param doc The MongoDB document to convert.
     * @return The openHAB state.
     * @throws IllegalArgumentException If the item type is not supported.
     */
    public static State getStateFromDocument(Item item, Document doc) {
        return ITEM_STATE_CONVERTERS.entrySet().stream().filter(entry -> entry.getKey().isInstance(item)).findFirst()
                .map(entry -> entry.getValue().apply(item, doc))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported item type: " + item.getClass().getName()));
    }

    /**
     * Converts an openHAB filter operator to a MongoDB query operator.
     *
     * @param operator The openHAB filter operator to convert.
     * @return The MongoDB query operator, or null if the operator is not supported.
     */
    public static @Nullable String convertOperator(Operator operator) {
        switch (operator) {
            case EQ:
                return "$eq";
            case GT:
                return "$gt";
            case GTE:
                return "$gte";
            case LT:
                return "$lt";
            case LTE:
                return "$lte";
            case NEQ:
                return "$neq";
            default:
                return null;
        }
    }
}
