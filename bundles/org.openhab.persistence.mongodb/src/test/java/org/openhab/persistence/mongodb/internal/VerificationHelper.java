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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.types.Binary;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringListType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
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
@NonNullByDefault
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

        // Use the map to handle the expected value
        BiFunction<Object, Document, Pair<Object, Object>> handler = HandleTypes.get(expectedValue.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("Unsupported type: " + expectedValue.getClass());
        }
        Pair<Object, Object> values = handler.apply(expectedValue, document);

        JsonWriterSettings jsonWriterSettings = JsonWriterSettings.builder().indent(true).build();
        assertEquals(values.left, values.right,
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

    // Define a map from types to functions that handle those types
    private static final Map<Class<?>, BiFunction<Object, Document, Pair<Object, Object>>> HandleTypes = Map.ofEntries(
            Map.entry(Double.class, VerificationHelper::handleGeneric),
            Map.entry(String.class, VerificationHelper::handleGeneric),
            Map.entry(HSBType.class, VerificationHelper::handleToString),
            Map.entry(DecimalType.class, VerificationHelper::handleDecimalType),
            Map.entry(DateTimeType.class, VerificationHelper::handleDateTimeType),
            Map.entry(IncreaseDecreaseType.class, VerificationHelper::handleToString),
            Map.entry(RewindFastforwardType.class, VerificationHelper::handleToString),
            Map.entry(NextPreviousType.class, VerificationHelper::handleToString),
            Map.entry(OnOffType.class, VerificationHelper::handleToString),
            Map.entry(OpenClosedType.class, VerificationHelper::handleToString),
            Map.entry(PercentType.class, VerificationHelper::handlePercentType),
            Map.entry(PlayPauseType.class, VerificationHelper::handleToString),
            Map.entry(PointType.class, VerificationHelper::handleToString),
            Map.entry(StopMoveType.class, VerificationHelper::handleToString),
            Map.entry(StringListType.class, VerificationHelper::handleToString),
            Map.entry(StringType.class, VerificationHelper::handleGeneric),
            Map.entry(UpDownType.class, VerificationHelper::handleToString),
            Map.entry(QuantityType.class, VerificationHelper::handleQuantityType),
            Map.entry(RawType.class, VerificationHelper::handleRawType));

    private static Pair<Object, Object> handleGeneric(Object ev, Document doc) {
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        return Pair.of(ev, value != null ? value : new Object());
    }

    private static Pair<Object, Object> handleToString(Object ev, Document doc) {
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        return Pair.of(ev.toString(), value != null ? value : new Object());
    }

    private static Pair<Object, Object> handleDecimalType(Object ev, Document doc) {
        Double value = doc.getDouble(MongoDBFields.FIELD_VALUE);
        return Pair.of(((DecimalType) ev).doubleValue(), value != null ? value : new Object());
    }

    private static Pair<Object, Object> handleDateTimeType(Object ev, Document doc) {
        String value = doc.getString(MongoDBFields.FIELD_VALUE);
        return Pair.of(((DateTimeType) ev).getZonedDateTime().toString(), value != null ? value : new Object());
    }

    private static Pair<Object, Object> handlePercentType(Object ev, Document doc) {
        Integer value = doc.getInteger(MongoDBFields.FIELD_VALUE);
        return Pair.of(((PercentType) ev).intValue(), value != null ? value : new Object());
    }

    private static Pair<Object, Object> handleQuantityType(Object ev, Document doc) {
        Double value = doc.getDouble(MongoDBFields.FIELD_VALUE);
        String unit = doc.getString(MongoDBFields.FIELD_UNIT);
        if (value != null && unit != null) {
            QuantityType<?> quantityType = (QuantityType<?>) ev;
            return Pair.of(quantityType.doubleValue() + "--" + quantityType.getUnit(), value + "--" + unit);
        }
        return Pair.of(new Object(), new Object());
    }

    private static Pair<Object, Object> handleRawType(Object ev, Document doc) {
        RawType rawType = (RawType) ev;
        Document expectedDoc = new Document();
        expectedDoc.put(MongoDBFields.FIELD_VALUE_TYPE, rawType.getMimeType());
        expectedDoc.put(MongoDBFields.FIELD_VALUE_DATA, new Binary(rawType.getBytes()));
        Object value = doc.get(MongoDBFields.FIELD_VALUE);
        return Pair.of(expectedDoc, value != null ? value : new Object());
    }

    public record Pair<L, R> (L left, R right) {
        public static <L, R> Pair<L, R> of(final L left, final R right) {
            return left != null || right != null ? new Pair<>(left, right) : new Pair<>(null, null);
        }
    }
}
