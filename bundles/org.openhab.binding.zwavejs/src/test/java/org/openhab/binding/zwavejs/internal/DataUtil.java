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
package org.openhab.binding.zwavejs.internal;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.zwavejs.internal.api.adapter.InstantAdapter;
import org.openhab.binding.zwavejs.internal.api.dto.Node;
import org.openhab.binding.zwavejs.internal.api.dto.messages.ResultMessage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

/**
 * Utility class for working with test data in unit tests
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class DataUtil {

    public static Reader openDataReader(String fileName) throws FileNotFoundException {
        String filePath = "src/test/resources/json/" + fileName;

        InputStream inputStream = new FileInputStream(filePath);
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    public static <T> T fromJson(String fileName, Type typeOfT) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            Gson gson = new GsonBuilder().setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
                    .registerTypeAdapter(Instant.class, new InstantAdapter()).create();

            return gson.fromJson(reader, typeOfT);
        }
    }

    public static String fromFile(String fileName) throws IOException {
        try (Reader reader = openDataReader(fileName)) {
            return Objects
                    .requireNonNull(new BufferedReader(reader).lines().parallel().collect(Collectors.joining("\n")));
        }
    }

    public static Node getNodeFromStore(String storeFileName, int nodeId) throws IOException {
        ResultMessage resultMessage = DataUtil.fromJson(storeFileName, ResultMessage.class);
        return Objects.requireNonNull(
                resultMessage.result.state.nodes.stream().filter(f -> f.nodeId == nodeId).findAny().orElse(null));
    }
}
