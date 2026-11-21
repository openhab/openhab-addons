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

import static org.openhab.binding.knx.internal.KNXBindingConstants.OWN_ADDRESS;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.StatusUpdateCallback;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.binding.knx.internal.security.KeyringFacts;
import org.openhab.binding.knx.internal.security.SecureSetupException;
import org.openhab.core.OpenHAB;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.calimero.GroupAddress;
import io.calimero.IndividualAddress;
import io.calimero.KNXFormatException;
import io.calimero.knxnetip.SecureConnection;
import io.calimero.secure.Keyring;
import io.calimero.secure.Keyring.Backbone;
import io.calimero.secure.Keyring.Interface;
import io.calimero.secure.KnxSecureException;
import io.calimero.secure.Security;
import io.calimero.xml.KNXMLException;

/**
 * The {@link KNXBridgeBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Simon Kaufmann - Initial contribution and API
 * @author Holger Friedrich - KNX Secure configuration
 */
@NonNullByDefault
public abstract class KNXBridgeBaseThingHandler extends BaseBridgeHandler implements StatusUpdateCallback {

    public static class SecureTunnelConfig {
        public SecureTunnelConfig() {
            devKey = new byte[0];
            userKey = new byte[0];
            user = 0;
        }

        public byte[] devKey;
        public byte[] userKey;
        public int user = 0;
    }

    public static class SecureRoutingConfig {
        public SecureRoutingConfig() {
            backboneGroupKey = new byte[0];
            latencyToleranceMs = 0;
        }

        public byte[] backboneGroupKey;
        public long latencyToleranceMs = 0;
    }

    /**
     * Helper class to carry information which can be used by the
     * command line extension (openHAB console).
     */
    public record CommandExtensionData(SortedMap<String, Long> unknownGA) {
    }

    private static final String TUNNEL_USER_ID_TEXT = "error.knx-tunnel-user-id-invalid";
    private static final String TUNNEL_USER_ID_MESSAGE = "tunnelUserId must be a number from 1 to 127";

    private final Logger logger = LoggerFactory.getLogger(KNXBridgeBaseThingHandler.class);
    private final ScheduledExecutorService knxScheduler = ThreadPoolManager.getScheduledPool("knx");
    private final ScheduledExecutorService backgroundScheduler = Executors.newSingleThreadScheduledExecutor();
    @Nullable
    protected Keyring keyring;
    // password used to protect content of the keyring
    private String keyringPassword = "";
    // backbone key (shared password used for secure router mode)

    protected Security openhabSecurity;
    protected SecureRoutingConfig secureRouting;
    protected SecureTunnelConfig secureTunnel;
    private CommandExtensionData commandExtensionData;
    private final TelegramMonitor telegramMonitor;
    private volatile @Nullable KeyringFacts keyringFacts;
    private volatile String keyringFileName = "";
    private volatile boolean keyringSignatureValid = false;

    public KNXBridgeBaseThingHandler(Bridge bridge) {
        super(bridge);
        keyring = null;
        openhabSecurity = Security.newSecurity();
        secureRouting = new SecureRoutingConfig();
        secureTunnel = new SecureTunnelConfig();
        commandExtensionData = new CommandExtensionData(new TreeMap<>());
        telegramMonitor = new TelegramMonitor();
    }

    protected abstract KNXClient getClient();

    /** {@return the client of this bridge, to send through it the way a channel does} */
    public KNXClient client() {
        return getClient();
    }

    public CommandExtensionData getCommandExtensionData() {
        return commandExtensionData;
    }

    /** {@return the group monitor of this bridge; it lives as long as this handler} */
    public TelegramMonitor getTelegramMonitor() {
        return telegramMonitor;
    }

    /** A size out of range must not stop the bus, so it is clamped and said once, not a configuration error. */
    protected void applyMonitorBufferSize(int configured) {
        int size = TelegramMonitor.clampBufferSize(configured);
        if (size != configured) {
            logger.warn("Bridge {}: monitorBufferSize {} is outside 0 to {}, using {}.", thing.getUID(), configured,
                    TelegramMonitor.MAX_BUFFER_SIZE, size);
        }
        telegramMonitor.resize(size);
    }

    /** {@return the name of the keyring file as configured, empty if none is configured} */
    public String getKeyringFileName() {
        return keyringFileName;
    }

    /** {@return true if the loaded keyring's signature matches the configured keyring password} */
    public boolean isKeyringSignatureValid() {
        return keyringSignatureValid;
    }

    /** {@return the address openHAB sends from on the current connection, empty while not connected} */
    public Optional<IndividualAddress> getOwnAddress() {
        return getClient().ownAddress();
    }

