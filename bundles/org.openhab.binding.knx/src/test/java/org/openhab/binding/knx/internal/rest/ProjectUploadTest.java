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
package org.openhab.binding.knx.internal.rest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.LogCapture;
import org.openhab.binding.knx.internal.projectstore.ProjectNames;
import org.openhab.binding.knx.internal.projectstore.StoredProject;
import org.openhab.binding.knx.internal.rest.dto.ProjectDTO;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/**
 * Checks the project upload: each answer, and after every case an empty upload folder, wiped password arrays, the
 * earlier project kept on a failure and no secret in a log line or an error body.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ProjectUploadTest {
    private static final Instant IMPORTED = Instant.parse("2026-11-14T12:34:56Z");
    private static final String OLD_NAME = "Made-up Old";
    private static final String BRIDGE = "knx:ip:home";
    private static final ThingUID UID = RestFixtures.uid("home");

    private @TempDir Path folder = Path.of("");
    private Path uploadFolder = Path.of("");
    private KnxProjectStore store = new KnxProjectStore(Path.of("target", "unused"), Clock.systemUTC());
    private List<Uploads.Password> passwords = new ArrayList<>();
    private List<String> errorTexts = new ArrayList<>();
    private @Nullable LogCapture log;

    @BeforeEach
    void setUp() throws Exception {
        store = new KnxProjectStore(folder.resolve("knx"), Clock.fixed(IMPORTED, ZoneOffset.UTC));
        uploadFolder = folder.resolve("knx").resolve("upload");
        passwords = new ArrayList<>();
        errorTexts = new ArrayList<>();
        store.importProject(UID, ProjectTestFiles.plain(OLD_NAME), null, null, "old.knxproj");
        log = new LogCapture();
    }

    @AfterEach
    void closeLog() {
        LogCapture capture = log;
        if (capture != null) {
            capture.close();
        }
    }

    @FunctionalInterface
    private interface UploadCall {
        void run() throws Exception;
    }

    private ProjectUpload upload(KnxProjectStore projectStore, long maxBytes) {
        KnxRestService service = new KnxRestService(
                RestFixtures.factory(RestFixtures.handler("home", new TelegramMonitor())), projectStore,
                mock(ThingRegistry.class), mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class));
        return new ProjectUpload(service, projectStore, new Uploads(uploadFolder, passwords::add), maxBytes);
    }

    private ProjectUpload upload() {
        return upload(store, ProjectUpload.MAX_BYTES);
    }

    private static InputStream body(Path file) throws IOException {
        return new ByteArrayInputStream(Files.readAllBytes(file));
    }

    private static String header(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
    }

    private void assertFails(KnxRestError error, Executable call) {
        KnxRestException e = assertThrows(KnxRestException.class, call);
        assertEquals(error.code(), e.code());
        assertEquals(error.status(), e.status());
        errorTexts.add(e.text());
        assertOldProjectKept();
    }

    private void assertOldProjectKept() {
        StoredProject stored = store.get(UID);
        assertNotNull(stored);
        assertEquals(OLD_NAME, Objects.requireNonNull(stored).project().info().name());
        assertEquals("old.knxproj", stored.sourceName());
    }

    /** What must hold after every case: nothing left in the upload folder, every password array wiped. */
    private void assertCleanedUp() throws IOException {
        if (Files.exists(uploadFolder)) {
            try (Stream<Path> left = Files.list(uploadFolder)) {
                assertEquals(List.of(), left.toList());
            }
        }
        for (Uploads.Password password : passwords) {
            assertTrue(password.isWiped());
        }
    }

    @Test
    void theRightPasswordImportsTheProtectedProject() throws Exception {
        ProjectDTO answer = upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null,
                header(ProjectTestFiles.PASSWORD), "de-DE", "house.knxproj");

        assertEquals("LOADED", answer.state);
        assertEquals(ProjectTestFiles.PROJECT_NAME, answer.name);
        assertEquals("house.knxproj", answer.sourceName);
        assertEquals("de-DE", answer.language);
        assertEquals(ProjectTestFiles.PROJECT_NAME, Objects.requireNonNull(store.get(UID)).project().info().name());
        assertEquals(1, passwords.size());
        assertCleanedUp();
    }

    @Test
    void theArrayHandedToTheStoreHeldThePasswordAndIsWipedAfterwards() throws Exception {
        KnxProjectStore spy = spy(store);
        ArgumentCaptor<char[]> handed = ArgumentCaptor.forClass(char[].class);

        upload(spy, ProjectUpload.MAX_BYTES).upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null,
                header(ProjectTestFiles.PASSWORD), null, null);

        verify(spy).importProject(eq(UID), any(), handed.capture(), isNull(), eq(ProjectUpload.DEFAULT_NAME));
        char[] array = handed.getValue();
        assertEquals(ProjectTestFiles.PASSWORD.length(), array.length);
        for (char c : array) {
            assertEquals('\0', c);
        }
        assertCleanedUp();
    }

    @Test
    void withoutPasswordAProtectedProjectAsksForOne() throws Exception {
        assertFails(KnxRestError.PASSWORD_REQUIRED,
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null, null, null, null));
        assertFails(KnxRestError.PASSWORD_REQUIRED,
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null, "", null, null));
        assertCleanedUp();
    }

    @Test
    void aWrongPasswordIsRefused() throws Exception {
        assertFails(KnxRestError.WRONG_PASSWORD, () -> upload().upload(BRIDGE,
                body(ProjectTestFiles.protectedProject()), null, header("not the made-up password"), null, null));
        assertFalse(passwords.isEmpty());
        assertCleanedUp();
    }

    @Test
    void aHeaderThatIsNotBase64HasAFixedText() throws Exception {
        KnxRestException e = assertThrows(KnxRestException.class, () -> upload().upload(BRIDGE,
                body(ProjectTestFiles.protectedProject()), null, "made-up-not-base64!", null, null));
        assertEquals("bad-input", e.code());
        assertEquals(Uploads.PASSWORD_TEXT, e.text());
        errorTexts.add(e.text());
        assertOldProjectKept();
        assertCleanedUp();
        for (String line : Objects.requireNonNull(log).all()) {
            assertFalse(line.toLowerCase(Locale.ROOT).contains("base64"), line);
        }
    }

    @Test
    void aHeaderThatIsNotUtf8HasTheSameFixedText() throws Exception {
        String notUtf8 = Base64.getEncoder().encodeToString(new byte[] { 'a', (byte) 0xFF, (byte) 0xFE });
        KnxRestException e = assertThrows(KnxRestException.class,
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null, notUtf8, null, null));
        assertEquals(Uploads.PASSWORD_TEXT, e.text());
        assertEquals(1, passwords.size());
        assertCleanedUp();
    }

    @Test
    void aTextFileIsNotAProject() throws Exception {
        assertFails(KnxRestError.NOT_A_PROJECT,
                () -> upload().upload(BRIDGE,
                        new ByteArrayInputStream("made-up text, no project".getBytes(StandardCharsets.UTF_8)), null,
                        null, null, "notes.txt"));
        assertFails(KnxRestError.NOT_A_PROJECT,
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.notAProject()), null, null, null, null));
        assertCleanedUp();
    }

    @Test
    void aContentLengthAboveTheCapIsRefusedBeforeAByteIsRead() throws Exception {
        InputStream untouchable = new InputStream() {
            @Override
            public int read() {
                throw new AssertionError("the body was read");
            }
        };
        assertFails(KnxRestError.TOO_LARGE, () -> upload().upload(BRIDGE, untouchable,
                String.valueOf(ProjectUpload.MAX_BYTES + 1), null, null, null));
        assertFails(KnxRestError.TOO_LARGE,
                () -> upload().upload(BRIDGE, untouchable, "99999999999999999999999", null, null, null));
        assertFails(KnxRestError.BAD_INPUT, () -> upload().upload(BRIDGE, untouchable, "a lot", null, null, null));
        assertCleanedUp();
    }

    @Test
    void aBodyLongerThanTheCapIsRefusedWhileCopying() throws Exception {
        byte[] large = new byte[5000];
        assertFails(KnxRestError.TOO_LARGE,
                () -> upload(store, 1000).upload(BRIDGE, new ByteArrayInputStream(large), null, null, null, null));
        assertFails(KnxRestError.TOO_LARGE, () -> upload(store, 1000).upload(BRIDGE, new ByteArrayInputStream(large),
                "10", header(ProjectTestFiles.PASSWORD), null, null));
        assertCleanedUp();
    }

    @Test
    void aBodyThatFailsWhileReadingIsAReadFailure() throws Exception {
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("made-up failure at " + uploadFolder);
            }
        };
        assertFails(KnxRestError.READ_FAILED,
                () -> upload().upload(BRIDGE, broken, null, header(ProjectTestFiles.PASSWORD), null, null));
        assertCleanedUp();
    }

    @Test
    void aSecondUploadWhileOneRunsIsBusy() throws Exception {
        ProjectUpload upload = upload();
        List<KnxRestException> inner = new ArrayList<>();
        byte[] project = Files.readAllBytes(ProjectTestFiles.plain());
        InputStream body = new ByteArrayInputStream(project) {
            private boolean asked;

            @Override
            public synchronized int read(byte @Nullable [] b, int off, int len) {
                if (!asked) {
                    asked = true;
                    inner.add(assertThrows(KnxRestException.class,
                            () -> upload.upload(BRIDGE, new ByteArrayInputStream(project), null, null, null, null)));
                }
                return super.read(b, off, len);
            }
        };

        assertEquals("LOADED", upload.upload(BRIDGE, body, null, null, null, null).state);
        assertEquals(1, inner.size());
        assertEquals(KnxRestError.BUSY.code(), inner.get(0).code());
        assertEquals("LOADED", upload.upload(BRIDGE, body(ProjectTestFiles.plain()), null, null, null, null).state);
        assertCleanedUp();
    }

    @Test
    void aFailedStoreWriteKeepsTheEarlierProject() throws Exception {
        Path knx = folder.resolve("knx");
        Path stored = knx.resolve("project-knx_ip_home.json");
        byte[] before = Files.readAllBytes(stored);
        // a folder that cannot be written makes the store's temporary file fail
        Files.createDirectories(uploadFolder);
        assertTrue(knx.toFile().setWritable(false));
        try {
            assertFails(KnxRestError.STORE_FAILED, () -> upload().upload(BRIDGE,
                    body(ProjectTestFiles.protectedProject()), null, header(ProjectTestFiles.PASSWORD), null, null));
        } finally {
            assertTrue(knx.toFile().setWritable(true));
        }
        assertArrayEquals(before, Files.readAllBytes(stored));
        assertCleanedUp();
    }

    @Test
    void anUnknownBridgeOrLanguageIsRefusedBeforeTheBodyIsRead() throws Exception {
        InputStream untouchable = new InputStream() {
            @Override
            public int read() {
                throw new AssertionError("the body was read");
            }
        };
        assertFails(KnxRestError.UNKNOWN_BRIDGE,
                () -> upload().upload("knx:ip:gone", untouchable, null, null, null, null));
        for (String language : List.of("d", "deutsch", "de_DE", "de-", "de-D", "de/../x", "de-DE-" + "x".repeat(40))) {
            assertFails(KnxRestError.BAD_INPUT, () -> upload().upload(BRIDGE, untouchable, null, null, language, null));
        }
        assertEquals("LOADED",
                upload().upload(BRIDGE, body(ProjectTestFiles.plain()), null, null, "zh-Hans-CN", null).state);
        assertCleanedUp();
    }

    @Test
    void theNameIsCutToItsLastPartAndCleaned() {
        assertEquals("x.knxproj", ProjectUpload.cleanName("../../x.knxproj"));
        assertEquals("passwd", ProjectUpload.cleanName("/etc/passwd"));
        assertEquals("y.knxproj", ProjectUpload.cleanName("C:\\made-up\\y.knxproj"));
        assertEquals("x".repeat(100), ProjectUpload.cleanName("x".repeat(300)));
        assertEquals("a_b.knxproj", ProjectUpload.cleanName("a\u0007b.knxproj"));
        assertEquals("a_b.knxproj", ProjectUpload.cleanName("a\u0000b.knxproj"));
        assertEquals("Haus _1_ m_t.knxproj", ProjectUpload.cleanName("Haus (1) m\u00e4t.knxproj"));
        assertEquals("made-up_name.v2.knxproj", ProjectUpload.cleanName("made-up_name.v2.knxproj"));
        for (String empty : List.of("", ".", "..", "a/..", "a\\..", "/", "x/")) {
            assertEquals(ProjectUpload.DEFAULT_NAME, ProjectUpload.cleanName(empty), empty);
        }
        assertEquals(ProjectUpload.DEFAULT_NAME, ProjectUpload.cleanName(null));
    }

    @Test
    void everyNameLandsInsideTheUploadFolderAndBecomesTheSourceName() throws Exception {
        List<String> names = List.of("../../x.knxproj", "/etc/made-up.knxproj", "x".repeat(300), "", ".", "..", "a/..",
                "a\u0007b", "a\u0000b", "..\\..\\y.knxproj");
        for (String name : names) {
            KnxProjectStore spy = spy(store);
            ArgumentCaptor<Path> file = ArgumentCaptor.forClass(Path.class);

            ProjectDTO answer = upload(spy, ProjectUpload.MAX_BYTES).upload(BRIDGE, body(ProjectTestFiles.plain()),
                    null, null, null, name);

            verify(spy).importProject(eq(UID), file.capture(), isNull(), isNull(), anyString());
            Path landed = file.getValue().toAbsolutePath().normalize();
            assertEquals(uploadFolder.toAbsolutePath().normalize(), landed.getParent().getParent(), name);
            assertEquals(ProjectUpload.cleanName(name), Objects.requireNonNull(store.get(UID)).sourceName(), name);
            assertEquals(ProjectNames.clean(ProjectUpload.cleanName(name)), answer.sourceName, name);
            assertCleanedUp();
        }
    }

    @Test
    void noCaseLogsOrAnswersASecretOrTheTemporaryPath() throws Exception {
        String wrongPassword = "made-up wrong 99";
        List<UploadCall> cases = List.of(
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null,
                        header(ProjectTestFiles.PASSWORD), null, null),
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null, header(wrongPassword),
                        null, null),
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null, null, null, null),
                () -> upload().upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null,
                        header(wrongPassword) + "!", null, null),
                () -> upload(store, 1000).upload(BRIDGE, body(ProjectTestFiles.protectedProject()), null,
                        header(ProjectTestFiles.PASSWORD), null, null),
                () -> upload().upload(BRIDGE, new ByteArrayInputStream(new byte[] { 1, 2, 3 }), null,
                        header(wrongPassword), null, null));
        for (UploadCall call : cases) {
            try {
                call.run();
            } catch (KnxRestException e) {
                errorTexts.add(e.text());
            }
        }
        assertEquals(5, errorTexts.size());

        List<String> secrets = new ArrayList<>(List.of(ProjectTestFiles.PASSWORD, header(ProjectTestFiles.PASSWORD),
                wrongPassword, header(wrongPassword), uploadFolder.toString(), folder.toString(), "upload/"));
        secrets.addAll(ProjectTestFiles.madeUpValues());
        secrets.add("MADEUP-");
        List<String> outputs = new ArrayList<>(errorTexts);
        outputs.addAll(Objects.requireNonNull(log).all());
        for (String output : outputs) {
            for (String secret : secrets) {
                assertFalse(output.contains(secret), "\"" + output + "\" holds a secret");
            }
        }
        assertCleanedUp();
    }
}
