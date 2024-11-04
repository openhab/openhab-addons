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
package org.openhab.binding.lgthinq.handler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link JsonUtils}
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class JsonUtils {
    public static String loadJson(String fileName) {
        ClassLoader classLoader = JsonUtils.class.getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(
                        "Unexpected error. It is not expected this behaviour since json test files must be present: "
                                + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Unexpected error. It is not expected this behaviour since json test files must be present.", e);
        }
    }
}
