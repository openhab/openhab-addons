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
     * Reads a file into a String preserving its original line breaks, unlike {@link #readFileInString(String)},
     * whose line concatenation would merge adjacent protobuf TextFormat fields into one bogus token.
     */
    public static String readRawFileInString(String filename) {
        try {
            return Files.readString(Path.of(filename), StandardCharsets.UTF_8);
        } catch (IOException e) {
            fail();
        }
        return "ERR";
    }
}
