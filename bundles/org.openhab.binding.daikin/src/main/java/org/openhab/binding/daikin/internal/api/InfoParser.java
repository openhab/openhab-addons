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
package org.openhab.binding.daikin.internal.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for parsing the comma separated values and array values returned by the Daikin Controller.
 *
 * @author Jimmy Tanagra - Initial Contribution
 *         urldecode the parsed value
 *
 */
@NonNullByDefault
public class InfoParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfoParser.class);

    private InfoParser() {
    }

    public static Map<String, String> parse(String response) {
        return Stream.of(response.split(",")).filter(kv -> kv.contains("=")).map(kv -> {
            String[] keyValue = kv.split("=");
            String key = keyValue[0];
            String value = keyValue.length > 1 ? urldecode(keyValue[1]) : "";
            return new String[] { key, value };
        }).collect(Collectors.toMap(x -> x[0], x -> x[1]));
    }

    public static Optional<Double> parseDouble(String value) {
        if ("-".equals(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Double.parseDouble(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer> parseInt(String value) {
        if ("-".equals(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer[]> parseArrayOfInt(String value) {
        if ("-".equals(value)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Stream.of(value.split("/")).map(val -> Integer.parseInt(val)).toArray(Integer[]::new));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static Optional<Integer[]> parseArrayOfInt(String value, int expectedArraySize) {
        Optional<Integer[]> result = parseArrayOfInt(value);
        if (result.isPresent() && result.get().length == expectedArraySize) {
            return result;
        }
        return Optional.empty();
    }

    public static String urldecode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unsupported encoding error in '{}'", value, e);
            return value;
        }
    }
}
