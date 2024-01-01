/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Paths;
import java.security.KeyStore;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.exceptions.PairingFailedException;

/**
 * Tests cases for {@link BoschSslUtil}.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
class BoschSslUtilTest {

    @BeforeAll
    static void beforeAll() {
        prepareTempFolderForKeyStore();
    }

    public static void prepareTempFolderForKeyStore() {
        // Use temp folder for userdata folder
        String tmpDir = System.getProperty("java.io.tmpdir");
        tmpDir = tmpDir != null ? tmpDir : "/tmp";
        System.setProperty("openhab.userdata", tmpDir);
        // prepare temp folder on local drive
        File tempDir = Paths.get(tmpDir, "etc").toFile();
        if (!tempDir.exists()) {
            assertTrue(tempDir.mkdirs());
        }
    }

    @Test
    void getBoschShcClientId() {
        // OpenSource Bosch SHC clients needs start with oss
        assertTrue(BoschSslUtil.getBoschShcClientId().startsWith("oss"));
    }

    @Test
    void getBoschShcServerId() {
        // OpenSource Bosch SHC clients needs start with oss
        assertTrue(BoschSslUtil.getBoschShcServerId("localhost").startsWith("oss"));
        assertTrue(BoschSslUtil.getBoschShcServerId("localhost").contains("localhost"));
    }

    @Test
    void getKeystorePath() {
        BoschSslUtil sslUtil = new BoschSslUtil("123.45.67.89");
        assertTrue(sslUtil.getKeystorePath().endsWith(".jks"));
    }

    /**
     * Test if the keyStore can be created if it doesn't exist.
     */
    @Test
    void keyStoreAndFactory() throws PairingFailedException {
        BoschSslUtil sslUtil1 = new BoschSslUtil("127.0.0.1");

        // remote old, existing jks
        File keyStoreFile = new File(sslUtil1.getKeystorePath());
        keyStoreFile.deleteOnExit();
        if (keyStoreFile.exists()) {
            assertTrue(keyStoreFile.delete());
        }

        assertFalse(keyStoreFile.exists());

        BoschSslUtil sslUtil2 = new BoschSslUtil("127.0.0.1");
        // fist call where keystore is created
        KeyStore keyStore = sslUtil2.getKeyStoreAndCreateIfNecessary();
        assertNotNull(keyStore);

        assertTrue(keyStoreFile.exists());

        // second call where keystore is reopened
        KeyStore keyStore2 = sslUtil2.getKeyStoreAndCreateIfNecessary();
        assertNotNull(keyStore2);

        // basic test if a SSL factory instance can be created
        SslContextFactory factory = sslUtil2.getSslContextFactory();
        assertNotNull(factory);
    }
}
