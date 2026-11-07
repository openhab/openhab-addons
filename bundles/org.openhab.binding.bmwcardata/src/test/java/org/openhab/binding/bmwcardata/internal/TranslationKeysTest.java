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
package org.openhab.binding.bmwcardata.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Verifies that all translation keys referenced by the code exist in the default translation file. These keys are not
 * generated from the XML definitions and get lost when the file is regenerated from scratch.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class TranslationKeysTest {

    private static final Path SOURCES = Path.of("src/main/java");
    private static final Path TRANSLATIONS = Path.of("src/main/resources/OH-INF/i18n/bmwcardata.properties");
    private static final Pattern TEXT_KEY = Pattern.compile("@text/([A-Za-z0-9_.-]+)");

    @Test
    void referencedKeysAreTranslated() throws IOException {
        Properties translations = new Properties();
        try (Reader reader = Files.newBufferedReader(TRANSLATIONS, StandardCharsets.UTF_8)) {
            translations.load(reader);
        }

        Set<String> referenced = new TreeSet<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
                Matcher matcher = TEXT_KEY.matcher(Files.readString(file, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    referenced.add(matcher.group(1));
                }
            }
        }

        assertFalse(referenced.isEmpty());
        Set<String> missing = new TreeSet<>(referenced);
        missing.removeAll(translations.stringPropertyNames());
        assertTrue(missing.isEmpty(), "Missing translations: " + missing);
    }
}
