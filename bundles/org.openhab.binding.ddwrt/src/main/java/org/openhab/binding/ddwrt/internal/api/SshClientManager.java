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
package org.openhab.binding.ddwrt.internal.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.keyboard.UserInteraction;
import org.apache.sshd.client.config.hosts.DefaultConfigFileHostEntryResolver;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.future.CancelOption;
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

    private final Logger logger = Objects.requireNonNull(LoggerFactory.getLogger(SshClientManager.class));

    private static final SshClientManager INSTANCE = new SshClientManager();

    public static SshClientManager getInstance() {
        return INSTANCE;
    }

    private final SshClient client;
    private final String ohPrivateKeyDirString = OpenHAB.getUserDataFolder() + "/ddwrt/keys";

    private SshClientManager() {
        File ohPrivateKeyDir = new File(ohPrivateKeyDirString);

        if (!ohPrivateKeyDir.exists()) {
            logger.debug("Creating directory {}", ohPrivateKeyDirString);
            ohPrivateKeyDir.mkdirs();
        }

        // In OSGi, JCA ServiceLoader doesn't auto-discover providers from other bundles.
        // Explicitly register the EdDSA provider so Ed25519 keys work.
        try {
            Class<?> providerClass = Class.forName("net.i2p.crypto.eddsa.EdDSASecurityProvider");
            java.security.Provider eddsaProvider = (java.security.Provider) providerClass.getDeclaredConstructor()
                    .newInstance();
            if (java.security.Security.getProvider(eddsaProvider.getName()) == null) {
                java.security.Security.addProvider(eddsaProvider);
                logger.debug("Registered EdDSA security provider for Ed25519 support");
            }
        } catch (ReflectiveOperationException e) {
            logger.debug("EdDSA provider not available, Ed25519 keys will not be supported: {}", e.getMessage());
        }

        client = Objects.requireNonNull(SshClient.setUpDefaultClient());
        logger.debug("SSH Client Key Verifier Accept All");
        client.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE); // MVP only; replace with pinning
        client.setHostConfigEntryResolver(DefaultConfigFileHostEntryResolver.INSTANCE);

        // Load keys from both directories and register on client so they are
        // available for ProxyJump authentication and all sessions.
        List<Path> keyPaths = collectKeyPaths(ohPrivateKeyDir);
        if (!keyPaths.isEmpty()) {
            logger.debug("Registering {} key files on SSH client", keyPaths.size());
            FileKeyPairProvider keyProvider = new FileKeyPairProvider(keyPaths);
            keyProvider.setPasswordFinder(FilePasswordProvider.EMPTY);
            client.setKeyIdentityProvider(keyProvider);
        }

        client.start();
    }

    private List<Path> collectKeyPaths(File ohPrivateKeyDir) {
        List<Path> keyPaths = new ArrayList<>();

        if (ohPrivateKeyDir.exists()) {
            logger.debug("Scanning keys directory {}", ohPrivateKeyDir.getPath());
            File @Nullable [] files = ohPrivateKeyDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (isLikelyPrivateKey(f)) {
                        keyPaths.add(f.toPath());
                        logger.debug("Found key: {}", f.getPath());
                    }
                }
            }
        }

        Path homeSshDir = getHomeSshDir();
        if (homeSshDir != null) {
            File homeDir = homeSshDir.toFile();
            if (homeDir.exists()) {
                logger.debug("Scanning keys directory {}", homeDir.getPath());
                File @Nullable [] homeFiles = homeDir.listFiles();
                if (homeFiles != null) {
                    for (File f : homeFiles) {
                        if (isLikelyPrivateKey(f)) {
                            keyPaths.add(f.toPath());
                            logger.debug("Found key: {}", f.getPath());
                        }
                    }
                }
            }
        }

        return keyPaths;
    }

    private static @Nullable Path getHomeSshDir() {
        String home = System.getProperty("user.home");
        return home != null ? Paths.get(home, ".ssh") : null;
    }

    // Disable NonNullByDefault on this class to match Apache SSHD’s UserInteraction
    @NonNullByDefault({})
    private static final class BannerCapturingUserInteraction implements UserInteraction {
        private final Logger logger = LoggerFactory.getLogger(BannerCapturingUserInteraction.class);
        private final AtomicReference<@Nullable String> bannerRef;

        private BannerCapturingUserInteraction(AtomicReference<@Nullable String> bannerRef) {
            this.bannerRef = bannerRef;
        }

        @Override
        public boolean isInteractionAllowed(ClientSession session) {
            return true;
        }

        @Override
        public void welcome(ClientSession session, String banner, String lang) {
            logger.debug("{} Banner:\n{}", session.getRemoteAddress(), banner);
            if (banner != null && !banner.isBlank()) {
                bannerRef.set(banner);
            }
        }

        @Override
        public String[] interactive(ClientSession session, String name, String instruction, String lang,
                String[] prompts, boolean[] echo) {
            return new String[0];
        }

        @Override
        public String getUpdatedPassword(ClientSession session, String prompt, String lang) {
            return "";
        }
    }

    /**
     * Check if a file is likely a private key (not a .pub, config, known_hosts, etc.).
     */
    private static boolean isLikelyPrivateKey(File file) {
        if (!file.isFile()) {
            return false;
        }
        String name = file.getName();
        // Skip known non-key files; accept files starting with "id_" or any other file
        // without a common non-key extension
        return !name.endsWith(".pub") && !name.startsWith("known_hosts") && !name.startsWith("config")
                && !name.startsWith("authorized_keys") && !name.endsWith("~");
    }

    @SuppressWarnings("null")
    public SshAuthSession openAuthSession(String host, int port, String user, @Nullable String password,
            @Nullable String privateKeyRef, @Nullable String pinnedFingerprint, Duration defaultTimeout)
            throws IOException {
        // Pass empty string when user is not configured so the HostConfigEntryResolver
        // can fill it from ~/.ssh/config User directive. The resolver falls back to the
        // system username if SSH config also has no User for the host.
        String effectiveUser = (user == null || user.isBlank()) ? "" : user;
        logger.debug("Connecting to {} port {} as {}", host, port, effectiveUser);
        // Port 0 means "not set" — MINA SSHD resolves from ~/.ssh/config or defaults to 22
        ConnectFuture cf = client.connect(effectiveUser, host, port);
        cf.verify(Duration.ofMillis(10000), CancelOption.CANCEL_ON_TIMEOUT);
        ClientSession cs = cf.getSession();

        AtomicReference<@Nullable String> bannerRef = new AtomicReference<>(null);

        cs.setUserInteraction(new BannerCapturingUserInteraction(bannerRef));

        if (password != null && !password.isBlank()) {
            cs.addPasswordIdentity(password);
        }

        // Keys are registered at the client level (see constructor) so they are
        // available for all sessions including ProxyJump hops.

        cs.auth().verify(Duration.ofMillis(10000), CancelOption.CANCEL_ON_TIMEOUT);

        logger.debug("Connected to the server {}:{} as {}", host, port, cs.getUsername());
        logger.debug("Server Ident {}", cs.getServerVersion());

        @Nullable
        String banner = bannerRef.get();
        return new SshAuthSession(cs, defaultTimeout, (banner == null || banner.isBlank()) ? null : banner, host);
    }
}
