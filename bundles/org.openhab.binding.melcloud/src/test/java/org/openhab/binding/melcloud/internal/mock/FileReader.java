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
package org.openhab.binding.melcloud.internal.mock;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * {@link FileReader} reads a file (e.g. a captured JSON API response fixture) into a {@link String}, relative to
 * the module's working directory (i.e. paths like {@code "src/test/ata.json"} are read as-is, not resolved via the
 * test classpath).
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class FileReader {

    public static String readFileInString(String fileName) {
        try {
            return Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            fail(e.getMessage());
        }
        fail("Should not reach this point!");
        return "";
    }
}
