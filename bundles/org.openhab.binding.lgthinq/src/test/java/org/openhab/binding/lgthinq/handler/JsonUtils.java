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
package org.openhab.binding.lgthinq.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * The {@link JsonUtils}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class JsonUtils {
    public static <T> T unmashallJson(String fileName) {
        InputStream inputStream = JsonUtils.class.getResourceAsStream(fileName);
        try {
            return new ObjectMapper().readValue(inputStream, new TypeReference<>() {
            });
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unexpected error. It is not expected this behaviour since json test files must be present.");
        }
    }

    public static String loadJson(String fileName) {
        try {
            ClassLoader classLoader = JsonUtils.class.getClassLoader();
            URL fileUrl = classLoader.getResource(fileName);
            if (fileUrl == null) {
                throw new IllegalArgumentException(
                        "Unexpected error. It is not expected this behaviour since json test files must be present: "
                                + fileName);
            }
            byte[] encoded = Files.readAllBytes(new File(fileUrl.getFile()).toPath());
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unexpected error. It is not expected this behaviour since json test files must be present.", e);
        }
    }
}
