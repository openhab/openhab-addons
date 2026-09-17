/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link FileReader} reads from file into String
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class FileReader {

    public static String readFileInString(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "CP1252"));) {
            StringBuffer buf = new StringBuffer();
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                buf.append(sCurrentLine);
            }
            return buf.toString();
        } catch (IOException e) {
            // fail if file cannot be read
            fail();
        }
        return "ERR";
    }

    /**
     * Reads a file into a String while preserving its original line breaks. Unlike
     * {@link #readFileInString(String)} - which concatenates lines with no separator, harmless for JSON since
     * every token is delimited by braces/quotes/commas - this is required for protobuf TextFormat fixtures
     * ({@code .raw} files under {@code src/test/resources/vehiclestatusupdates}), where two adjacent lines
     * concatenated without a newline (e.g. {@code full_update: true} + {@code auxheatwarnings {}) would merge
     * into a single bogus token and fail to parse.
     */
    public static String readRawFileInString(String filename) {
        try {
            return Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // fail if file cannot be read
            fail();
        }
        return "ERR";
    }
}
