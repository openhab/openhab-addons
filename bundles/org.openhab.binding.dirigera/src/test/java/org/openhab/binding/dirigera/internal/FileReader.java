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
package org.openhab.binding.dirigera.internal;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.interfaces.ResourceProvider;

/**
 * {@link FileReader} reads from file into String
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class FileReader implements ResourceProvider {

    public static String readFileInString(String fileName) {
        try {
            return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        fail("Should not reach this point!");
        return "";
    }

    @Override
    public String getResourceFile(String resourcePath) {
        return readFileInString("src/main/resources" + resourcePath);
    }

    @Override
    public String getResourceFileUncompressed(String resourcePath) {
        return readFileInString("src/main/resources" + resourcePath);
    }
}