    /** {@return what the loaded keyring says about interfaces and secured group addresses, null without a keyring} */
    public @Nullable KeyringFacts getKeyringFacts() {
        return keyringFacts;
    }

    /***
     * Initialize KNX secure if configured (simple interface)
     *
     * @param cKeyringFile keyring file, exported from ETS tool
     * @param cKeyringPassword keyring password, set during export from ETS tool
     * @return
     */
    protected boolean initializeSecurity(String cKeyringFile, String cKeyringPassword) throws KnxSecureException {
        return initializeSecurity(cKeyringFile, cKeyringPassword, "", "", "", "", "");
    }

    /***
     * Initialize KNX secure if configured (full interface)
     *
     * @param cKeyringFile keyring file, exported from ETS tool
     * @param cKeyringPassword keyring password, set during export from ETS tool
     * @param cRouterBackboneGroupKey shared key for secure router mode. If not given, it will be read from keyring.
     * @param cTunnelDevAuth device password for IP interface in tunnel mode. If not given it will be read from keyring
     *            if cTunnelSourceAddr is configured.
     * @param cTunnelUser user id for tunnel mode. Must be an integer >0. If not given it will be read from keyring if
     *            cTunnelSourceAddr is configured.
     * @param cTunnelPassword user password for tunnel mode. If not given it will be read from keyring if
     *            cTunnelSourceAddr is configured.
     * @param cTunnelSourceAddr specify the KNX address to uniquely identify a tunnel connection in secure tunneling
     *            mode. Not required if cTunnelDevAuth, cTunnelUser, and cTunnelPassword are given.
     * @return
     */
    protected boolean initializeSecurity(String cKeyringFile, String cKeyringPassword, String cRouterBackboneGroupKey,
            String cTunnelDevAuth, String cTunnelUser, String cTunnelPassword, String cTunnelSourceAddr)
            throws KnxSecureException {
        keyring = null;
        keyringFacts = null;
        keyringFileName = "";
        keyringSignatureValid = false;
        keyringPassword = "";
        IndividualAddress secureTunnelSourceAddr = null;
        secureRouting = new SecureRoutingConfig();
        secureTunnel = new SecureTunnelConfig();

        boolean securityInitialized = false;

        // step 1: secure routing, backbone group key manually specified in OH config (typically it is read from
        // keyring)
        if (!cRouterBackboneGroupKey.isBlank()) {
            // provided in config, this will override whatever is read from keyring
            String key = cRouterBackboneGroupKey.trim().replaceFirst("^0x", "").trim().replace(" ", "");
            if (!key.isEmpty()) {
                // helper may throw KnxSecureException
                secureRouting.backboneGroupKey = secHelperParseBackboneKey(key);
                securityInitialized = true;
            }
        }

        // step 2: check if valid tunnel parameters are specified in config
        if (!cTunnelSourceAddr.isBlank()) {
            try {
                secureTunnelSourceAddr = new IndividualAddress(cTunnelSourceAddr.trim());
                securityInitialized = true;
            } catch (KNXFormatException e) {
                throw new KnxSecureException("tunnel source address cannot be parsed, valid format is x.y.z");
            }
        }
        if (!cTunnelDevAuth.isBlank()) {
            secureTunnel.devKey = SecureConnection.hashDeviceAuthenticationPassword(cTunnelDevAuth.toCharArray());
            securityInitialized = true;
        }
        if (!cTunnelPassword.isBlank()) {
            secureTunnel.userKey = SecureConnection.hashUserPassword(cTunnelPassword.toCharArray());
            securityInitialized = true;
        }
        if (!cTunnelUser.isBlank()) {
            String user = cTunnelUser.trim();
            try {
                secureTunnel.user = Integer.decode(user);
            } catch (NumberFormatException e) {
                throw new SecureSetupException(TUNNEL_USER_ID_TEXT, TUNNEL_USER_ID_MESSAGE);
            }
            if (secureTunnel.user < 1 || secureTunnel.user > 127) {
                throw new SecureSetupException(TUNNEL_USER_ID_TEXT, TUNNEL_USER_ID_MESSAGE);
            }
            securityInitialized = true;
        }

        // step 3: keyring
        if (!cKeyringFile.isBlank()) {
            // filename defined in config, start parsing
            try {
                // load keyring file from config dir, folder misc
                String keyringUri = keyringLocation(
                        OpenHAB.getConfigFolder() + File.separator + "misc" + File.separator + cKeyringFile,
                        cKeyringFile);
                try {
                    keyring = Keyring.load(keyringUri);
                } catch (KNXMLException e) {
                    throw new KnxSecureException("keyring file configured, but loading failed: ", e);
                }
                if (keyring == null) {
                    throw new KnxSecureException("keyring file configured, but loading failed: " + keyringUri);
                }

                KeyringFacts facts = new KeyringFacts(Objects.requireNonNull(keyring));
                keyringFacts = facts;
                keyringFileName = cKeyringFile;
                keyringSignatureValid = facts.signatureMatches(cKeyringPassword.toCharArray());
                if (!keyringSignatureValid) {
                    throw new SecureSetupException("error.knx-keyring-wrong-password",
                            "keyring password does not match the keyring file");
                }

                // loading was successful, check signatures
                // -> disabled, as Calimero v2.5 does this within the load() function
                // if (!keyring.verifySignature(cKeyringPassword.toCharArray()))
                // throw new KnxSecureException(
                // "signature verification failed, please check keyring file: " + keyringUri);
                keyringPassword = cKeyringPassword;

                // We use a specific Security instance instead of default Calimero static instance
                // Security.defaultInstallation().
                // This necessary as it seems there is no possibility to clear the global instance on config changes.
                openhabSecurity.useKeyring(keyring, keyringPassword.toCharArray());

                securityInitialized = true;
            } catch (KnxSecureException e) {
                keyring = null;
                keyringPassword = "";
                throw e;
            } catch (Exception e) {
                // load() may throw KnxSecureException or other undeclared exceptions, e.g. UncheckedIOException when
                // file is not found
                keyring = null;
                keyringPassword = "";
                throw new KnxSecureException("keyring file configured, but loading failed", e);
            }
        }

        // step 4: router: load backboneGroupKey from keyring if not manually specified
        if ((secureRouting.backboneGroupKey.length == 0) && (keyring != null)) {
            // backbone group key is only available if secure routers are present
            final Optional<byte[]> key = secHelperReadBackboneKey(keyring, keyringPassword);
            if (key.isPresent()) {
                secureRouting.backboneGroupKey = key.get();
                securityInitialized = true;
            }
        }

        // step 5: router: load latencyTolerance
        // default to 2000ms
        // this parameter is currently not exposed in config, in case it must be set by using the keyring
        secureRouting.latencyToleranceMs = 2000;
        if (keyring != null) {
            // backbone latency is only relevant if secure routers are present
            final Optional<Backbone> bb = keyring.backbone();
            if (bb.isPresent()) {
                final long toleranceMs = bb.get().latencyTolerance().toMillis();
                secureRouting.latencyToleranceMs = toleranceMs;
            }
        }

        // step 6: tunnel: load data from keyring
        if (secureTunnelSourceAddr != null) {
            // requires a valid keyring
            if (keyring == null) {
                throw new KnxSecureException("valid keyring specification required for secure tunnel mode");
            }
            // other parameters will not be accepted, since all is read from keyring in this case
            if ((secureTunnel.userKey.length > 0) || secureTunnel.user != 0 || (secureTunnel.devKey.length > 0)) {
                throw new KnxSecureException(
                        "tunnelSourceAddr is configured, please do not specify other parameters of secure tunnel");
            }

            Optional<SecureTunnelConfig> config = secHelperReadTunnelConfig(keyring, keyringPassword,
                    secureTunnelSourceAddr);
            if (config.isEmpty()) {
                throw new KnxSecureException("tunnel definition cannot be read from keyring");
            }
            secureTunnel = config.get();
        }
        return securityInitialized;
    }

