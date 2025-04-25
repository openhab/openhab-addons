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
package org.openhab.binding.mybmw.internal.util;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.utils.Constants;

/**
 * The {@link FileReader} Helper Util to read test resource files
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - added reading of image
 */
@NonNullByDefault
public class FileReader {

    /**
     * reads a file into a string
     *
     * @param filename
     * @return
     */
    public static String fileToString(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(FileReader.class.getClassLoader()).getResourceAsStream(filename), "UTF-8"))) {
            StringBuilder buf = new StringBuilder();
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                buf.append(sCurrentLine);
            }
            return buf.toString();
        } catch (IOException e) {
            fail("Read failure " + filename, e);
        }
        return Constants.UNDEF;
    }

    /**
     * reads a file into a byte[]
     *
     * @param filename
     * @return
     */
    public static byte[] fileToByteArray(String filename) {
        File file = new File(filename);
        byte[] bytes = new byte[(int) file.length()];

        try (InputStream is = (Objects.requireNonNull(FileReader.class.getClassLoader())
                .getResourceAsStream(filename))) {
            Objects.requireNonNull(is).read(bytes);
        } catch (IOException e) {
            fail("Read failure " + filename, e);
        }

        return bytes;
    }
}
