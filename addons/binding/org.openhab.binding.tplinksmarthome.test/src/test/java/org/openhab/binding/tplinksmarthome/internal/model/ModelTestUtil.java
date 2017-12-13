/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;

/**
 * Util class for reading test resources.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public final class ModelTestUtil {

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
    public static <T> T toJson(Gson gson, String filename, Class<T> clazz) throws IOException {
        return gson.fromJson(readJson(filename), clazz);
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
        return IOUtils
                .toString(ModelTestUtil.class.getResourceAsStream(filename + ".json"), StandardCharsets.UTF_8.name())
                .replaceAll("[\n\r\t ]", "");
    }
}
