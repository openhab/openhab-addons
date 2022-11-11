/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.StatusUpdateCallback;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.knxnetip.SecureConnection;
import tuwien.auto.calimero.mgmt.Destination;
import tuwien.auto.calimero.secure.KnxSecureException;

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

    protected ConcurrentHashMap<IndividualAddress, Destination> destinations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService knxScheduler = ThreadPoolManager.getScheduledPool("knx");
    private final ScheduledExecutorService backgroundScheduler = Executors.newSingleThreadScheduledExecutor();
    protected SecureRoutingConfig secureRouting;
    protected SecureTunnelConfig secureTunnel;

    public KNXBridgeBaseThingHandler(Bridge bridge) {
        super(bridge);
        secureRouting = new SecureRoutingConfig();
        secureTunnel = new SecureTunnelConfig();
    }

    protected abstract KNXClient getClient();

    /***
     * Initialize KNX secure if configured (full interface)
     *
     * @param cRouterBackboneGroupKey shared key for secure router mode.
     * @param cTunnelDevAuth device password for IP interface in tunnel mode.
     * @param cTunnelUser user id for tunnel mode. Must be an integer >0.
     * @param cTunnelPassword user password for tunnel mode.
     * @return
     */
    protected boolean initializeSecurity(String cRouterBackboneGroupKey, String cTunnelDevAuth, String cTunnelUser,
            String cTunnelPassword) throws KnxSecureException {
        secureRouting = new SecureRoutingConfig();
        secureTunnel = new SecureTunnelConfig();

        boolean securityInitialized = false;

        // step 1: secure routing, backbone group key manually specified in OH config
        if (!cRouterBackboneGroupKey.isBlank()) {
            // provided in config
            String key = cRouterBackboneGroupKey.trim().replaceFirst("^0x", "").trim().replace(" ", "");
            if (!key.isEmpty()) {
                // helper may throw KnxSecureException
                secureRouting.backboneGroupKey = secHelperParseBackboneKey(key);
                securityInitialized = true;
            }
        }

        // step 2: check if valid tunnel parameters are specified in config
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
                throw new KnxSecureException("tunnelUser must be a number >0");
            }
            if (secureTunnel.user <= 0) {
                throw new KnxSecureException("tunnelUser must be a number >0");
            }
            securityInitialized = true;
        }

        // step 5: router: load latencyTolerance
        // default to 2000ms
        // this parameter is currently not exposed in config, it may later be set by using the keyring
        secureRouting.latencyToleranceMs = 2000;

        return securityInitialized;
    }

    /***
     * converts hex string (32 characters) to byte[16]
     *
     * @param hexstring 32 characters hex
     * @return key in byte array format
     */
    public static byte[] secHelperParseBackboneKey(String hexstring) throws KnxSecureException {
        if (hexstring.length() != 32) {
            throw new KnxSecureException("backbone key must be 32 characters (16 byte hex notation)");
        }

        byte[] parsed = new byte[16];
        try {
            for (byte i = 0; i < 16; i++) {
                parsed[i] = (byte) Integer.parseInt(hexstring.substring(2 * i, 2 * i + 2), 16);
            }
        } catch (NumberFormatException e) {
            throw new KnxSecureException("backbone key configured, cannot parse hex string, illegal character", e);
        }
        return parsed;
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
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }
}
