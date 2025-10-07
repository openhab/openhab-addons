/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ddwrt.internal.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SshRunner} executing command in a ssh session.
 *
 * @author Lee Ballard - Initial contribution
 */
@NonNullByDefault
public class SshClientManager {

    private final Logger logger = LoggerFactory.getLogger(SshClientManager.class);

    private static final SshClientManager INSTANCE = new SshClientManager();

    public static SshClientManager getInstance() {
        return INSTANCE;
    }

    private final SshClient client;

    private SshClientManager() {
        client = SshClient.setUpDefaultClient();
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE); // MVP only; replace with pinning
        client.start();
    }

    private static @Nullable Path getHomeSshDir() {
        String home = System.getProperty("user.home");
        return home != null ? Paths.get(home, ".ssh") : null;
    }

    public SshRunner openRunner(String host, int port, String user, @Nullable String password,
            @Nullable String privateKeyRef, @Nullable String pinnedFingerprint, Duration defaultTimeout)
            throws IOException {

        String ohPrivateKeyDirString = OpenHAB.getUserDataFolder() + "/ddwrt/keys";
        File ohPrivateKeyDir = new File(ohPrivateKeyDirString);

        ConnectFuture cf = client.connect(user, host, port);
        cf.verify();
        ClientSession session = cf.getSession();

        if (password != null && !password.isBlank()) {
            session.addPasswordIdentity(password);
        }
        List<File> keyFiles = new ArrayList<>();

        if (ohPrivateKeyDir.exists()) {
            logger.debug("opening keys directory {}", ohPrivateKeyDir.getPath());
            Collections.addAll(keyFiles, ohPrivateKeyDir.listFiles());
        }

        Path homeSshDir = getHomeSshDir();
        if (homeSshDir != null) {
            File homePrivateKeyDir = homeSshDir.toFile();
            if (homePrivateKeyDir.exists()) {
                logger.debug("opening keys directory {}", homePrivateKeyDir.getPath());
                Collections.addAll(keyFiles, homePrivateKeyDir.listFiles());
            }
        }

        if (!keyFiles.isEmpty()) {
            logger.debug("keys present");
            for (File privateKeyFile : keyFiles) {
                logger.debug("keys from {}", privateKeyFile.getPath());
                try {
                    FileKeyPairProvider keyPairProvider = new FileKeyPairProvider(
                            Collections.singletonList(privateKeyFile.toPath()));
                    // :TODO: add passphrase support
                    keyPairProvider.setPasswordFinder(FilePasswordProvider.EMPTY);
                    Iterable<KeyPair> keyPairs = keyPairProvider.loadKeys(null);
                    if (keyPairs.iterator().hasNext()) {
                        // Add private key identity
                        session.addPublicKeyIdentity(keyPairs.iterator().next());
                    } else {
                        logger.warn("No valid key pairs found in {}", privateKeyFile.getName());
                    }
                } catch (Exception ex) {
                    logger.warn("Skipping file {}: not a valid key file. Reason: {}", privateKeyFile.getName(),
                            ex.getMessage());
                }
            }
        }

        session.auth().verify();

        logger.debug("Connected to the server {}:{} as {}", host, port, user);

        return new SshRunner(session, defaultTimeout);
    }
}
