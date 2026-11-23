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
package org.openhab.binding.tibber.internal.pulse;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests the scenarios "Status description keys", "No key collision", "Keys of the old binding id gone", and "No locale
 * files" of the {@code documentation} spec. The default translation file is scanned line by line: neither the
 * generator nor {@link java.util.Properties} reports a key that occurs twice.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class DefaultTranslationsTest {

    private static final String FILE = "/OH-INF/i18n/tibber.properties";
    private static final String STATUS_SECTION = "# thing status descriptions";
    private static final String CLOUD_STATUS_SECTION = "# things status details";
    // The id of the test version, assembled so that no source file contains it as one word.
    private static final String TEST_VERSION_ID = "tibber" + "pulselocal";

    private final List<String> keysInOrder = new ArrayList<>();
    private final Map<String, String> texts = new LinkedHashMap<>();
    private final Map<String, String> sectionOfKey = new LinkedHashMap<>();
    private final List<String> sections = new ArrayList<>();

    @BeforeEach
    void load() throws IOException {
        try (InputStream in = DefaultTranslationsTest.class.getResourceAsStream(FILE)) {
            assertNotNull(in);
            String section = "";
            for (String line : new String(in.readAllBytes(), StandardCharsets.UTF_8).split("\n")) {
                if (line.startsWith("#")) {
                    section = line.trim();
                    sections.add(section);
                } else if (line.contains("=")) {
                    String key = line.substring(0, line.indexOf('=')).trim();
                    keysInOrder.add(key);
                    texts.put(key, line.substring(line.indexOf('=') + 1).trim());
                    sectionOfKey.put(key, section);
                }
            }
        }
    }

    private List<String> keysOfSection(String section) {
        return keysInOrder.stream().filter(k -> section.equals(sectionOfKey.get(k))).toList();
    }

    private static List<String> statusTextKeys() {
        return Stream
                .of(TEXT_INVALID_HOSTNAME, TEXT_EMPTY_PASSWORD, TEXT_INVALID_DEVICE_ID, TEXT_INVALID_STALE_TIMEOUT,
                        TEXT_NO_BRIDGE, TEXT_PASSWORD_REJECTED, TEXT_NO_STREAM, TEXT_HTTP_STATUS, TEXT_UNREACHABLE,
                        TEXT_CLOSED_BY_BRIDGE, TEXT_SILENT, TEXT_INVALID_DATA, TEXT_NO_READING)
                .map(t -> t.substring("@text/".length())).toList();
    }

    // documentation: Status description keys

    @Test
    void whenTheStatusTextKeysOfThePulsePartAreLookedUpThenEachExistsInTheSectionNextToTheCloudStatusSection() {
        List<String> keys = statusTextKeys();

        assertEquals(13, keys.size());
        assertEquals(Set.copyOf(keys), Set.copyOf(keysOfSection(STATUS_SECTION)));
        for (String key : keys) {
            assertFalse(texts.getOrDefault(key, "").isBlank(), key);
        }
        assertEquals(sections.indexOf(CLOUD_STATUS_SECTION) + 1, sections.indexOf(STATUS_SECTION));
    }

    @Test
    void whenAnyTextKeyOfTheJavaCodeIsLookedUpThenItExistsInTheDefaultFile() throws IOException {
        Pattern textKey = Pattern.compile("@text/([A-Za-z0-9._-]+)");
        Set<String> missing = new TreeSet<>();
        int used = 0;
        try (Stream<Path> files = Files.walk(Path.of("src/main/java"))) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher matcher = textKey.matcher(Files.readString(file));
                while (matcher.find()) {
                    used++;
                    if (!texts.containsKey(matcher.group(1))) {
                        missing.add(matcher.group(1));
                    }
                }
            }
        }

        assertTrue(used >= 13, "the scan found only " + used + " keys");
        assertEquals(Set.of(), missing);
    }

    // documentation: No key collision

    @Test
    void whenAllKeysAreListedThenNoKeyOccursTwice() {
        Set<String> seen = new HashSet<>();
        List<String> duplicates = keysInOrder.stream().filter(k -> !seen.add(k)).toList();

        assertEquals(List.of(), duplicates);
        assertTrue(keysInOrder.size() > 100, "the file was not read completely");
    }

    @Test
    void whenTheStatusDescriptionKeysAreComparedWithTheOtherKeysThenTheyAreDisjointFromStatusActionAndGeneratedKeys() {
        Set<String> own = Set.copyOf(keysOfSection(STATUS_SECTION));

        for (String key : keysInOrder) {
            if (own.contains(key)) {
                assertTrue(key.startsWith("offline."), key);
            } else {
                assertFalse(key.startsWith("offline."), key);
            }
        }
        assertTrue(own.stream().noneMatch(k -> k.startsWith("status.") || k.startsWith("action")));
        assertEquals(own.size(), keysOfSection(STATUS_SECTION).size());
    }

    @Test
    void whenTheStatusKeysOfTheCloudPartAreListedThenTheyAreStillThere() {
        assertEquals(List.of("status.configuration-error", "status.initial-call-failed", "status.price-update-failed",
                "status.history-update"), keysOfSection(CLOUD_STATUS_SECTION));
        assertEquals("Either token or homeId missing", texts.get("status.configuration-error"));
        assertEquals("Tibber API connector", texts.get("thing-type.tibber.tibberapi.description"));
        assertEquals("Total price including taxes", texts.get("channel-type.tibber.price.description"));
    }

    // documentation: Keys of the old binding id gone

    @Test
    void whenTheKeysAreSearchedForTheIdOfTheTestVersionThenThereIsNoMatchAndThePulseTypesUseTheTibberId() {
        for (String key : keysInOrder) {
            assertFalse(key.startsWith(TEST_VERSION_ID) || key.contains("." + TEST_VERSION_ID + "."), key);
        }
        assertTrue(texts.keySet().stream().noneMatch(k -> k.contains(TEST_VERSION_ID)));
        for (String key : List.of("thing-type.tibber.pulse-bridge.label", "thing-type.tibber.pulse-meter.label",
                "thing-type.config.tibber.pulse-bridge.hostname.label",
                "thing-type.config.tibber.pulse-meter.deviceId.label",
                "thing-type.config.tibber.pulse-meter.staleTimeout.label", "channel-type.tibber.pulse-power.label",
                "channel-type.tibber.pulse-energy-import.label", "channel-type.tibber.pulse-last-update.label")) {
            assertTrue(texts.containsKey(key), key);
        }
        // The cloud channel type keeps its own keys.
        assertEquals("Power Consumption/Production", texts.get("channel-type.tibber.power.label"));
    }

    // documentation: No locale files

    @Test
    void whenTheI18nFolderIsListedThenOnlyTheDefaultFileExists() throws IOException {
        try (Stream<Path> files = Files.list(Path.of("src/main/resources/OH-INF/i18n"))) {
            assertEquals(List.of("tibber.properties"), files.map(p -> p.getFileName().toString()).sorted().toList());
        }
    }
}
