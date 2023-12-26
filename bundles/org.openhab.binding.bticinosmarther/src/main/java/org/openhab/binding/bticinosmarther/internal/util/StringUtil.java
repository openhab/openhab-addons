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
package org.openhab.binding.bticinosmarther.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@code StringUtil} class defines common string utility functions used across the whole binding.
 *
 * @author Fabio Possieri - Initial contribution
 */
@NonNullByDefault
public final class StringUtil {

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    /**
     * Checks if a string is whitespace, empty ("") or {@code null}.
     *
     * @param str
     *            the string to check, may be {@code null}
     *
     * @return {@code true} if the string is {@code null}, empty or whitespace
     */
    public static boolean isBlank(@Nullable String str) {
        return (str == null || str.trim().isEmpty());
    }

    /**
     * Returns either the passed in string or, if the string is {@code null}, an empty string ("").
     *
     * @param str
     *            the string to check, may be {@code null}
     *
     * @return the passed in string, or the empty string if it was {@code null}
     *
     */
    public static String defaultString(@Nullable String str) {
        return (str == null) ? "" : str;
    }

    /**
     * Returns either the passed in string or, if the string is whitespace, empty ("") or {@code null}, a default value.
     *
     * @param str
     *            the string to check, may be {@code null}
     * @param defaultStr
     *            the default string to return
     *
     * @return the passed in string, or the default one
     */
    public static String defaultIfBlank(String str, String defaultStr) {
        return StringUtil.isBlank(str) ? defaultStr : str;
    }

    /**
     * Strips whitespace from the start and end of a string returning {@code null} if the string is empty ("") after the
     * strip.
     *
     * @param str
     *            the string to be stripped, may be {@code null}
     *
     * @return the stripped string, {@code null} if whitespace, empty or {@code null} input string
     */
    public static @Nullable String stripToNull(@Nullable String str) {
        if (str == null) {
            return null;
        }
        String s = str.trim();
        return (s.isEmpty()) ? null : s;
    }

    /**
     * Get the contents of an {@link InputStream} stream as a string using the default character encoding of the
     * platform. This method buffers the input internally, so there is no need to use a {@code BufferedInputStream}.
     *
     * @param input
     *            the {@code InputStream} to read from
     *
     * @return the string read from stream
     *
     * @throws IOException if an I/O error occurs
     */
    public static String streamToString(InputStream input) throws IOException {
        InputStreamReader reader = new InputStreamReader(input);

        final StringWriter writer = new StringWriter();
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];

        int n = 0;
        while ((n = reader.read(buffer)) != EOF) {
            writer.write(buffer, 0, n);
        }

        return writer.toString();
    }

    /**
     * Get the contents of a {@link Reader} stream as a string using the default character encoding of the platform.
     * This method doesn't buffer the input internally, so eventually {@code BufferedReder} needs to be used externally.
     *
     * @param reader
     *            the {@code Reader} to read from
     *
     * @return the string read from stream
     *
     * @throws IOException if an I/O error occurs
     */
    public static String readerToString(Reader reader) throws IOException {
        final StringWriter writer = new StringWriter();

        int c;
        while ((c = reader.read()) != EOF) {
            writer.write(c);
        }

        return writer.toString();
    }
}