    /**
     * {@return the keyring in the config folder; if it is not there, one of that name the REST upload stored in
     * {@code userdata/knx/keyring}, which is only looked for under a plain file name}
     */
    private static String keyringLocation(String inConfigFolder, String fileName) {
        if (new File(inConfigFolder).exists() || fileName.contains("/") || fileName.contains("\\")
                || ".".equals(fileName) || "..".equals(fileName)) {
            return inConfigFolder;
        }
        File uploaded = new File(OpenHAB.getUserDataFolder() + File.separator + "knx" + File.separator + "keyring"
                + File.separator + fileName);
        return uploaded.isFile() ? uploaded.getPath() : inConfigFolder;
    }

    /***
     * converts hex string (32 characters) to byte[16]
     *
     * @param hexString 32 characters hex
     * @return key in byte array format
     */
    public static byte[] secHelperParseBackboneKey(String hexString) throws KnxSecureException {
        if (hexString.length() != 32) {
            throw new KnxSecureException("backbone key must be 32 characters (16 byte hex notation)");
        }

        byte[] parsed = new byte[16];
        try {
            for (byte i = 0; i < 16; i++) {
                parsed[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
            }
        } catch (NumberFormatException e) {
            throw new KnxSecureException("backbone key configured, cannot parse hex string, illegal character", e);
        }
        return parsed;
    }

    public static Optional<byte[]> secHelperReadBackboneKey(@Nullable Keyring keyring, String keyringPassword) {
        if (keyring == null) {
            throw new KnxSecureException("keyring not available, cannot read backbone key");
        }
        final Optional<Backbone> bb = keyring.backbone();
        if (bb.isPresent()) {
            final Optional<byte[]> gk = bb.get().groupKey();
            if (gk.isPresent()) {
                byte[] secureRoutingBackboneGroupKey = keyring.decryptKey(gk.get(), keyringPassword.toCharArray());
                if (secureRoutingBackboneGroupKey.length != 16) {
                    throw new KnxSecureException("backbone key found, unexpected length != 16");
                }
                return Optional.of(secureRoutingBackboneGroupKey);
            }
        }
        return Optional.empty();
    }

    public static Optional<SecureTunnelConfig> secHelperReadTunnelConfig(@Nullable Keyring keyring,
            String keyringPassword, IndividualAddress secureTunnelSourceAddr) {
        if (keyring == null) {
            throw new KnxSecureException("keyring not available, cannot read tunnel config");
        }
        // iterate all interfaces to find matching secureTunnelSourceAddr
        SecureTunnelConfig secureTunnel = new SecureTunnelConfig();
        Iterator<List<Interface>> itInterface = keyring.interfaces().values().iterator();
        boolean complete = false;
        while (!complete && itInterface.hasNext()) {
            List<Interface> eInterface = itInterface.next();
            // tunnels are nested
            Iterator<Interface> itTunnel = eInterface.iterator();
            while (!complete && itTunnel.hasNext()) {
                Interface eTunnel = itTunnel.next();

                if (secureTunnelSourceAddr.equals(eTunnel.address())) {
                    String pw = "";
                    final Optional<byte[]> pwBytes = eTunnel.password();
                    if (pwBytes.isPresent()) {
                        pw = new String(keyring.decryptPassword(pwBytes.get(), keyringPassword.toCharArray()));
                        secureTunnel.userKey = SecureConnection.hashUserPassword(pw.toCharArray());
                    }

                    String au = "";
                    final Optional<byte[]> auBytes = eTunnel.authentication();
                    if (auBytes.isPresent()) {
                        au = new String(keyring.decryptPassword(auBytes.get(), keyringPassword.toCharArray()));
                        secureTunnel.devKey = SecureConnection.hashDeviceAuthenticationPassword(au.toCharArray())
                                .clone();
                    }

                    // set user, 0=fail
                    secureTunnel.user = eTunnel.user();

                    return Optional.of(secureTunnel);
                }
            }
        }
        return Optional.empty();
    }

    /***
     * Show all secure group addresses and surrogates. A surrogate is the device which is asked to carry out an indirect
     * read/write request.
     * Simpler approach w/o surrogates: Security.defaultInstallation().groupSenders().toString();
     */
    public static String secHelperGetSecureGroupAddresses(final Security openhabSecurity) {
        Map<GroupAddress, Set<String>> groupSendersWithSurrogate = new HashMap<GroupAddress, Set<String>>();
        final Map<GroupAddress, Set<IndividualAddress>> senders = openhabSecurity.groupSenders();
        for (var entry : senders.entrySet()) {
            final GroupAddress ga = entry.getKey();
            // the following approach is uses by Calimero to deduce the surrogate for GA diagnostics
            // see calimero-core security/SecureApplicationLayer.java, surrogate(...)
            IndividualAddress surrogate = null;
            try {
                surrogate = senders.getOrDefault(ga, Set.of()).stream().findAny().get();
            } catch (NoSuchElementException ignored) {
            }
            Set<String> devices = new HashSet<String>();
            for (var device : entry.getValue()) {
                if (device.equals(surrogate)) {
                    devices.add(device.toString() + " (S)");
                } else {
                    devices.add(device.toString());
                }
            }
            groupSendersWithSurrogate.put(ga, devices);
        }
        return groupSendersWithSurrogate.toString();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here
    }

    public ScheduledExecutorService getScheduler() {
        return knxScheduler;
    }

    public ScheduledExecutorService getBackgroundScheduler() {
        return backgroundScheduler;
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
        publishOwnAddress(status);
    }

    @Override
    public void updateStatus(ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(statusDetail, description);
    }

    @Override
    public void updateStatus(ThingStatus status, @Nullable String description) {
        super.updateStatus(status, description);
        publishOwnAddress(status);
    }

    /** The address openHAB sends from is known once the connection is up, and may change with a reconnect. */
    private void publishOwnAddress(ThingStatus status) {
        if (status != ThingStatus.ONLINE) {
            return;
        }
        try {
            getOwnAddress().ifPresent(address -> updateProperty(OWN_ADDRESS, address.toString()));
        } catch (RuntimeException e) {
            logger.debug("Bridge {} could not publish its own address: {}", thing.getUID(), e.toString());
        }
    }
}
