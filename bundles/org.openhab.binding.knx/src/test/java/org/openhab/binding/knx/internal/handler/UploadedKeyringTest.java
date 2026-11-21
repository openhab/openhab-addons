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
package org.openhab.binding.knx.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openhab.binding.knx.internal.security.KeyringTestData;
import org.openhab.binding.knx.internal.security.SecureSetupException;
import org.openhab.core.OpenHAB;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;

import io.calimero.secure.KnxSecureException;

/**
 * Checks where the bridge finds its keyring: in {@code conf/misc} as before, else in the folder the REST upload
 * writes to, {@code userdata/knx/keyring}; a file in {@code conf/misc} wins.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class UploadedKeyringTest {
    private static final String NAME = "knx_ip_home.knxkeys";

    private @TempDir Path folder = Path.of("");
    private Path configMisc = Path.of("");
    private Path uploaded = Path.of("");
    private @Nullable String previousConfig;
    private @Nullable String previousUserData;

    @BeforeEach
    void pointBothFoldersAtTheTestFolder() throws Exception {
        previousConfig = System.getProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT);
        previousUserData = System.getProperty(OpenHAB.USERDATA_DIR_PROG_ARGUMENT);
        Path config = folder.resolve("conf");
        Path userData = folder.resolve("userdata");
        configMisc = Files.createDirectories(config.resolve("misc"));
        uploaded = Files.createDirectories(userData.resolve("knx").resolve("keyring"));
        System.setProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT, config.toString());
        System.setProperty(OpenHAB.USERDATA_DIR_PROG_ARGUMENT, userData.toString());
    }

    @AfterEach
    void restoreFolders() {
        restore(OpenHAB.CONFIG_DIR_PROG_ARGUMENT, previousConfig);
        restore(OpenHAB.USERDATA_DIR_PROG_ARGUMENT, previousUserData);
    }

    private static void restore(String property, @Nullable String previous) {
        if (previous == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, previous);
        }
    }

    private static IPBridgeThingHandler handler() {
        return new IPBridgeThingHandler(mock(Bridge.class), mock(NetworkAddressService.class));
    }

    private static void writeKeyring(Path file) throws Exception {
        Files.writeString(file, KeyringTestData.fileContent("openhab6.knxkeys"), StandardCharsets.UTF_8);
    }

    /** A readable keyring whose signature matches no password. */
    private static void writeOtherKeyring(Path file) throws Exception {
        KeyringTestData.synthetic(file.getParent(), "", KeyringTestData.groupWithKey(31, 7, 1));
        Files.move(file.getParent().resolve("made-up.knxkeys"), file);
    }

    @Test
    void aFileOnlyInTheConfigFolderIsLoadedAsBefore() throws Exception {
        writeKeyring(configMisc.resolve(NAME));
        IPBridgeThingHandler handler = handler();

        assertTrue(handler.initializeSecurity(NAME, KeyringTestData.PASSWORD));

        assertEquals(NAME, handler.getKeyringFileName());
        assertEquals(17, Objects.requireNonNull(handler.getKeyringFacts()).interfaces().size());
    }

    @Test
    void aFileOnlyInTheUploadFolderIsLoaded() throws Exception {
        writeKeyring(uploaded.resolve(NAME));
        IPBridgeThingHandler handler = handler();

        assertTrue(handler.initializeSecurity(NAME, KeyringTestData.PASSWORD));

        assertEquals(NAME, handler.getKeyringFileName());
        assertTrue(handler.isKeyringSignatureValid());
        assertEquals(17, Objects.requireNonNull(handler.getKeyringFacts()).interfaces().size());
    }

    @Test
    void theConfigFolderWins() throws Exception {
        writeKeyring(configMisc.resolve(NAME));
        writeOtherKeyring(uploaded.resolve(NAME));
        assertTrue(handler().initializeSecurity(NAME, KeyringTestData.PASSWORD));

        writeOtherKeyring(configMisc.resolve("other.knxkeys"));
        writeKeyring(uploaded.resolve("other.knxkeys"));
        IPBridgeThingHandler handler = handler();
        assertThrows(SecureSetupException.class, () -> handler.initializeSecurity("other.knxkeys", ""));
        assertEquals(0, Objects.requireNonNull(handler.getKeyringFacts()).interfaces().size());
    }

    @Test
    void aFileInNeitherFolderFailsWithTheTextOfBefore() {
        KnxSecureException e = assertThrows(KnxSecureException.class,
                () -> handler().initializeSecurity(NAME, KeyringTestData.PASSWORD));

        assertTrue(Objects.requireNonNull(e.getMessage()).startsWith("keyring file configured, but loading failed"),
                e.getMessage());
        assertFalse(Objects.requireNonNull(e.getMessage()).contains("userdata"), e.getMessage());
    }

    @Test
    void aNameWithAFolderPartIsNotLookedForInTheUploadFolder() throws Exception {
        Files.createDirectories(uploaded.resolve("sub"));
        writeKeyring(uploaded.resolve("sub").resolve(NAME));
        writeKeyring(folder.resolve("userdata").resolve("knx").resolve(NAME));

        assertThrows(KnxSecureException.class,
                () -> handler().initializeSecurity("sub/" + NAME, KeyringTestData.PASSWORD));
        assertThrows(KnxSecureException.class,
                () -> handler().initializeSecurity("../" + NAME, KeyringTestData.PASSWORD));
    }
}
