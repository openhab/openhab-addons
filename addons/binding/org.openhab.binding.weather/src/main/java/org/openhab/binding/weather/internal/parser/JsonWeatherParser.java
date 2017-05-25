/**
 * Copyright (c) 2010-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weather.internal.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map.Entry;

import org.openhab.binding.weather.internal.model.Weather;
import org.openhab.binding.weather.internal.utils.PropertyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Weather parser with JSON data in the InputStream.
 *
 * @author Gerhard Riegler
 * @since 1.6.0
 */
public class JsonWeatherParser extends AbstractWeatherParser {
    private static final Logger logger = LoggerFactory.getLogger(JsonWeatherParser.class);
    JsonParser parser = new JsonParser();

    public JsonWeatherParser(CommonIdHandler commonIdHandler) {
        super(commonIdHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void parseInto(InputStream is, Weather weather) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);

        // JsonElement element = parser.parse(reader);
        JsonElement element = parser.parse(reader);
        handleToken(element, null, weather);
        reader.close();

        super.parseInto(is, weather);
    }

    /**
     * Iterates through the JSON structure and collects weather data.
     */
    private void handleToken(JsonElement element, String property, Weather weather) throws Exception {
        if (element.isJsonArray()) {
            // go through all the elements and send back to ourselves.
            JsonArray array = (JsonArray) element;
            for (JsonElement arrayElement : array) {
                Weather forecast = startIfForecast(weather, property);
                handleToken(arrayElement, property, forecast);
                endIfForecast(weather, forecast, property);
            }
        } else if (element.isJsonObject()) {
            JsonObject object = (JsonObject) element;
            for (Entry<String, JsonElement> entry : object.entrySet()) {
                String prop = PropertyResolver.add(property, entry.getKey());
                if (entry.getValue().isJsonPrimitive()) {
                    try {
                        setValue(weather, prop, entry.getValue().getAsString());
                    } catch (Exception ex) {
                        logger.error("Error setting property '{}' with value '{}' ({})", prop,
                                entry.getValue().getAsString(), ex.getMessage());
                    }
                } else {
                    handleToken(entry.getValue(), prop, weather);
                }
            }
        }
    }
}
