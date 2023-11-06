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
package org.openhab.binding.ihc.internal.ws;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.stream.Collectors;

/**
 * Util class to load file content from resource files.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class ResourceFileUtils {
    public static String getFileContent(String resourceFile) {
        try (InputStream inputStream = ResourceFileUtils.class.getClassLoader().getResourceAsStream(resourceFile);
                Reader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            fail("IOException reading xml file '" + resourceFile + "': " + e);
        }
        return "";
    }
}
