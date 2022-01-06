/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for handling test resources.
 *
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public final class ResourceUtil {
    private ResourceUtil() {
    }

    /**
     * Gets the contents of a resource file as {@link String}.
     *
     * @param resourceName The resource name (path inside the resources source folder).
     * @return The file contents.
     * @throws IOException if reading the resource fails or it cannot be found.
     */
    public static String getResourceAsString(String resourceName) throws IOException {
        InputStream inputStream = ResourceUtil.class.getResourceAsStream(resourceName);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
