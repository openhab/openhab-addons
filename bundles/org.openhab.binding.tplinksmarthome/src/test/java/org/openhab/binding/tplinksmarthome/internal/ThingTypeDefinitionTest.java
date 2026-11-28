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
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Checks the Thing type definitions in OH-INF/thing.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class ThingTypeDefinitionTest {

    private static final Path THING_DIR = Path.of("src/main/resources/OH-INF/thing");

    /**
     * openHAB doesn't initialize a Thing without a bridge when its type supports a bridge, so supporting one would
     * stop every existing Thing that has none.
     */
    @Test
    public void thingTypesSupportNoBridge() throws IOException {
        final List<Path> files;
        try (Stream<Path> paths = Files.list(THING_DIR)) {
            files = paths.filter(p -> p.toString().endsWith(".xml")).toList();
        }
        assertFalse(files.isEmpty());
        for (final Path file : files) {
            assertFalse(Files.readString(file).contains("supported-bridge-type-refs"), file.toString());
        }
    }
}
