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
package org.openhab.binding.tplinksmarthome.internal.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;

/**
 * Util class for reading test resources.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public final class ModelTestUtil {

    public static final Gson GSON = GsonUtil.createGson();

    private ModelTestUtil() {
        // Util class
    }

    /**
     * Util method to read a json file into it's data class.
     *
     * @param <T> Type of the class the json data represents.
     * @param gson gson class
     * @param filename filename of the json file to read. The file is read relative to the directory of this class
     * @param clazz Data class expected to be read from the json file
     * @return instance of clazz with read data from json file
     * @throws IOException when file could not be read.
     */
    public static <T> T jsonFromFile(String filename, Class<T> clazz) throws IOException {
        return GSON.fromJson(readJson(filename), clazz);
    }

    /**
     * Util method to read a json file. It normalizes the string by removing returns, tabs and spaces. It's not very
     * smart as it removes spaces inside json values as well. But this method is mainly intended be able to compare 2
     * json strings.
     *
     * @param filename filename of the json file to read. The file is read relative to the directory of this class
     * @return read json string
     * @throws IOException when file could not be read.
     */
    public static String readJson(String filename) throws IOException {
        return new String(ModelTestUtil.class.getResourceAsStream(filename + ".json").readAllBytes(),
                StandardCharsets.UTF_8).replaceAll("[\n\r\t ]", "");
    }
}
