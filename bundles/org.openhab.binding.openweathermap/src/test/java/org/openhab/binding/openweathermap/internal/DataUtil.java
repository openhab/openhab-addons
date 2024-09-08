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
package org.openhab.binding.openweathermap.internal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;

/**
 * Utility class for working with test data in unit tests
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class DataUtil {
    @SuppressWarnings("null")
    public static Reader openDataReader(String fileName) throws FileNotFoundException {
        String filePath = "src/test/resources/" + fileName;

        InputStream inputStream = new FileInputStream(filePath);
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    public static <T> T fromJson(String fileName, Type typeOfT) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            return new Gson().fromJson(reader, typeOfT);
        }
    }

    @SuppressWarnings("null")
    public static String fromFile(String fileName) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            return new BufferedReader(reader).lines().parallel().collect(Collectors.joining("\n"));
        }
    }
}
