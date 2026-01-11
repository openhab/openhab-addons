/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.viessmann.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ViessmannUtil} class provides utility methods for the Viessmann binding.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public final class ViessmannUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViessmannUtil.class);

    private ViessmannUtil() {
        throw new AssertionError("No instances allowed");
    }

    /**
     * Gets a resource file as {@link InputStream}.
     *
     * @param clazz class used to get the classloader
     * @param fileName resource file name
     * @return optional stream of the resource
     */
    public static Optional<InputStream> getResourceStream(Class<?> clazz, String fileName) {
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader == null) {
            LOGGER.warn("Could not get classloader for class '{}'", clazz);
            return Optional.empty();
        }
        return Optional.ofNullable(classLoader.getResourceAsStream(fileName));
    }

    /**
     * Reads a properties file into a {@link Map}.
     *
     * @param clazz class used to get the classloader
     * @param fileName properties file name
     * @return map with key-value pairs, or empty map if not readable
     */
    public static Map<String, String> readProperties(Class<?> clazz, String fileName) {
        return Objects.requireNonNull(getResourceStream(clazz, fileName).map(inputStream -> {
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
                return properties.entrySet().stream()
                        .collect(Collectors.toMap(e -> (String) e.getKey(), e -> (String) e.getValue()));
            } catch (IOException e) {
                LOGGER.warn("Could not read resource file '{}', binding will probably fail: {}", fileName,
                        e.getMessage());
                return new HashMap<String, String>();
            }
        }).orElse(Map.of()));
    }

    /**
     * Converts camelCase to hyphenated lowercase.
     *
     * @param input camelCase string
     * @return hyphenated lowercase string
     */
    public static String camelToHyphen(String input) {
        String result = input.replaceAll("([a-z])([A-Z])", "$1-$2").replaceAll("([A-Z])([A-Z][a-z])", "$1-$2");
        result = result.replaceAll("([a-zA-Z])([0-9]+)", "$1-$2").replaceAll("([0-9]+)([a-zA-Z])", "$1-$2");
        return result.toLowerCase();
    }
}
