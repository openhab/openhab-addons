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
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.project.ProjectTestFiles;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.binding.knx.internal.projectstore.LogCapture;
import org.openhab.binding.knx.internal.rest.dto.KeyringUploadDTO;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

import io.calimero.secure.Keyring;

/**
 * Checks the keyring upload: the file is stored unchanged under the bridge's fixed name, the signature is checked
 * only with a password, a file that is no keyring keeps the earlier one, and nothing secret is answered or logged.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class KeyringUploadTest {
    private static final String BRIDGE = "knx:ip:home";
    private static final String STORED_NAME = "knx_ip_home.knxkeys";

    private @TempDir Path folder = Path.of("");
    private Path uploadFolder = Path.of("");
    private Path keyringFolder = Path.of("");
    private Path configMisc = Path.of("");
    private List<Uploads.Password> passwords = new ArrayList<>();
    private List<String> errorTexts = new ArrayList<>();
    private Uploads uploads = new Uploads(Path.of("target", "unused"));
    private @Nullable LogCapture log;

    @BeforeEach
    void setUp() throws IOException {
        uploadFolder = folder.resolve("knx").resolve("upload");
        keyringFolder = folder.resolve("knx").resolve("keyring");
        configMisc = Files.createDirectories(folder.resolve("conf").resolve("misc"));
        passwords = new ArrayList<>();
        errorTexts = new ArrayList<>();
        uploads = new Uploads(uploadFolder, passwords::add);
        log = new LogCapture();
    }

    @AfterEach
    void closeLog() {
        LogCapture capture = log;
        if (capture != null) {
            capture.close();
        }
    }

    private KnxRestService service() {
        return new KnxRestService(RestFixtures.factory(RestFixtures.handler("home", new TelegramMonitor())),
                new KnxProjectStore(folder.resolve("knx"), Clock.systemUTC()), mock(ThingRegistry.class),
                mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class));
    }

    private KeyringUpload upload(long maxBytes) {
        return new KeyringUpload(service(), uploads, keyringFolder, configMisc, maxBytes);
    }

    private KeyringUpload upload() {
        return upload(KeyringUpload.MAX_BYTES);
    }

    private static byte[] keyringBytes() throws Exception {
        return KeyringTestData.fileContent("openhab6.knxkeys").getBytes(StandardCharsets.UTF_8);
    }

    private static InputStream body(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    private static String header(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes(StandardCharsets.UTF_8));
    }

    private Path stored() {
        return keyringFolder.resolve(STORED_NAME);
    }

    private void assertFails(KnxRestError error, Executable call) {
        KnxRestException e = assertThrows(KnxRestException.class, call);
        assertEquals(error.code(), e.code());
        errorTexts.add(e.text());
    }

    private void assertCleanedUp() throws IOException {
        if (Files.exists(uploadFolder)) {
            try (Stream<Path> left = Files.list(uploadFolder)) {
                assertEquals(List.of(), left.toList());
            }
        }
        if (Files.exists(keyringFolder)) {
            try (Stream<Path> files = Files.list(keyringFolder)) {
                assertEquals(List.of(STORED_NAME), files.map(file -> String.valueOf(file.getFileName())).toList());
            }
        }
        for (Uploads.Password password : passwords) {
            assertTrue(password.isWiped());
        }
    }

    @Test
    void theKeyringIsStoredUnchangedUnderTheBridgesFixedName() throws Exception {
        KeyringUploadDTO answer = upload().upload(BRIDGE, body(keyringBytes()), null, null);

        assertTrue(answer.stored);
        assertEquals(STORED_NAME, answer.fileName);
        assertNull(answer.signatureValid);
        assertFalse(answer.shadowedByConfigFolder);
        assertNotNull(answer.keyring);
        assertTrue(Objects.requireNonNull(answer.keyring).loaded);
        assertEquals(STORED_NAME, Objects.requireNonNull(answer.keyring).fileName);
        assertNull(Objects.requireNonNull(answer.keyring).signatureValid);
        assertEquals(17, Objects.requireNonNull(Objects.requireNonNull(answer.keyring).interfaces).size());
        assertArrayEquals(keyringBytes(), Files.readAllBytes(stored()));
        if (FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            assertEquals("rw-------", PosixFilePermissions.toString(Files.getPosixFilePermissions(stored())));
            assertEquals("rwx------", PosixFilePermissions.toString(Files.getPosixFilePermissions(keyringFolder)));
        }
        assertCleanedUp();
    }

    @Test
    void thePasswordOnlyChecksTheSignatureAndTheFileIsStoredEitherWay() throws Exception {
        KeyringUploadDTO right = upload().upload(BRIDGE, body(keyringBytes()), null, header(KeyringTestData.PASSWORD));
        assertEquals(Boolean.TRUE, right.signatureValid);
        assertEquals(Boolean.TRUE, Objects.requireNonNull(right.keyring).signatureValid);
        Files.delete(stored());

        KeyringUploadDTO wrong = upload().upload(BRIDGE, body(keyringBytes()), null, header("not the made-up one"));
        assertTrue(wrong.stored);
        assertEquals(Boolean.FALSE, wrong.signatureValid);
        assertArrayEquals(keyringBytes(), Files.readAllBytes(stored()));
        assertEquals(2, passwords.size());
        assertCleanedUp();
    }

    @Test
    void aNewUploadReplacesTheEarlierFile() throws Exception {
        Files.createDirectories(keyringFolder);
        Files.writeString(stored(), "made-up earlier file", StandardCharsets.UTF_8);

        upload().upload(BRIDGE, body(keyringBytes()), null, null);

        assertArrayEquals(keyringBytes(), Files.readAllBytes(stored()));
        assertCleanedUp();
    }

    @Test
    void aFileThatIsNoKeyringKeepsTheEarlierOne() throws Exception {
        upload().upload(BRIDGE, body(keyringBytes()), null, null);
        List<byte[]> bodies = List.of("made-up text, no keyring".getBytes(StandardCharsets.UTF_8), new byte[0],
                "<?xml version=\"1.0\"?><Other xmlns=\"http://knx.org/xml/keyring/1\"/>"
                        .getBytes(StandardCharsets.UTF_8),
                "<Keyring xmlns=\"http://knx.org/xml/keyring/1\"><Interface".getBytes(StandardCharsets.UTF_8));
        for (byte[] bytes : bodies) {
            assertFails(KnxRestError.NOT_A_KEYRING,
                    () -> upload().upload(BRIDGE, body(bytes), null, header(KeyringTestData.PASSWORD)));
            assertArrayEquals(keyringBytes(), Files.readAllBytes(stored()));
        }
        assertCleanedUp();
    }

    @Test
    void aFileWithADocumentTypeOrAnEntityIsRefusedBeforeItIsParsed() throws Exception {
        String xml = KeyringTestData.fileContent("openhab6.knxkeys");
        int start = xml.indexOf("<Keyring");
        List<String> hostile = List.of(
                xml.substring(0, start) + "<!DOCTYPE Keyring [<!ENTITY made-up \"up\">]>" + xml.substring(start),
                xml.substring(0, start) + "<!doctype Keyring>" + xml.substring(start),
                xml.substring(0, start) + "<!ENTITY x SYSTEM \"file:///made-up\">" + xml.substring(start),
                xml.replace("<Keyring", "\u0000<Keyring"));
        for (String file : hostile) {
            assertFails(KnxRestError.NOT_A_KEYRING,
                    () -> upload().upload(BRIDGE, body(file.getBytes(StandardCharsets.UTF_8)), null, null));
            assertFalse(Files.exists(stored()));
        }
        assertCleanedUp();
    }

    @Test
    void theCheckForADocumentTypeRunsBeforeTheParserAndSeesEveryEncoding() throws Exception {
        String xml = KeyringTestData.fileContent("openhab6.knxkeys");
        int end = xml.lastIndexOf("</Keyring>");
        // the parser alone would read both: a comment is ignored, and an XML file may be written in UTF-16
        List<byte[]> refused = List.of(
                (xml.substring(0, end) + "<!-- <!ENTITY made-up> -->" + xml.substring(end))
                        .getBytes(StandardCharsets.UTF_8),
                xml.replace("\uFEFF", "").replaceFirst("encoding=\"utf-8\"", "encoding=\"UTF-16\"")
                        .getBytes(StandardCharsets.UTF_16));
        for (byte[] file : refused) {
            assertFails(KnxRestError.NOT_A_KEYRING, () -> upload().upload(BRIDGE, body(file), null, null));
            assertFalse(Files.exists(stored()));
        }
        assertCleanedUp();
    }

    private KeyringUpload parsedInto(List<String> parsed) {
        return new KeyringUpload(service(), uploads, keyringFolder, configMisc, KeyringUpload.MAX_BYTES, file -> {
            parsed.add(file);
            return Keyring.load(file);
        });
    }

    private static byte[] concat(byte[] first, byte[] second) {
        byte[] both = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, both, first.length, second.length);
        return both;
    }

    @Test
    void onlyAFileThatStartsAsPlainAsciiCompatibleXmlReachesTheParser() throws Exception {
        String xml = KeyringTestData.fileContent("openhab6.knxkeys").replace("﻿", "");
        String body = xml.substring(xml.indexOf("?>") + 2);
        byte[] ebcdic = new byte[64];
        Arrays.fill(ebcdic, (byte) 0x40);
        byte[] ebcdicStart = { 0x4C, 0x6F, (byte) 0xA7, (byte) 0x94 };
        System.arraycopy(ebcdicStart, 0, ebcdic, 0, ebcdicStart.length);
        List<byte[]> refused = List.of(ebcdic,
                concat(new byte[] { (byte) 0xFE, (byte) 0xFF }, xml.getBytes(StandardCharsets.UTF_16BE)),
                ("<?xml version=\"1.0\" encoding=\"IBM037\"?>" + body).getBytes(StandardCharsets.UTF_8),
                ("<?xml version=\"1.0\" encoding='utf-16'?>" + body).getBytes(StandardCharsets.UTF_8),
                ("<?xml version=\"1.0\"" + " ".repeat(200) + "?>" + body).getBytes(StandardCharsets.UTF_8),
                (" " + xml).getBytes(StandardCharsets.UTF_8),
                concat(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF },
                        ("x" + xml).getBytes(StandardCharsets.UTF_8)));
        List<String> parsed = new ArrayList<>();
        for (byte[] file : refused) {
            assertFails(KnxRestError.NOT_A_KEYRING, () -> parsedInto(parsed).upload(BRIDGE, body(file), null, null));
            assertFalse(Files.exists(stored()));
        }
        assertEquals(List.of(), parsed);

        List<byte[]> accepted = List.of(keyringBytes(), xml.getBytes(StandardCharsets.UTF_8),
                xml.replaceFirst("encoding=\"utf-8\"", "encoding=\"US-ASCII\"").getBytes(StandardCharsets.UTF_8),
                xml.replaceFirst(" encoding=\"utf-8\"", "").getBytes(StandardCharsets.UTF_8),
                body.strip().getBytes(StandardCharsets.UTF_8));
        for (byte[] file : accepted) {
            assertTrue(parsedInto(parsed).upload(BRIDGE, body(file), null, null).stored);
            assertArrayEquals(file, Files.readAllBytes(stored()));
        }
        assertEquals(accepted.size(), parsed.size());
        assertCleanedUp();
    }

    @Test
    void aKeyringAboveTheCapIsRefused() throws Exception {
        InputStream untouchable = new InputStream() {
            @Override
            public int read() {
                throw new AssertionError("the body was read");
            }
        };
        assertFails(KnxRestError.TOO_LARGE, () -> upload().upload(BRIDGE, untouchable,
                String.valueOf(KeyringUpload.MAX_BYTES + 1), header(KeyringTestData.PASSWORD)));
        assertFails(KnxRestError.TOO_LARGE, () -> upload(1000).upload(BRIDGE, body(keyringBytes()), null, null));
        assertFails(KnxRestError.TOO_LARGE, () -> upload(1000).upload(BRIDGE, body(keyringBytes()), "900", null));
        assertFalse(Files.exists(stored()));
        assertCleanedUp();
    }

    @Test
    void aBadPasswordHeaderOrAnUnknownBridgeIsRefused() throws Exception {
        assertFails(KnxRestError.BAD_INPUT,
                () -> upload().upload(BRIDGE, body(keyringBytes()), null, "made-up-not-base64!"));
        assertEquals(Uploads.PASSWORD_TEXT, errorTexts.get(0));
        assertFails(KnxRestError.UNKNOWN_BRIDGE,
                () -> upload().upload("knx:ip:gone", body(keyringBytes()), null, null));
        assertFalse(Files.exists(stored()));
        assertCleanedUp();
    }

    @Test
    void aFileOfThatNameInTheConfigFolderIsReportedAsShadowing() throws Exception {
        Files.writeString(configMisc.resolve(STORED_NAME), "made-up", StandardCharsets.UTF_8);

        assertTrue(upload().upload(BRIDGE, body(keyringBytes()), null, null).shadowedByConfigFolder);
    }

    @Test
    void theKeyringUploadWaitsForNoProjectUploadAndIsBusyMeanwhile() throws Exception {
        KnxProjectStore store = new KnxProjectStore(folder.resolve("knx"), Clock.systemUTC());
        KnxRestService service = new KnxRestService(
                RestFixtures.factory(RestFixtures.handler("home", new TelegramMonitor())), store,
                mock(ThingRegistry.class), mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class));
        ProjectUpload project = new ProjectUpload(service, store, uploads);
        List<KnxRestException> inner = new ArrayList<>();
        byte[] keyring = keyringBytes();
        InputStream projectBody = new ByteArrayInputStream(Files.readAllBytes(ProjectTestFiles.plain())) {
            private boolean asked;

            @Override
            public synchronized int read(byte @Nullable [] b, int off, int len) {
                if (!asked) {
                    asked = true;
                    inner.add(assertThrows(KnxRestException.class,
                            () -> upload().upload(BRIDGE, body(keyring), null, null)));
                }
                return super.read(b, off, len);
            }
        };

        project.upload(BRIDGE, projectBody, null, null, null, null);

        assertEquals(KnxRestError.BUSY.code(), inner.get(0).code());
        assertFalse(Files.exists(stored()));
        assertTrue(upload().upload(BRIDGE, body(keyring), null, null).stored);
    }

    @Test
    void noAnswerErrorOrLogLineHoldsAValueOfTheFileOrThePassword() throws Exception {
        List<String> outputs = new ArrayList<>();
        outputs.add(RestFixtures.GSON.toJson(upload().upload(BRIDGE, body(keyringBytes()), null, null)));
        outputs.add(RestFixtures.GSON
                .toJson(upload().upload(BRIDGE, body(keyringBytes()), null, header(KeyringTestData.PASSWORD))));
        outputs.add(RestFixtures.GSON
                .toJson(upload().upload(BRIDGE, body(keyringBytes()), null, header("made-up wrong 99"))));
        assertFails(KnxRestError.NOT_A_KEYRING, () -> upload().upload(BRIDGE,
                body("made-up".getBytes(StandardCharsets.UTF_8)), null, header(KeyringTestData.PASSWORD)));
        assertFails(KnxRestError.BAD_INPUT,
                () -> upload().upload(BRIDGE, body(keyringBytes()), null, header(KeyringTestData.PASSWORD) + "!"));
        outputs.addAll(errorTexts);
        outputs.addAll(Objects.requireNonNull(log).all());

        List<String> secrets = new ArrayList<>(KnxRestSecretsTest.secrets());
        secrets.addAll(List.of(header(KeyringTestData.PASSWORD), "made-up wrong 99", header("made-up wrong 99"),
                uploadFolder.toString()));
        int checked = 0;
        for (String output : outputs) {
            String text = output.toLowerCase(Locale.ROOT);
            for (String secret : secrets) {
                assertFalse(text.contains(secret.toLowerCase(Locale.ROOT)), "\"" + output + "\" holds a secret");
                checked++;
            }
        }
        assertTrue(checked > 40 * outputs.size());
        assertCleanedUp();
    }
}
