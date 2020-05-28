/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link StringUtil} class defines common String utility functions, which are used across the whole binding.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public class StringUtil {

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Checks if a String is whitespace, empty ("") or null.
     *
     * @param str the String to check, may be null
     * @return true if the String is null, empty or whitespace
     */
    public static boolean isBlank(@Nullable String str) {
        return (str == null || str.trim().isEmpty());
    }

    /**
     * Returns either the passed in String, or if the String is whitespace, empty ("") or null, the value of defaultStr.
     *
     * @param str the String to check, may be null
     * @param defaultStr the default String to return
     * @return the passed in String, or the default
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return StringUtil.isBlank(str) ? defaultStr : str;
    }

    /**
     * Strips whitespace from the start and end of a String returning null if the String is empty ("") after the strip.
     *
     * @param str the String to be stripped, may be null
     * @return the stripped String, null if whitespace, empty or null String input
     */
    public static @Nullable String stripToNull(@Nullable String str) {
        if (str == null) {
            return null;
        }
        String s = str.trim();
        return (s.isEmpty()) ? null : s;
    }

    /**
     * Capitalizes a String changing the first letter to title case as per {@link Character#toTitleCase(char)}. No other
     * letters are changed.
     *
     * @param str the String to capitalize, may be null
     * @return the capitalized String, null if null String input
     */
    public static @Nullable String capitalize(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Get the contents of an <code>InputStream</code> as a String using the default character encoding of the platform.
     * This method buffers the input internally, so there is no need to use a <code>BufferedInputStream</code>.
     *
     * @param input the <code>InputStream</code> to read from
     * @return the requested String
     * @throws NullPointerException if the input is null
     * @throws IOException if an I/O error occurs
     */
    public static String streamToString(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);

        final StringWriter writer = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];

        int n = 0;
        while (EOF != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
        }

        return writer.toString();
    }

}
