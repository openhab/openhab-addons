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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.KeyFactory;
import java.security.Provider;
import java.security.Security;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.auth.keyboard.UserInteraction;
import org.apache.sshd.client.config.hosts.DefaultConfigFileHostEntryResolver;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.future.CancelOption;
import org.apache.sshd.common.keyprovider.FileKeyPairProvider;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.OpenHAB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.i2p.crypto.eddsa.EdDSASecurityProvider;

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

    /**
     * Enable or disable strict host key checking. When enabled, unknown host keys
     * are rejected. When disabled (default), unknown keys are accepted on first
     * connection (TOFU) and saved to known_hosts. Changed keys are always rejected.
     */
    public void setStrictHostKeyChecking(boolean strict) {
        this.strictHostKeyChecking = strict;
        logger.debug("Strict host key checking {}", strict ? "enabled" : "disabled");
    }

    /**
     * Stop the SSH client and release resources. Called from the handler factory
     * deactivate to ensure clean shutdown during OSGi bundle restarts.
     */
    public void shutdown() {
        try {
            client.stop();
            logger.debug("SSH client stopped");
        } catch (Exception e) {
            logger.debug("Error stopping SSH client: {}", e.getMessage());
        }
    }

    private final SshClient client;
    private final String ohPrivateKeyDirString = OpenHAB.getUserDataFolder() + "/ddwrt/keys";
    private volatile boolean strictHostKeyChecking = false;

    private SshClientManager() {
        File ohPrivateKeyDir = new File(ohPrivateKeyDirString);

        if (!ohPrivateKeyDir.exists()) {
            logger.debug("Creating directory {}", ohPrivateKeyDirString);
            ohPrivateKeyDir.mkdirs();
        }

        // In OSGi, JCA ServiceLoader doesn't auto-discover providers from other bundles.
        // Directly instantiate (not via reflection) so the EdDSA classes are loaded by
        // this bundle's classloader, which also sees sshd-osgi. This ensures JCA can
        // resolve EdDSAPublicKeySpec when SSHD decodes Ed25519 keys from known_hosts.
        // Always remove/re-add the provider. JCA providers are JVM-global and survive
        // OSGi bundle restarts; otherwise an old provider instance can retain a stale
        // bundle classloader and later fail with ClassNotFoundException for
        // net.i2p.crypto.eddsa.KeyFactory.
        registerEdDsaProvider();
        logEdDsaRuntimeDiagnostics("after-eddsa-provider-registration");

        client = Objects.requireNonNull(SshClient.setUpDefaultClient());
        logEdDsaRuntimeDiagnostics("after-sshd-client-setup");
        // TOFU: trust on first use, persist to ~/.ssh/known_hosts, reject changed keys
        Path knownHostsPath = getHomeSshDir() != null ? Objects.requireNonNull(getHomeSshDir()).resolve("known_hosts")
                : Paths.get(ohPrivateKeyDirString, "known_hosts");
        ServerKeyVerifier verifier = new KnownHostsServerKeyVerifier((s, a, k) -> {
            if (strictHostKeyChecking) {
                logger.warn("Rejecting unknown host key for {} (strict host key checking is enabled)", a);
                return false;
            }
            logger.debug("TOFU: auto-accepting host key for {}", a);
            return true;
        }, knownHostsPath) {
            @Override
            @NonNullByDefault({})
            public boolean acceptModifiedServerKey(ClientSession session, java.net.SocketAddress remoteAddress,
                    org.apache.sshd.client.config.hosts.KnownHostEntry entry, java.security.PublicKey expected,
                    java.security.PublicKey actual) throws Exception {
                String host = remoteAddress.toString().replaceFirst("^/", "");
                int line = findKnownHostsLine(knownHostsPath, host);
                logger.warn(
                        "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                                + "    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!\n"
                                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n"
                                + "IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!\n"
                                + "The host key for {} has changed.\n"
                                + "Add correct host key in {} to get rid of this message.\n"
                                + "Offending key in {}:{}\n" + "Host key verification failed.",
                        host, knownHostsPath, knownHostsPath, line);
                return false;
            }
        };
        client.setServerKeyVerifier(verifier);
        logger.debug("SSH Client Key Verifier: known_hosts at {}", knownHostsPath);
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
        logEdDsaRuntimeDiagnostics("after-sshd-client-start");
    }

    private void registerEdDsaProvider() {
        Provider existing = Security.getProvider(EdDSASecurityProvider.PROVIDER_NAME);
        if (existing != null) {
            logger.debug(
                    "Removing existing EdDSA security provider before re-registration: provider={}, class={}, "
                            + "classloader={}",
                    existing, existing.getClass().getName(), existing.getClass().getClassLoader());
            Security.removeProvider(EdDSASecurityProvider.PROVIDER_NAME);
        }

        Provider provider = new EdDSASecurityProvider();
        Security.insertProviderAt(provider, 1);
        logger.debug("Registered EdDSA security provider for Ed25519 support: provider={}, class={}, classloader={}",
                provider, provider.getClass().getName(), provider.getClass().getClassLoader());

        Provider registered = Security.getProvider(EdDSASecurityProvider.PROVIDER_NAME);
        if (registered != null) {
            logger.debug("Active EdDSA security provider after registration: provider={}, class={}, classloader={}",
                    registered, registered.getClass().getName(), registered.getClass().getClassLoader());
        }
    }

    /**
     * Emit runtime diagnostics for Ed25519/EdDSA provider and classloader issues.
     * This is intentionally DEBUG-level so it can be enabled on a live Karaf system with:
     * log:set DEBUG org.openhab.binding.ddwrt.internal.api.SshClientManager
     */
    private void logEdDsaRuntimeDiagnostics(String phase) {
        if (!logger.isDebugEnabled()) {
            return;
        }

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        ClassLoader ownCl = Objects.requireNonNull(SshClientManager.class.getClassLoader());
        logger.debug(
                "EdDSA diagnostics [{}]: java.version={}, java.vendor={}, TCCL={}, SshClientManager.classloader={}",
                phase, System.getProperty("java.version"), System.getProperty("java.vendor"), tccl, ownCl);

        logClassInfo(phase, "net.i2p.crypto.eddsa.EdDSASecurityProvider", ownCl);
        logClassInfo(phase, "net.i2p.crypto.eddsa.EdDSAKey", ownCl);
        logClassInfo(phase, "net.i2p.crypto.eddsa.EdDSAPublicKey", ownCl);
        logClassInfo(phase, "net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec", ownCl);
        logClassInfo(phase, "org.apache.sshd.common.util.security.SecurityUtils", ownCl);
        logClassInfo(phase, "org.apache.sshd.common.util.security.eddsa.EdDSASecurityProviderRegistrar", ownCl);
        logClassInfo(phase, "org.apache.sshd.common.util.security.eddsa.NetI2pCryptoEdDSASupport", ownCl);

        if (tccl != null && !tccl.equals(ownCl)) {
            logClassInfo(phase + "/tccl", "net.i2p.crypto.eddsa.EdDSAPublicKey", tccl);
            logClassInfo(phase + "/tccl", "net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec", tccl);
            logClassInfo(phase + "/tccl", "org.apache.sshd.common.util.security.SecurityUtils", tccl);
        }

        logJcaProviderDiagnostics(phase);
        logSshdSecurityUtilsDiagnostics(phase, ownCl);
    }

    private void logClassInfo(String phase, String className, @Nullable ClassLoader loader) {
        if (loader == null) {
            logger.debug("EdDSA diagnostics [{}]: class {} not loadable because classloader is null", phase, className);
            return;
        }
        try {
            Class<?> clazz = Class.forName(className, false, loader);
            CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
            URL location = codeSource != null ? codeSource.getLocation() : null;
            logger.debug("EdDSA diagnostics [{}]: class {} loaded by {} from {}", phase, className,
                    clazz.getClassLoader(), location);
        } catch (Exception t) {
            logger.debug("EdDSA diagnostics [{}]: class {} not loadable via {}: {}: {}", phase, className, loader,
                    t.getClass().getName(), t.getMessage());
        }
    }

    private void logJcaProviderDiagnostics(String phase) {
        Provider[] providers = Security.getProviders();
        for (int i = 0; i < providers.length; i++) {
            Provider provider = providers[i];
            if ("EdDSA".equalsIgnoreCase(provider.getName()) || provider.getService("KeyFactory", "EdDSA") != null
                    || provider.getService("KeyFactory", "Ed25519") != null
                    || provider.getService("Signature", "EdDSA") != null
                    || provider.getService("Signature", "Ed25519") != null) {
                logger.debug(
                        "EdDSA diagnostics [{}]: JCA provider[{}] name={}, version={}, class={}, classloader={}, info={}, KeyFactory.EdDSA={}, KeyFactory.Ed25519={}, Signature.EdDSA={}, Signature.Ed25519={}",
                        phase, i + 1, provider.getName(), provider.getVersionStr(), provider.getClass().getName(),
                        provider.getClass().getClassLoader(), provider.getInfo(),
                        provider.getService("KeyFactory", "EdDSA"), provider.getService("KeyFactory", "Ed25519"),
                        provider.getService("Signature", "EdDSA"), provider.getService("Signature", "Ed25519"));
            }
        }

        logKeyFactoryProvider(phase, "EdDSA");
        logKeyFactoryProvider(phase, "Ed25519");
        logNamedKeyFactoryProvider(phase, "EdDSA", EdDSASecurityProvider.PROVIDER_NAME);
    }

    private void logKeyFactoryProvider(String phase, String algorithm) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            Provider provider = keyFactory.getProvider();
            logger.debug("EdDSA diagnostics [{}]: KeyFactory.getInstance({}) resolved to provider {} ({})", phase,
                    algorithm, provider.getName(), provider.getClass().getName());
        } catch (Exception e) {
            logger.debug("EdDSA diagnostics [{}]: KeyFactory.getInstance({}) failed: {}: {}", phase, algorithm,
                    e.getClass().getName(), e.getMessage());
        }
    }

    private void logNamedKeyFactoryProvider(String phase, String algorithm, String providerName) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm, providerName);
            Provider provider = keyFactory.getProvider();
            logger.debug("EdDSA diagnostics [{}]: KeyFactory.getInstance({}, {}) resolved to provider {} ({})", phase,
                    algorithm, providerName, provider.getName(), provider.getClass().getName());
        } catch (Exception e) {
            logger.debug("EdDSA diagnostics [{}]: KeyFactory.getInstance({}, {}) failed: {}: {}", phase, algorithm,
                    providerName, e.getClass().getName(), e.getMessage());
        }
    }

    private void logSshdSecurityUtilsDiagnostics(String phase, ClassLoader loader) {
        try {
            Class<?> securityUtils = Class.forName("org.apache.sshd.common.util.security.SecurityUtils", false, loader);
            logger.debug("EdDSA diagnostics [{}]: {}.isRegistrationCompleted() -> {}", phase, securityUtils.getName(),
                    SecurityUtils.isRegistrationCompleted());
            logger.debug("EdDSA diagnostics [{}]: {}.isEDDSACurveSupported() -> {}", phase, securityUtils.getName(),
                    SecurityUtils.isEDDSACurveSupported());
        } catch (Exception t) {
            logger.debug("EdDSA diagnostics [{}]: unable to inspect SSHD SecurityUtils: {}: {}", phase,
                    t.getClass().getName(), t.getMessage());
        }
    }

    private List<Path> collectKeyPaths(File ohPrivateKeyDir) {
        List<Path> keyPaths = new ArrayList<>();

        if (ohPrivateKeyDir.exists()) {
            logger.debug("Scanning keys directory {}", ohPrivateKeyDir.getPath());
            for (File f : listFilesOrEmpty(ohPrivateKeyDir)) {
                if (isLikelyPrivateKey(f)) {
                    keyPaths.add(f.toPath());
                    logger.debug("Found key: {}", f.getPath());
                }
            }
        }

        Path homeSshDir = getHomeSshDir();
        if (homeSshDir != null) {
            File homeDir = homeSshDir.toFile();
            if (homeDir.exists()) {
                logger.debug("Scanning keys directory {}", homeDir.getPath());
                for (File f : listFilesOrEmpty(homeDir)) {
                    if (isLikelyPrivateKey(f)) {
                        keyPaths.add(f.toPath());
                        logger.debug("Found key: {}", f.getPath());
                    }
                }
            }
        }

        return keyPaths;
    }

    private static File[] listFilesOrEmpty(File directory) {
        return Objects.requireNonNullElseGet(directory.listFiles(), () -> new File[0]);
    }

    private static @Nullable Path getHomeSshDir() {
        String home = System.getProperty("user.home");
        if (home == null) {
            return null;
        }
        Path sshDir = Paths.get(home, ".ssh");
        if (!sshDir.toFile().exists()) {
            sshDir.toFile().mkdirs();
        }
        return sshDir;
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

    /**
     * Find the line number in known_hosts that matches the given host.
     */
    private static int findKnownHostsLine(Path knownHostsPath, String host) {
        try {
            // Strip port suffix if present (e.g., "192.168.0.1:22" -> "192.168.0.1")
            String bareHost = host.contains(":") ? host.substring(0, host.lastIndexOf(':')) : host;
            List<String> lines = Files.readAllLines(knownHostsPath);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (!line.isEmpty() && !line.startsWith("#") && line.startsWith(bareHost)) {
                    return i + 1; // 1-indexed
                }
            }
        } catch (Exception e) {
            // Ignore - best effort
        }
        return 0;
    }

    public SshAuthSession openAuthSession(String host, int port, String user, @Nullable String password,
            Duration defaultTimeout) throws IOException {
        // Precedence: user@ in hostnames > user parameter > ~/.ssh/config > system username.
        // The config default is "root" but the user can clear it to fall through to
        // ~/.ssh/config or the system username via the HostConfigEntryResolver.
        String effectiveUser = user.isBlank() ? "" : user;
        try {
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
        } catch (IOException | RuntimeException e) {
            if (String.valueOf(e.getMessage()).contains("key spec")
                    || String.valueOf(e.getMessage()).contains("EdDSA")) {
                logger.warn("SSH EdDSA/key-spec failure while connecting to {}:{} as {}: {}", host, port, effectiveUser,
                        e.getMessage(), e);
                logEdDsaRuntimeDiagnostics("openAuthSession-failure");
            }
            throw e;
        }
    }
}
