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
package org.openhab.binding.pixometer.internal.serializer;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pixometer.internal.config.ReadingInstance;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Custom Deserializer for meter reading api responses
 *
 * @author Jerome Luckenbach - Initial contribution
 *
 */
@NonNullByDefault
public class CustomReadingInstanceDeserializer implements JsonDeserializer<ReadingInstance> {

    private static final String COUNT = "count";
    private static final String RESULTS = "results";
    private static final String URL = "url";
    private static final String APPLIED_METHOD = "applied_method";
    private static final String READING_DATE = "reading_date";
    private static final String VALUE = "value";
    private static final String VALUE_SECOND_TARIFF = "value_second_tariff";
    private static final String PROVIDED_FRACTION_DIGITS = "provided_fraction_digits";
    private static final String PROVIDED_FRACTION_DIGITS_SECOND_TARIFF = "provided_fraction_digits_second_tariff";

    @Override
    @NonNullByDefault({})
    public ReadingInstance deserialize(final JsonElement json, final Type typeOfT,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();

        if (!jsonObject.has(COUNT)) {
            throw new JsonParseException("Invalid Json Response");
        }

        ReadingInstance result = new ReadingInstance();
        int resultCount = Integer.parseInt(jsonObject.get(COUNT).toString());

        // No readings provided yet
        if (resultCount < 1) {
            result.setReadingDate(ZonedDateTime.from(Instant.EPOCH));
            result.setValue(0);
        }

        // Fist result is the last reading instance
        JsonObject latestReading = jsonObject.getAsJsonArray(RESULTS).get(0).getAsJsonObject();

        result.setUrl(getStringFromJson(latestReading, URL));
        result.setAppliedMethod(getStringFromJson(latestReading, APPLIED_METHOD));
        result.setReadingDate(ZonedDateTime.parse(getStringFromJson(latestReading, READING_DATE)));
        result.setValue(Double.parseDouble(getStringFromJson(latestReading, VALUE)));

        // Not all meters provide useful data for second tariff and fraction digits , so zero should be used in case of
        // a null value.
        String secondTariffValue = getStringFromJson(latestReading, VALUE_SECOND_TARIFF);
        result.setValueSecondTariff(
                checkStringForNullValues(secondTariffValue) ? 0 : Double.parseDouble(secondTariffValue));

        String providedFractionDigits = getStringFromJson(latestReading, PROVIDED_FRACTION_DIGITS);
        result.setProvidedFractionDigits(
                checkStringForNullValues(providedFractionDigits) ? 0 : Integer.parseInt(providedFractionDigits));

        String secondprovidedFractionDigits = getStringFromJson(latestReading, PROVIDED_FRACTION_DIGITS_SECOND_TARIFF);
        result.setProvidedFractionDigitsSecondTariff(checkStringForNullValues(secondprovidedFractionDigits) ? 0
                : Integer.parseInt(secondprovidedFractionDigits));

        return result;
    }

    /**
     * Returns the node value and removes possible added quotation marks, which would lead to parse errors.
     *
     * @param data The Json source to get the string from
     * @param key The key for the wanted Json Node
     * @return The wanted string without unnecessary quotation marks
     */
    private String getStringFromJson(JsonObject data, String key) {
        return data.get(key).toString().replace("\"", "");
    }

    /**
     * @param s the striong to check
     * @return returns true if null values have been found, false otherwise
     */
    private boolean checkStringForNullValues(String s) {
        return (s == null || s.isEmpty() || "null".equals(s));
    }
}
