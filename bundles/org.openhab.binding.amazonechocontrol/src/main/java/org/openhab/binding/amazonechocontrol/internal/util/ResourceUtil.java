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
package org.openhab.binding.amazonechocontrol.internal.util;

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
 * The {@link ResourceUtil} is a set of utils for handling bundle resources
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ResourceUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceUtil.class);

    private ResourceUtil() {
        // prevent instantiation
    }

    /**
     * Get an {@link InputStream} that reads from a file in a bundle
     *
     * @param clazz a class contained in the bundle
     * @param fileName the fileName in the resources folder (including path)
     * @return the {@link InputStream} as {@link Optional} (empty if not found)
     */
    public static Optional<InputStream> getResourceStream(Class<?> clazz, String fileName) {
        // we need the classloader of the bundle that our handler belongs to
        ClassLoader classLoader = clazz.getClassLoader();
        if (classLoader == null) {
            LOGGER.warn("Could not get classloader for class '{}'", clazz);
            return Optional.empty();
        }

        return Optional.ofNullable(classLoader.getResourceAsStream(fileName));
    }

    /**
     * Read a .properties file from a bundle
     *
     * @param clazz a class contained in the bundle
     * @param fileName the fileName in the resources folder (including path)
     * @return a {@link Map} of strings containing the file contents (empty if file not found)
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
}
