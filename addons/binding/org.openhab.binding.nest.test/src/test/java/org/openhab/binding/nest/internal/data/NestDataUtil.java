/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.openhab.binding.nest.internal.NestUtils;

/**
 * Utility class for working with Nest test data in unit tests.
 *
 * @author Wouter Born - Increase test coverage
 */
public final class NestDataUtil {

    public static final String COMPLETE_DATA_FILE_NAME = "top-level-streaming-data.json";
    public static final String INCOMPLETE_DATA_FILE_NAME = "top-level-streaming-data-incomplete.json";
    public static final String EMPTY_DATA_FILE_NAME = "top-level-streaming-data-empty.json";

    public static final String CAMERA1_DEVICE_ID = "_LK8j9rRXwCKEBOtDo7JskNxzWfHBOIm3CLouCT3FQZzrvokK_DzFQ";
    public static final String CAMERA1_WHERE_ID = "z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKCxvyZfxNpKA";

    public static final String CAMERA2_DEVICE_ID = "VG7C7BU6Zf8OjEfizmBCVnwnuKHSnOBIHgbQKa57xKJzrvokK_DzFQ";
    public static final String CAMERA2_WHERE_ID = "qpWvTu89Knhn6GRFM-VtGoE4KYwbzbJg9INR6WyPfhW1EJ04GRyYbQ";

    public static final String SMOKE1_DEVICE_ID = "p1b1oySOcs_sbi4iczruW3Ou-iQr8PMV";
    public static final String SMOKE1_WHERE_ID = "z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsIm5E0NfJPeeg";

    public static final String SMOKE2_DEVICE_ID = "p1b1oySOcs8W9WwaNu80oXOu-iQr8PMV";
    public static final String SMOKE2_WHERE_ID = "z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKCxvyZfxNpKA";

    public static final String SMOKE3_DEVICE_ID = "p1b1oySOcs-OJHIgmgeMkHOu-iQr8PMV";
    public static final String SMOKE3_WHERE_ID = "6UAWzz8czKpFrH6EK3AcjDiTjbRgts8x5MJxEnn1yKKQpYTBO7n2UQ";

    public static final String SMOKE4_DEVICE_ID = "p1b1oySOcs8Qu7IAJVrQ7XOu-iQr8PMV";
    public static final String SMOKE4_WHERE_ID = "z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKQrCrjN0yXiw";

    public static final String STRUCTURE1_STRUCTURE_ID = "ysCnsCaq1pQwKUPP9H4AqE943C1XtLin3x6uCVN5Qh09IDyTg7Ey5A";

    public static final String THERMOSTAT1_DEVICE_ID = "G1jouHN5yl6mXFaQw5iGwXOu-iQr8PMV";
    public static final String THERMOSTAT1_WHERE_ID = "z8fK075vJJPPWnXxLx1m3GskRSZQ64iQydB59k-UPsKQrCrjN0yXiw";

    private NestDataUtil() {
        // Hidden utility class constructor
    }

    public static Reader openDataReader(String fileName) throws UnsupportedEncodingException {
        String packagePath = (NestDataUtil.class.getPackage().getName()).replaceAll("\\.", "/");
        String filePath = "src/test/resources/" + packagePath + "/" + fileName;
        InputStream inputStream = NestDataUtil.class.getClassLoader().getResourceAsStream(filePath);
        return new InputStreamReader(inputStream, "UTF-8");
    }

    public static <T> T fromJson(String fileName, Class<T> dataClass) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            return NestUtils.fromJson(reader, dataClass);
        }
    }

    public static String fromFile(String fileName, Unit<Temperature> temperatureUnit) throws IOException {
        String json = fromFile(fileName);
        if (temperatureUnit == SIUnits.CELSIUS) {
            json = json.replace("\"temperature_scale\": \"F\"", "\"temperature_scale\": \"C\"");
        } else if (temperatureUnit == ImperialUnits.FAHRENHEIT) {
            json = json.replace("\"temperature_scale\": \"C\"", "\"temperature_scale\": \"F\"");
        }
        return json;
    }

    public static String fromFile(String fileName) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            StringWriter writer = new StringWriter();
            char[] buffer = new char[1024 * 4];
            int n = 0;
            while (-1 != (n = reader.read(buffer))) {
                writer.write(buffer, 0, n);
            }
            return writer.toString();
        }
    }

}
