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
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.project.KnxDevice;
import org.openhab.binding.knx.internal.project.KnxIndividualAddress;
import org.openhab.binding.knx.internal.project.KnxProject;
import org.openhab.binding.knx.internal.project.KnxProjectException;
import org.openhab.binding.knx.internal.project.KnxProjectReader;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.FileState;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore.StoreFailedException;
import org.openhab.core.thing.ThingUID;

/**
 * Checks how the store keeps, replaces, loads and removes the project of a bridge.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KnxProjectStoreTest {

    static final ThingUID BRIDGE = new ThingUID("knx:ip:made-up");
    static final String FILE_NAME = "project-knx_ip_made-up.json";

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-14T12:34:56.789Z"), ZoneOffset.UTC);

    @TempDir
    @Nullable
    Path tempFolder;

    Path folder() {
        return Objects.requireNonNull(tempFolder);
    }

    KnxProjectStore store() {
        return new KnxProjectStore(folder(), clock);
    }

    Path storedFile() {
        return folder().resolve(FILE_NAME);
    }

    String storedText() throws IOException {
        return Files.readString(storedFile(), StandardCharsets.UTF_8);
    }

    static KnxDevice device(KnxProject project, int device) {
        return Objects.requireNonNull(project.device(new KnxIndividualAddress(7, 7, device)));
    }

    private List<Path> temporaryFiles() throws IOException {
        try (Stream<Path> files = Files.list(folder())) {
            return files.filter(file -> String.valueOf(file.getFileName()).endsWith(".tmp")).toList();
        }
    }

    @Test
    void aSecondStoreOnTheSameFolderGivesAnEqualProject() throws Exception {
        StoredProject imported = store().importProject(BRIDGE, ProjectTestFiles.plain(), null, "de-DE");

        KnxProjectStore restarted = store();

        assertEquals(FileState.LOADED, restarted.fileState(BRIDGE));
        assertEquals(imported, restarted.get(BRIDGE));
        assertEquals(clock.instant(), imported.importedAt());
        assertEquals("de-DE", imported.language());
        assertEquals(ProjectTestFiles.PROJECT_NAME, imported.project().info().name());
    }

    @Test
    void aProtectedProjectIsKeptTheSameWay() throws Exception {
        StoredProject imported = store().importProject(BRIDGE, ProjectTestFiles.protectedProject(),
                ProjectTestFiles.PASSWORD.toCharArray(), null);

        assertEquals(imported, store().get(BRIDGE));
        assertNull(imported.language());
        assertFalse(storedText().contains("\"language\""), "a null component is left out");
    }

    @Test
    void parameterValuesAndModuleInstancesAreNeitherInMemoryNorInTheFile() throws Exception {
        Path file = ProjectTestFiles.plain();
        KnxDevice read = device(new KnxProjectReader().read(file, null), 1);
        assertFalse(read.parameters().isEmpty(), "the fixture has parameter values");
        assertFalse(read.modules().isEmpty(), "the fixture has a module instance");

        StoredProject imported = store().importProject(BRIDGE, file, null, null);

        KnxDevice kept = device(imported.project(), 1);
        assertEquals(List.of(), kept.parameters());
        assertEquals(List.of(), kept.modules());
        assertEquals(read.objects(), kept.objects());
        assertFalse(storedText().contains("\"parameters\""));
        assertFalse(storedText().contains("\"modules\""));
        assertEquals(List.of(), device(Objects.requireNonNull(store().get(BRIDGE)).project(), 1).parameters());
    }

    @Test
    void theFileHoldsNoSecretNoParameterValueNoPasswordAndNoFolder() throws Exception {
        List<String> madeUp = ProjectTestFiles.madeUpValues();
        assertTrue(madeUp.size() >= 10, "the fixture holds keys, tool keys and passwords");
        Path file = ProjectTestFiles.protectedProject();
        char[] password = ProjectTestFiles.PASSWORD.toCharArray();

        StoredProject imported = store().importProject(BRIDGE, file, password, null);

        String text = storedText();
        assertTrue(text.contains("Made-up temperature"), "the file is the project");
        for (String value : madeUp) {
            assertFalse(text.contains(value), "found a value that must not be kept");
        }
        assertFalse(text.contains(ProjectTestFiles.PARAMETER_VALUE));
        assertFalse(text.contains(ProjectTestFiles.PASSWORD));
        assertFalse(text.contains(String.valueOf(file.getParent())));
        assertEquals(String.valueOf(file.getFileName()), imported.sourceName());
        assertArrayEquals(ProjectTestFiles.PASSWORD.toCharArray(), password, "the caller wipes the array");
    }

    @Test
    void theSourceNameIsKeptWithoutAFolder() throws Exception {
        assertEquals("upload.knxproj", store()
                .importProject(BRIDGE, ProjectTestFiles.plain(), null, null, "/made/up/upload.knxproj").sourceName());
        assertEquals("other.knxproj",
                store().importProject(BRIDGE, ProjectTestFiles.plain(), null, null, "C:\\made\\up\\other.knxproj")
                        .sourceName());
    }

    @Test
    void aFailingWriteKeepsTheOldFileAndProject() throws Exception {
        KnxProjectStore first = store();
        StoredProject old = first.importProject(BRIDGE, ProjectTestFiles.plain("Made-up old"), null, null);
        byte[] oldBytes = Files.readAllBytes(storedFile());
        KnxProjectStore failing = new KnxProjectStore(folder(), clock, (temporary, target) -> {
            assertTrue(Files.size(temporary) > 0, "the temporary file was written");
            throw new IOException("made-up failure /made/up/folder");
        }, KnxProjectStore.MAX_FILE_BYTES);
        assertEquals(old, failing.get(BRIDGE));

        StoreFailedException e = assertThrows(StoreFailedException.class,
                () -> failing.importProject(BRIDGE, ProjectTestFiles.plain("Made-up new"), null, null));

        assertEquals("The project was read, but the result could not be stored.", e.getMessage());
        assertNull(e.getCause());
        assertArrayEquals(oldBytes, Files.readAllBytes(storedFile()));
        assertEquals(old, failing.get(BRIDGE));
        assertEquals(List.of(), temporaryFiles());
    }

    /** Notes the steps of a write and moves as the store does. */
    private static final class RecordingMover implements KnxProjectStore.Mover {
        final List<String> steps = new ArrayList<>();
        long forcedSize = -1;
        boolean failForce;

        @Override
        public void force(FileChannel channel) throws IOException {
            steps.add("force");
            forcedSize = channel.size();
            if (failForce) {
                throw new IOException("made-up failure /made/up/folder");
            }
            KnxProjectStore.Mover.super.force(channel);
        }

        @Override
        public void move(Path temporary, Path target) throws IOException {
            steps.add("move");
            assertEquals(forcedSize, Files.size(temporary), "the forced channel is the one of the temporary file");
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Test
    void theTemporaryFileIsForcedToDiskBeforeItIsMoved() throws Exception {
        RecordingMover mover = new RecordingMover();
        KnxProjectStore forcing = new KnxProjectStore(folder(), clock, mover, KnxProjectStore.MAX_FILE_BYTES);

        forcing.importProject(BRIDGE, ProjectTestFiles.plain("Made-up forced"), null, null);

        assertEquals(List.of("force", "move"), mover.steps);
        assertTrue(mover.forcedSize > 0, "the file was written before the force");
        assertEquals(mover.forcedSize, Files.size(storedFile()));
        assertEquals(List.of(), temporaryFiles());
    }

    @Test
    void aFailingForceKeepsTheOldFileAndProjectAndDeletesTheTemporaryFile() throws Exception {
        StoredProject old = store().importProject(BRIDGE, ProjectTestFiles.plain("Made-up old"), null, null);
        byte[] oldBytes = Files.readAllBytes(storedFile());
        RecordingMover mover = new RecordingMover();
        mover.failForce = true;
        KnxProjectStore failing = new KnxProjectStore(folder(), clock, mover, KnxProjectStore.MAX_FILE_BYTES);

        StoreFailedException e = assertThrows(StoreFailedException.class,
                () -> failing.importProject(BRIDGE, ProjectTestFiles.plain("Made-up new"), null, null));

        assertEquals("The project was read, but the result could not be stored.", e.getMessage());
        assertNull(e.getCause());
        assertEquals(List.of("force"), mover.steps);
        assertArrayEquals(oldBytes, Files.readAllBytes(storedFile()));
        assertEquals(old, failing.get(BRIDGE));
        assertEquals(List.of(), temporaryFiles());
    }

    @Test
    void theStoresOwnForceForcesTheContentAndTheMetadataOfTheChannel() throws Exception {
        FileChannel channel = mock(FileChannel.class);
        KnxProjectStore.Mover mover = (temporary, target) -> {
        };

        mover.force(channel);

        verify(channel).force(true);
        verifyNoMoreInteractions(channel);
    }

    private Path fileChangedAgo(String name, Duration ago) throws IOException {
        Path file = Files.writeString(folder().resolve(name), "made-up");
        Files.setLastModifiedTime(file, FileTime.from(clock.instant().minus(ago)));
        return file;
    }

    @Test
    void aNewStoreDeletesTemporaryFilesOlderThanTenMinutesOnly() throws Exception {
        Path old = fileChangedAgo(".project-1234567890.tmp", Duration.ofMinutes(11));
        Path young = fileChangedAgo(".project-0987654321.tmp", Duration.ofMinutes(9));
        Path otherName = fileChangedAgo(".other-1234567890.tmp", Duration.ofDays(3));
        Path otherEnd = fileChangedAgo(".project-1234567890.json", Duration.ofDays(3));
        Path stored = fileChangedAgo(FILE_NAME, Duration.ofDays(3));

        store();

        assertFalse(Files.exists(old));
        assertTrue(Files.exists(young));
        assertTrue(Files.exists(otherName));
        assertTrue(Files.exists(otherEnd));
        assertTrue(Files.exists(stored));
        assertEquals(List.of(), new KnxProjectStore(folder().resolve("missing"), clock).stored());
    }

    @Test
    void aFolderThatCannotBeCreatedFailsTheStore() throws Exception {
        Path blocked = folder().resolve("blocked");
        Files.writeString(blocked, "made-up");
        KnxProjectStore store = new KnxProjectStore(blocked.resolve("knx"), clock);

        assertThrows(StoreFailedException.class,
                () -> store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null));
        assertEquals(FileState.NONE, store.fileState(BRIDGE));
    }

    @Test
    void aWrongPasswordChangesNothing() throws Exception {
        KnxProjectStore store = store();
        StoredProject old = store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        byte[] oldBytes = Files.readAllBytes(storedFile());

        KnxProjectException e = assertThrows(KnxProjectException.class, () -> store.importProject(BRIDGE,
                ProjectTestFiles.protectedProject(), "made-up wrong".toCharArray(), null));

        assertEquals(KnxProjectException.Reason.WRONG_PASSWORD, e.getReason());
        assertEquals(old, store.get(BRIDGE));
        assertArrayEquals(oldBytes, Files.readAllBytes(storedFile()));
    }

    @Test
    void aFileThatIsNoProjectChangesNothing() throws Exception {
        KnxProjectStore store = store();
        StoredProject old = store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        byte[] oldBytes = Files.readAllBytes(storedFile());

        KnxProjectException e = assertThrows(KnxProjectException.class,
                () -> store.importProject(BRIDGE, ProjectTestFiles.notAProject(), null, null));

        assertEquals(KnxProjectException.Reason.NOT_A_PROJECT, e.getReason());
        assertEquals(old, store.get(BRIDGE));
        assertArrayEquals(oldBytes, Files.readAllBytes(storedFile()));
        assertEquals(List.of(), temporaryFiles());
    }

    @Test
    void aNewImportReplacesTheOldProject() throws Exception {
        KnxProjectStore store = store();
        store.importProject(BRIDGE, ProjectTestFiles.plain("Made-up old"), null, null);

        StoredProject replaced = store.importProject(BRIDGE, ProjectTestFiles.plain("Made-up new"), null, null);

        assertEquals(replaced, store.get(BRIDGE));
        assertEquals(replaced, store().get(BRIDGE));
        assertEquals(List.of(), temporaryFiles());
    }

    @Test
    void removeDeletesTheFileOnce() throws Exception {
        KnxProjectStore store = store();
        store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);

        assertTrue(store.remove(BRIDGE));
        assertFalse(store.remove(BRIDGE));

        assertFalse(Files.exists(storedFile()));
        assertNull(store.get(BRIDGE));
        assertEquals(FileState.NONE, store.fileState(BRIDGE));
        assertEquals(FileState.NONE, store().fileState(BRIDGE));
    }

    @Test
    void removeWorksOnAnUnreadableFile() throws Exception {
        Files.writeString(storedFile(), "made-up damage");
        KnxProjectStore store = store();
        assertEquals(FileState.UNREADABLE, store.fileState(BRIDGE));

        assertTrue(store.remove(BRIDGE));

        assertEquals(FileState.NONE, store.fileState(BRIDGE));
        assertFalse(Files.exists(storedFile()));
    }

    @Test
    void storedListsFilesOfBridgesThatAreGoneAndSkipsOtherNames() throws Exception {
        store().importProject(BRIDGE, ProjectTestFiles.plain(), null, null);
        Files.writeString(folder().resolve("project-knx_serial_gone.json"), "made-up");
        Files.writeString(folder().resolve("project-foo.json"), "made-up");
        Files.writeString(folder().resolve("project-knx_ip_bad!name.json"), "made-up");
        Files.writeString(folder().resolve("project-knx_device_x.json"), "made-up");
        Files.writeString(folder().resolve(".project-123.tmp"), "made-up");

        assertEquals(List.of(BRIDGE, new ThingUID("knx:serial:gone")), store().stored());
    }

    @Test
    void storedIsEmptyWithoutAFolder() {
        assertEquals(List.of(), new KnxProjectStore(folder().resolve("missing"), clock).stored());
    }

    @Test
    void theFolderIsCreatedAtTheFirstWrite() throws Exception {
        Path folder = folder().resolve("knx");
        KnxProjectStore store = new KnxProjectStore(folder, clock);
        assertEquals(FileState.NONE, store.fileState(BRIDGE));
        assertFalse(Files.exists(folder));

        store.importProject(BRIDGE, ProjectTestFiles.plain(), null, null);

        assertTrue(Files.isRegularFile(folder.resolve(FILE_NAME)));
    }

    @Test
    void theLookupIsOneObjectPerBridge() {
        KnxProjectStore store = store();

        assertSame(store.lookup(BRIDGE), store.lookup(new ThingUID("knx:ip:made-up")));
        assertNotSame(store.lookup(BRIDGE), store.lookup(new ThingUID("knx:ip:other")));
    }
}
