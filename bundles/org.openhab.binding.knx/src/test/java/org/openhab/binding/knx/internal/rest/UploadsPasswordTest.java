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
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.projectstore.KnxProjectStore;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;

/**
 * Checks that every array that held the password header on its way to a {@code char[]} is all zero afterwards: the
 * byte copy of the header, the decoder's buffer and the arrays of {@link Uploads.Password}.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class UploadsPasswordTest {
    private static final String PASSWORD = "made-up Pässwort 42";

    private @TempDir Path folder = Path.of("");
    private final List<byte[]> headerBytes = new ArrayList<>();
    private final List<char[]> buffers = new ArrayList<>();
    private final List<Uploads.Password> passwords = new ArrayList<>();

    private Uploads uploads() {
        return new Uploads(folder.resolve("upload"), new Uploads.PasswordArrays() {
            @Override
            public void password(Uploads.Password password) {
                passwords.add(password);
            }

            @Override
            public void headerBytes(byte[] bytes) {
                headerBytes.add(bytes);
            }

            @Override
            public void decodeBuffer(char[] buffer) {
                buffers.add(buffer);
            }
        });
    }

    private static String header(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private void assertAllWiped() {
        for (byte[] bytes : headerBytes) {
            for (byte b : bytes) {
                assertEquals(0, b, "the byte copy of the header");
            }
        }
        for (char[] buffer : buffers) {
            for (char c : buffer) {
                assertEquals('\0', c, "the decoder's buffer");
            }
        }
        for (Uploads.Password password : passwords) {
            assertTrue(password.isWiped());
        }
    }

    @Test
    void aGoodPasswordLeavesEveryArrayWiped() {
        String header = header(PASSWORD.getBytes(StandardCharsets.UTF_8));

        try (Uploads.Password password = uploads().password(header)) {
            assertArrayEquals(PASSWORD.toCharArray(), password.chars());
        }

        assertEquals(1, headerBytes.size());
        assertEquals(header.length(), headerBytes.get(0).length);
        assertEquals(1, buffers.size());
        assertTrue(buffers.get(0).length >= PASSWORD.length());
        assertEquals(1, passwords.size());
        assertAllWiped();
    }

    @Test
    void aHeaderThatIsNotBase64LeavesItsByteCopyWiped() {
        KnxRestException e = assertThrows(KnxRestException.class, () -> uploads().password("made-up-not-base64!"));

        assertEquals(Uploads.PASSWORD_TEXT, e.text());
        assertEquals(1, headerBytes.size());
        assertEquals(List.of(), buffers);
        assertEquals(List.of(), passwords);
        assertAllWiped();
    }

    @Test
    void bytesThatAreNotUtf8LeaveEveryArrayWiped() {
        String header = header(new byte[] { 'm', 'a', 'd', 'e', (byte) 0xFF, (byte) 0xFE });

        KnxRestException e = assertThrows(KnxRestException.class, () -> uploads().password(header));

        assertEquals(Uploads.PASSWORD_TEXT, e.text());
        assertEquals(1, headerBytes.size());
        assertEquals(1, buffers.size());
        assertEquals(1, passwords.size());
        assertAllWiped();
    }

    @Test
    void anExceptionWhereThePasswordIsUsedLeavesEveryArrayWiped() {
        String header = header(PASSWORD.getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> {
            try (Uploads.Password password = uploads().password(header)) {
                assertNotNull(password.chars());
                throw new IllegalStateException("made-up failure");
            }
        });

        assertEquals(1, headerBytes.size());
        assertEquals(1, buffers.size());
        assertAllWiped();
    }

    @Test
    void aStoreThatThrowsDuringTheUploadLeavesEveryArrayWiped() throws Exception {
        KnxProjectStore store = mock(KnxProjectStore.class);
        when(store.importProject(any(), any(), any(), any(), anyString()))
                .thenThrow(new IllegalStateException("made-up failure"));
        KnxRestService service = new KnxRestService(
                RestFixtures.factory(RestFixtures.handler("home", new TelegramMonitor())), store,
                mock(ThingRegistry.class), mock(ItemRegistry.class), mock(ItemChannelLinkRegistry.class));
        ProjectUpload upload = new ProjectUpload(service, store, uploads());
        String header = header(PASSWORD.getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> upload.upload("knx:ip:home",
                new ByteArrayInputStream(new byte[] { 1, 2, 3 }), null, header, null, null));

        verify(store).importProject(any(), any(), any(), any(), anyString());
        assertEquals(1, headerBytes.size());
        assertEquals(1, buffers.size());
        assertEquals(1, passwords.size());
        assertAllWiped();
    }
}
