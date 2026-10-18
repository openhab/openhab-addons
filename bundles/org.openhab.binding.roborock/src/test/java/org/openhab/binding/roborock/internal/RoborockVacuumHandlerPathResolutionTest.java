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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@NonNullByDefault({})
class RoborockVacuumHandlerPathResolutionTest {

    @Test
    void resolveRrMapDownloadDirectoryUsesDefaultWhenInputIsNull() {
        Path defaultDirectory = Paths.get("default-home");

        Path resolved = RoborockVacuumHandler.resolveRrMapDownloadDirectory(null, defaultDirectory);

        assertEquals(defaultDirectory, resolved);
    }

    @Test
    void resolveRrMapDownloadDirectoryUsesDefaultWhenInputIsBlank() {
        Path defaultDirectory = Paths.get("default-home");

        Path resolved = RoborockVacuumHandler.resolveRrMapDownloadDirectory("   ", defaultDirectory);

        assertEquals(defaultDirectory, resolved);
    }

    @Test
    void resolveRrMapDownloadDirectoryUsesProvidedDirectoryWhenValid() {
        Path defaultDirectory = Paths.get("default-home");

        Path resolved = RoborockVacuumHandler.resolveRrMapDownloadDirectory("custom/maps", defaultDirectory);

        assertEquals(Paths.get("custom/maps"), resolved);
    }

    @Test
    void resolveRrMapDownloadDirectoryUsesDefaultWhenInputIsInvalid() {
        Path defaultDirectory = Paths.get("default-home");

        String invalidPathInput = "\u0000invalid";
        Path resolved = RoborockVacuumHandler.resolveRrMapDownloadDirectory(invalidPathInput, defaultDirectory);

        assertEquals(defaultDirectory, resolved);
    }

    @Test
    void resolveRrMapDownloadDirectoryUsesDefaultWhenPathPointsToFile(@TempDir Path tempDir) throws IOException {
        Path defaultDirectory = Paths.get("default-home");
        Path existingFile = Files.createFile(tempDir.resolve("map.rrmap"));

        Path resolved = RoborockVacuumHandler.resolveRrMapDownloadDirectory(existingFile.toString(), defaultDirectory);

        assertEquals(defaultDirectory, resolved);
    }
}
