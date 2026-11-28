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
package org.openhab.binding.knx.internal.projectstore;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.project.KnxDatapointType;
import org.openhab.binding.knx.internal.project.KnxGroupAddress;
import org.openhab.binding.knx.internal.project.KnxGroupAddressStyle;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectInfo;
import org.openhab.binding.knx.internal.project.KnxTopology;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.FileState;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.StoreFailedException;
import org.openhab.core.thing.ThingUID;

import io.calimero.GroupAddress;

/**
 * Checks the rules of the stored file: what a damaged, hostile or newer file gives, what is skipped, and the
 * limits applied when writing.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class StoredFileTest {

    private static final ThingUID BRIDGE = KnxProjectStoreTest.BRIDGE;
    private static final String MARKER = "MARKER-7777";

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T12:34:56Z"), ZoneOffset.UTC);

    @TempDir
    @Nullable
    Path tempFolder;

    private Path folder() {
        return Objects.requireNonNull(tempFolder);
    }

    private Path storedFile() {
        return folder().resolve(KnxProjectStoreTest.FILE_NAME);
    }

    private String validText() throws Exception {
        new KnxProjectStore(folder(), clock).importProject(BRIDGE, ProjectTestFiles.plain(), null, null,
                "made-up.knxproj");
        return Files.readString(storedFile(), StandardCharsets.UTF_8);
    }

    private static String replaceOnce(String text, String old, String replacement) {
        assertTrue(text.contains(old), () -> "the file holds " + old);
        return text.replaceFirst(java.util.regex.Pattern.quote(old),
                java.util.regex.Matcher.quoteReplacement(replacement));
    }

    private void assertUnusable(FileState expected, byte[] content) throws Exception {
        Files.write(storedFile(), content);
        try (LogCapture log = new LogCapture()) {
            KnxProjectStore store = new KnxProjectStore(folder(), clock);

            assertEquals(expected, store.fileState(BRIDGE));
            assertNull(store.get(BRIDGE));
            assertNull(store.lookup(BRIDGE).groupAddressName(new GroupAddress(31, 7, 2)));
            assertNull(store.lookup(BRIDGE).deviceName(new io.calimero.IndividualAddress(7, 7, 1)));
            assertEquals(expected, store.fileState(BRIDGE));
            assertNull(store.get(BRIDGE));

            List<String> warnings = log.warnings();
            assertEquals(1, warnings.size(), warnings::toString);
            String warning = warnings.get(0);
            assertTrue(warning.startsWith("The stored ETS project of knx:ip:made-up cannot be used (" + expected),
                    warning);
            assertTrue(warning.endsWith("Import it again with: openhab:knx project import knx:ip:made-up <file>"),
                    warning);
            assertFalse(warning.contains(MARKER), warning);
            assertFalse(warning.contains("Made-up"), warning);
        }
        assertArrayEquals(content, Files.readAllBytes(storedFile()), "the file is left as it is");
    }

    private void assertUnreadable(UnaryOperator<String> damage) throws Exception {
        assertUnusable(FileState.UNREADABLE, damage.apply(validText()).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void aHigherVersionIsANewerVersion() throws Exception {
        assertUnusable(FileState.NEWER_VERSION,
                replaceOnce(validText(), "{\"version\":1,", "{\"version\":2,\"" + MARKER + "\":{")
                        .getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void aCutOffFileIsUnreadable() throws Exception {
        assertUnreadable(text -> text.substring(0, text.length() / 2));
        assertUnreadable(text -> text.substring(0, text.length() - 1));
    }

    @Test
    void anEmptyFileIsUnreadable() throws Exception {
        assertUnusable(FileState.UNREADABLE, new byte[0]);
    }

    @Test
    void aValueOfTheWrongKindIsUnreadable() throws Exception {
        assertUnreadable(text -> replaceOnce(text, "\"rawAddress\":65281", "\"rawAddress\":\"" + MARKER + "\""));
        assertUnreadable(text -> replaceOnce(text, "\"rawAddress\":65281", "\"rawAddress\":65281.5"));
        assertUnreadable(text -> replaceOnce(text, "\"secured\":true", "\"secured\":\"" + MARKER + "\""));
        assertUnreadable(text -> replaceOnce(text, "\"name\":\"Made-up light\"", "\"name\":null"));
        assertUnreadable(text -> replaceOnce(text, "\"groupRanges\":[", "\"groupRanges\":{\"" + MARKER + "\":["));
    }

    @Test
    void anUnknownConstantIsUnreadable() throws Exception {
        assertUnreadable(text -> replaceOnce(text, "\"THREE_LEVEL\"", "\"" + MARKER + "\""));
        assertUnreadable(text -> replaceOnce(text, "\"definition\":\"FOUND\"", "\"definition\":\"" + MARKER + "\""));
    }

    @Test
    void aMissingNeededFieldIsUnreadable() throws Exception {
        assertUnreadable(text -> replaceOnce(text, "\"installationName\":\"Made-up installation\",", ""));
        assertUnreadable(text -> replaceOnce(text, "\"rawAddress\":65281,", ""));
    }

    @Test
    void aFileWhoseFirstFieldIsNotTheVersionIsUnreadable() throws Exception {
        assertUnreadable(text -> replaceOnce(text, "{\"version\":1,\"bridge\":\"knx:ip:made-up\",",
                "{\"bridge\":\"knx:ip:made-up\",\"version\":1,"));
    }

    @Test
    void aFileOfAnotherBridgeIsUnreadable() throws Exception {
        assertUnreadable(text -> replaceOnce(text, "\"bridge\":\"knx:ip:made-up\"", "\"bridge\":\"knx:ip:other\""));
    }

    @Test
    void textAfterTheFileIsUnreadable() throws Exception {
        assertUnreadable(text -> text + "{\"" + MARKER + "\":1}");
    }

    @Test
    void bytesThatAreNotUtf8AreUnreadable() throws Exception {
        byte[] valid = validText().getBytes(StandardCharsets.UTF_8);
        byte[] damaged = valid.clone();
        int at = new String(valid, StandardCharsets.ISO_8859_1).indexOf("Made-up temperature");
        damaged[at] = (byte) 0xC3;
        damaged[at + 1] = (byte) 0x28;
        assertUnusable(FileState.UNREADABLE, damaged);
    }

    @Test
    void rangesNested64DeepAreReadAnd65AreUnreadable() throws Exception {
        new KnxProjectStore(folder(), clock).importProject(BRIDGE, ProjectTestFiles.nestedRanges(64), null, null);
        String text = Files.readString(storedFile(), StandardCharsets.UTF_8);
        assertEquals(FileState.LOADED, new KnxProjectStore(folder(), clock).fileState(BRIDGE));

        assertUnusable(FileState.UNREADABLE, replaceOnce(text, "\"ranges\":[]",
                "\"ranges\":[{\"id\":\"made-up\",\"name\":\"" + MARKER + "\",\"ranges\":[],\"groupAddresses\":[]}]")
                .getBytes(StandardCharsets.UTF_8));
    }

    private static String spaces(int depth) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            text.append("{\"id\":\"made-up-").append(i).append("\",\"name\":\"").append(MARKER)
                    .append("\",\"type\":\"Room\",\"spaces\":[");
        }
        text.append("]");
        text.append(",\"deviceIds\":[],\"functions\":[]}]".repeat(depth));
        return text.substring(0, text.length() - 1);
    }

    @Test
    void spacesNested64DeepAreReadAnd65AreUnreadable() throws Exception {
        String text = validText();
        String kitchen = "\"name\":\"Made-up kitchen\",\"type\":\"Room\",\"spaces\":[";
        Files.writeString(storedFile(), replaceOnce(text, kitchen, kitchen + spaces(62)), StandardCharsets.UTF_8);
        assertEquals(FileState.LOADED, new KnxProjectStore(folder(), clock).fileState(BRIDGE));

        assertUnusable(FileState.UNREADABLE,
                replaceOnce(text, kitchen, kitchen + spaces(63)).getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void aFileAboveTheLimitIsNotOpened() throws Exception {
        String text = validText();
        int size = text.getBytes(StandardCharsets.UTF_8).length;
        assertEquals(64L * 1024 * 1024, KnxProjectStore.MAX_FILE_BYTES);

        assertEquals(FileState.LOADED,
                new KnxProjectStore(folder(), clock, (from, to) -> fail("no write"), size).fileState(BRIDGE));
        try (LogCapture log = new LogCapture()) {
            KnxProjectStore small = new KnxProjectStore(folder(), clock, (from, to) -> fail("no write"), size - 1);
            assertEquals(FileState.UNREADABLE, small.fileState(BRIDGE));
            assertEquals(
                    List.of("The stored ETS project of knx:ip:made-up cannot be used (UNREADABLE, -). "
                            + "Import it again with: openhab:knx project import knx:ip:made-up <file>"),
                    log.warnings());
        }
    }

    @Test
    void unknownFieldsAreSkippedAtEveryLevel() throws Exception {
        String text = validText();
        StoredProject expected = Objects.requireNonNull(new KnxProjectStore(folder(), clock).get(BRIDGE));
        String extra = "\"" + MARKER + "\":{\"a\":[1,-2.5e3,true,false,null,\"x\\\"y\",{\"b\":[]}]},";
        String extended = text.replace("{\"", "{" + extra + "\"").replace("{" + extra + "\"version\"",
                "{\"version\":1," + extra + "\"version_\"");
        extended = replaceOnce(extended, "\"version_\":1,", "");
        assertTrue(extended.indexOf(MARKER) != extended.lastIndexOf(MARKER));
        Files.writeString(storedFile(), extended, StandardCharsets.UTF_8);

        KnxProjectStore store = new KnxProjectStore(folder(), clock);

        assertEquals(FileState.LOADED, store.fileState(BRIDGE));
        assertEquals(expected, store.get(BRIDGE));
    }

    @Test
    void rangesNestedDeeperThanTheFileAllowsAreNotStored() throws Exception {
        KnxProjectStore store = new KnxProjectStore(folder(), clock);
        StoredProject old = store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        byte[] oldBytes = Files.readAllBytes(storedFile());

        assertThrows(StoreFailedException.class,
                () -> store.importProject(BRIDGE, ProjectTestFiles.nestedRanges(65), null, null));

        assertEquals(old, store.get(BRIDGE));
        assertArrayEquals(oldBytes, Files.readAllBytes(storedFile()));
        assertEquals(List.of(), temporaryFiles());
    }

    @Test
    void aResultLargerThanTheFileAllowsIsNotStored() throws Exception {
        int size = validText().getBytes(StandardCharsets.UTF_8).length;
        Files.delete(storedFile());
        KnxProjectStore store = new KnxProjectStore(folder(), clock, (from, to) -> fail("no move"), size - 1);

        assertThrows(StoreFailedException.class,
                () -> store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null, "made-up.knxproj"));

        assertEquals(FileState.NONE, store.fileState(BRIDGE));
        assertEquals(List.of(), temporaryFiles());
    }

    private List<Path> temporaryFiles() throws IOException {
        try (Stream<Path> files = Files.list(folder())) {
            return files.filter(file -> !file.equals(storedFile())).toList();
        }
    }

    @Test
    void textsWithCharactersJsonMustEscapeComeBackTheSame() throws Exception {
        StringBuilder chars = new StringBuilder("q\"b\\s/t\tn\nr\re");
        for (int c : new int[] { 0x1b, 0x00, 0x2028, 0x2029, 0x7f, 0xe9, 0xd83d, 0xde00, 0x68, 0xd800, 0x6c, 0xdc00 }) {
            chars.append((char) c);
        }
        String hard = chars.append("end").toString();
        KnxGroupAddress address = new KnxGroupAddress("id-" + hard, 65281, "31/7/1", hard, hard,
                List.of(new KnxDatapointType(9, 1), new KnxDatapointType(1, null)), true, List.of(hard, ""));
        KnxProject project = new KnxProject(
                new KnxProjectInfo("P-1", hard, KnxGroupAddressStyle.FREE, null, null, 23, null), hard, List.of(),
                List.of(address), new KnxTopology(List.of(), List.of()), List.of(), 3);
        StoredProject stored = new StoredProject(project, Instant.parse("2026-11-14T01:02:03.456789Z"), hard, hard);
        StringWriter out = new StringWriter();

        ProjectFile.write(out, BRIDGE, stored);

        assertFalse(out.toString().contains("\n"), "not pretty-printed");
        assertTrue(out.toString().startsWith("{\"version\":1,\"bridge\":\"knx:ip:made-up\",\"importedAt\":"));
        assertEquals(stored, ProjectFile.read(new StringReader(out.toString()), BRIDGE, Long.MAX_VALUE));
    }
}
