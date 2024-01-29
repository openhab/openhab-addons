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

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.types.Binary;
import org.openhab.core.library.types.*;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.HistoricItem;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * This is a helper class for verifying various aspects of the MongoDB persistence service.
 * It provides methods for verifying log messages, MongoDB documents, and query results.
 * Each verification method checks if the actual value matches the expected value and throws an
 * AssertionError if they do not match.
 *
 * @author René Ulbricht - Initial contribution
 */
public class VerificationHelper {

    /**
     * Verifies a log message.
     *
     * @param logEvent The log event to verify.
     * @param expectedMessage The expected message of the log event.
     * @param expectedLevel The expected level of the log event.
     */
    public static void verifyLogMessage(ILoggingEvent logEvent, String expectedMessage, Level expectedLevel) {
        assertEquals(expectedMessage, logEvent.getFormattedMessage());
        assertEquals(expectedLevel, logEvent.getLevel());
    }

    /**
     * Verifies a document.
     *
     * @param document The document to verify.
     * @param expectedItem The expected item of the document.
     * @param expectedValue The expected value of the document.
     */
    public static void verifyDocument(Document document, String expectedItem, Object expectedValue) {
        verifyDocumentWithAlias(document, expectedItem, expectedItem, expectedValue);
    }

    /**
     * Verifies a document with an alias.
     *
     * @param document The document to verify.
     * @param expectedAlias The expected alias of the document.
     * @param expectedRealName The expected real name of the document.
     * @param expectedValue The expected value of the document. Can be a String or a Double.
     */
    public static void verifyDocumentWithAlias(Document document, String expectedAlias, String expectedRealName,
            Object expectedValue) {
        assertEquals(expectedAlias, document.get(MongoDBFields.FIELD_ITEM));
        assertEquals(expectedRealName, document.get(MongoDBFields.FIELD_REALNAME));

        // Define a map from types to functions that handle those types
        Map<Class<?>, BiFunction<Object, Document, Pair<Object, Object>>> handlers = new HashMap<>();
        handlers.put(Double.class, (ev, doc) -> Pair.of(ev, doc.get(MongoDBFields.FIELD_VALUE)));
        handlers.put(String.class, (ev, doc) -> Pair.of(ev, doc.get(MongoDBFields.FIELD_VALUE)));
        handlers.put(HSBType.class, (ev, doc) -> Pair.of(ev.toString(), doc.get(MongoDBFields.FIELD_VALUE)));
        handlers.put(DecimalType.class,
                (ev, doc) -> Pair.of(((DecimalType) ev).doubleValue(), doc.getDouble(MongoDBFields.FIELD_VALUE)));
        handlers.put(DateTimeType.class, (ev, doc) -> Pair.of(((DateTimeType) ev).getZonedDateTime().toString(),
                doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(IncreaseDecreaseType.class,
                (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(RewindFastforwardType.class,
                (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(NextPreviousType.class,
                (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(OnOffType.class, (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(OpenClosedType.class,
                (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(PercentType.class,
                (ev, doc) -> Pair.of(((PercentType) ev).intValue(), doc.getInteger(MongoDBFields.FIELD_VALUE)));
        handlers.put(PlayPauseType.class,
                (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(PointType.class, (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(StopMoveType.class, (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(StringListType.class,
                (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(StringType.class, (ev, doc) -> Pair.of(ev, doc.get(MongoDBFields.FIELD_VALUE)));
        handlers.put(UpDownType.class, (ev, doc) -> Pair.of(ev.toString(), doc.getString(MongoDBFields.FIELD_VALUE)));
        handlers.put(QuantityType.class, (ev, doc) -> {
            QuantityType<?> quantityType = (QuantityType<?>) ev;
            return Pair.of(quantityType.doubleValue() + "--" + quantityType.getUnit(),
                    doc.getDouble(MongoDBFields.FIELD_VALUE) + "--" + doc.getString(MongoDBFields.FIELD_UNIT));
        });
        handlers.put(RawType.class, (ev, doc) -> {
            RawType rawType = (RawType) ev;
            Document expectedDoc = new Document();
            expectedDoc.put(MongoDBFields.FIELD_VALUE_TYPE, rawType.getMimeType());
            expectedDoc.put(MongoDBFields.FIELD_VALUE_DATA, new Binary(rawType.getBytes()));
            return Pair.of(expectedDoc, doc.get(MongoDBFields.FIELD_VALUE));
        });

        // Use the map to handle the expected value
        BiFunction<Object, Document, Pair<Object, Object>> handler = handlers.get(expectedValue.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported type: " + expectedValue.getClass());
        }
        Pair<Object, Object> values = handler.apply(expectedValue, document);

        JsonWriterSettings jsonWriterSettings = JsonWriterSettings.builder().indent(true).build();
        assertEquals(values.getLeft(), values.getRight(),
                "Document: (" + expectedValue.getClass().getSimpleName() + ") " + document.toJson(jsonWriterSettings));

        assertNotNull(document.get("_id"));
        assertNotNull(document.get("timestamp"));
    }

    /**
     * Verifies the result of a query.
     *
     * @param result The result of the query.
     * @param startState The state of the first item in the result.
     * @param increment The increment for the expected state.
     */
    public static void verifyQueryResult(Iterable<HistoricItem> result, int startState, int increment, int totalSize) {
        List<HistoricItem> resultList = new ArrayList<>();
        result.forEach(resultList::add);

        assertEquals(totalSize, resultList.size());

        int expectedState = startState;
        for (HistoricItem item : resultList) {
            assertEquals(expectedState, ((DecimalType) item.getState()).intValue());
            expectedState += increment;
        }
    }

    public static void verifyQueryResult(Iterable<HistoricItem> result, Object expectedState) {
        List<HistoricItem> resultList = new ArrayList<>();
        result.forEach(resultList::add);

        assertEquals(1, resultList.size());

        assertEquals(expectedState, resultList.get(0).getState());
    }
}
