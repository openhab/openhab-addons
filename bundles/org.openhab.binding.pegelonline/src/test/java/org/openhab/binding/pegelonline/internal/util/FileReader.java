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
package org.openhab.binding.pegelonline.internal.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.pegelonline.internal.PegelOnlineBindingConstants;

/**
 * The {@link FileReader} Helper Util to read test resource files
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class FileReader {

    public static String readFileInString(String filename) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));) {
            StringBuilder buf = new StringBuilder();
            String sCurrentLine;

            while ((sCurrentLine = br.readLine()) != null) {
                buf.append(sCurrentLine);
            }
            return buf.toString();
        } catch (IOException e) {
            // fail if file cannot be read
            assertTrue(false, e.getMessage());
        }
        return PegelOnlineBindingConstants.UNKNOWN;
    }
}
