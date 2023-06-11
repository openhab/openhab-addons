/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nest.internal.sdm.dto;

import static org.openhab.binding.nest.internal.sdm.dto.SDMGson.GSON;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.stream.JsonWriter;

/**
 * Utility class for working with Nest SDM test data in unit tests.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public class SDMDataUtil {

    public static Reader openDataReader(String fileName) throws FileNotFoundException {
        String packagePath = (SDMDataUtil.class.getPackage().getName()).replaceAll("\\.", "/");
        String filePath = "src/test/resources/" + packagePath + "/" + fileName;

        InputStream inputStream = new FileInputStream(filePath);
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    public static <T> T fromJson(String fileName, Class<T> dataClass) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            return GSON.fromJson(reader, dataClass);
        }
    }

    public static String fromFile(String fileName) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            return new BufferedReader(reader).lines().parallel().collect(Collectors.joining("\n"));
        }
    }

    public static String toJson(Object object) {
        StringWriter writer = new StringWriter();
        JsonWriter jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent("  ");
        GSON.toJson(object, object.getClass(), jsonWriter);
        return writer.toString();
    }
}
