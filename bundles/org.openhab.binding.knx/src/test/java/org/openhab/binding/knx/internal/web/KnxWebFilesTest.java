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
package org.openhab.binding.knx.internal.web;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.web.KnxWebFiles.Entry;

/**
 * Checks the page's file table against the folder {@code web/}, and that a request's path can only pick an entry of
 * the table by its exact text.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxWebFilesTest {
    static final Path WEB = Path.of("src/main/resources/web");

    private static final Map<String, String> TYPES = Map.of(".html", "text/html; charset=utf-8", ".css",
            "text/css; charset=utf-8", ".js", "text/javascript; charset=utf-8", ".svg", "image/svg+xml");

    private final KnxWebFiles files = new KnxWebFiles(KnxWebFiles.ENTRIES);

    /** {@return the paths of every file in {@code web/}, relative to it, with {@code /} between the parts} */
    static Set<String> folderFiles() throws IOException {
        assertTrue(Files.isDirectory(WEB), WEB.toAbsolutePath().toString());
        try (Stream<Path> walk = Files.walk(WEB)) {
            return walk.filter(Files::isRegularFile).map(file -> WEB.relativize(file).toString().replace('\\', '/'))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    private static Entry entry(String path) {
        return Objects.requireNonNull(
                KnxWebFiles.ENTRIES.stream().filter(entry -> entry.path().equals(path)).findFirst().orElse(null), path);
    }

    @Test
    void theTableAndTheFolderHoldTheSameFiles() throws IOException {
        Set<String> table = KnxWebFiles.ENTRIES.stream().map(Entry::path)
                .collect(Collectors.toCollection(TreeSet::new));

        assertEquals(KnxWebFiles.ENTRIES.size(), table.size(), "a path is in the table twice");
        assertEquals(folderFiles(), table);
        assertTrue(table.contains("index.html"));
    }

    @Test
    void everyEntryIsTheFileOfItsPathInTheJar() {
        for (Entry entry : KnxWebFiles.ENTRIES) {
            assertEquals("web/" + entry.path(), entry.resource());
        }
    }

    @Test
    void everyEntryLoadsAndIsNotEmpty() throws IOException {
        assertEquals(List.of(), files.missing());
        for (Entry entry : KnxWebFiles.ENTRIES) {
            byte[] bytes = files.load(entry);
            assertTrue(bytes.length > 0, entry.path());
            assertArrayEquals(Files.readAllBytes(WEB.resolve(entry.path())), bytes, entry.path());
        }
    }

    @Test
    void theContentTypeFollowsTheEnding() {
        for (Entry entry : KnxWebFiles.ENTRIES) {
            String ending = entry.path().substring(entry.path().lastIndexOf('.'));
            assertEquals(TYPES.get(ending), entry.contentType(), entry.path());
        }
    }

    @Test
    void theEtagIsTheStartOfTheSha256InQuotes() throws NoSuchAlgorithmException {
        for (Entry entry : KnxWebFiles.ENTRIES) {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(files.load(entry));
            String expected = "\"" + HexFormat.of().formatHex(digest).substring(0, 16) + "\"";
            assertEquals(expected, files.etag(entry), entry.path());
        }
    }

    @Test
    void theStartPageAnswersTheSlashAndTheEmptyPath() {
        assertEquals(entry("index.html"), files.find("/"));
        assertEquals(entry("index.html"), files.find(""));
        assertEquals(entry("index.html"), files.find("/index.html"));
    }

    @Test
    void everyEntryIsFoundByItsPath() {
        for (Entry entry : KnxWebFiles.ENTRIES) {
            assertEquals(entry, files.find("/" + entry.path()));
        }
    }

    @Test
    void nothingAndOddPathsFindNothing() {
        assertNull(files.find(null));
        for (String odd : List.of("/..", "/../x", "/js/../index.html", "/%2e%2e/", "//index.html", "/index.html/",
                "/INDEX.HTML", "index.html", "/./index.html", "/index.html%00", "/index.html?x", "/web/index.html",
                "/" + "a".repeat(4999))) {
            assertNull(files.find(odd), odd);
        }
    }

    @Test
    void anEntryWhoseFileIsMissingIsKeptApartAndNeverFound() {
        Entry missing = new Entry("gone.js", "web/gone.js", KnxWebFiles.JAVASCRIPT);
        KnxWebFiles table = new KnxWebFiles(List.of(entry("index.html"), missing));

        assertEquals(List.of(missing), table.missing());
        assertNull(table.find("/gone.js"));
        assertEquals(entry("index.html"), table.find("/"));
        assertThrows(IllegalArgumentException.class, () -> table.load(missing));
    }
}
