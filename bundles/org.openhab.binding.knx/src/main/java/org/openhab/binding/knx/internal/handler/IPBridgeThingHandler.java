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
package org.openhab.binding.knx.internal.handler;

import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.binding.knx.internal.client.IPClient;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.client.NoOpClient;
import org.openhab.binding.knx.internal.config.IPBridgeConfiguration;
import org.openhab.binding.knx.internal.i18n.KNXTranslationProvider;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.secure.KnxSecureException;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX/IP Gateway, that either acts as a
 * conduit for other {@link DeviceThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - Refactoring and cleanup
 */
@NonNullByDefault
public class IPBridgeThingHandler extends KNXBridgeBaseThingHandler {
    private static final String MODE_ROUTER = "ROUTER";
    private static final String MODE_TUNNEL = "TUNNEL";
    private static final String MODE_SECURE_ROUTER = "SECUREROUTER";
    private static final String MODE_SECURE_TUNNEL = "SECURETUNNEL";
    private @Nullable Future<?> initJob = null;

    private final Logger logger = LoggerFactory.getLogger(IPBridgeThingHandler.class);

    private @Nullable IPClient client = null;
    private @Nullable final NetworkAddressService networkAddressService;

    public IPBridgeThingHandler(Bridge bridge, @Nullable NetworkAddressService networkAddressService) {
        super(bridge);
        this.networkAddressService = networkAddressService;
    }

    @Override
    public void initialize() {
        // initialization would take too long and show a warning during binding startup
        // KNX secure is adding serious delay
        updateStatus(ThingStatus.UNKNOWN);
        initJob = scheduler.submit(this::initializeLater);
    }

    public void initializeLater() {
        IPBridgeConfiguration config = getConfigAs(IPBridgeConfiguration.class);
        boolean securityAvailable = false;
        try {
            securityAvailable = initializeSecurity(config.getKeyringFile(), config.getKeyringPassword(),
                    config.getRouterBackboneKey(), config.getTunnelDeviceAuthentication(), config.getTunnelUserId(),
                    config.getTunnelUserPassword(), config.getTunnelSourceAddress());
            if (securityAvailable) {
                logger.debug("KNX secure: router backboneGroupKey is {} set",
                        ((secureRouting.backboneGroupKey.length == 16) ? "properly" : "not"));
                boolean tunnelOk = ((secureTunnel.user > 0) && (secureTunnel.devKey.length == 16)
                        && (secureTunnel.userKey.length == 16));
                logger.debug("KNX secure: tunnel keys are {} set", (tunnelOk ? "properly" : "not"));

                if (keyring.isPresent()) {
                    logger.debug("KNX secure available for {} devices, {} group addresses",
                            openhabSecurity.deviceToolKeys().size(), openhabSecurity.groupKeys().size());

                    logger.debug("Secure group addresses and associated devices: {}",
                            secHelperGetSecureGroupAddresses(openhabSecurity));
                } else {
                    logger.debug("KNX secure: keyring is not available");
                }
            } else {
                logger.debug("KNX security not configured");
            }
        } catch (KnxSecureException e) {
            logger.debug("{}, {}", thing.getUID(), e.toString());

            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    KNXTranslationProvider.I18N.getLocalizedException(cause));
            return;
        }

        int autoReconnectPeriod = config.getAutoReconnectPeriod();
        if (autoReconnectPeriod != 0 && autoReconnectPeriod < 30) {
            logger.info("autoReconnectPeriod for {} set to {}s, allowed range is 0 (never) or >30", thing.getUID(),
                    autoReconnectPeriod);
            autoReconnectPeriod = 30;
            config.setAutoReconnectPeriod(autoReconnectPeriod);
        }
        String localSource = config.getLocalSourceAddr();
        String connectionTypeString = config.getType();
        int port = config.getPortNumber();
        String ip = config.getIpAddress();
        InetSocketAddress localEndPoint = null;
        boolean useNAT = false;

        IPClient.IpConnectionType ipConnectionType;
        if (MODE_TUNNEL.equalsIgnoreCase(connectionTypeString)) {
            useNAT = config.getUseNAT();
            ipConnectionType = IPClient.IpConnectionType.TUNNEL;
        } else if (MODE_SECURE_TUNNEL.equalsIgnoreCase(connectionTypeString)) {
            useNAT = config.getUseNAT();
            ipConnectionType = IPClient.IpConnectionType.SECURE_TUNNEL;

            if (!securityAvailable) {
                logger.warn("Bridge {} missing security configuration for secure tunnel", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.knx-secure-tunnel-config-missing");
                return;
            }
            boolean tunnelOk = ((secureTunnel.user > 0) && (secureTunnel.devKey.length == 16)
                    && (secureTunnel.userKey.length == 16));
            if (!tunnelOk) {
                logger.warn("Bridge {} incomplete security configuration for secure tunnel", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.knx-secure-tunnel-config-incomplete");
                return;
            }

            logger.debug("KNX secure tunneling needs a few seconds to establish connection");
            // user id, key, devAuth are already stored
        } else if (MODE_ROUTER.equalsIgnoreCase(connectionTypeString)) {
            useNAT = false;
            if (ip.isEmpty()) {
                ip = KNXBindingConstants.DEFAULT_MULTICAST_IP;
            }
            ipConnectionType = IPClient.IpConnectionType.ROUTER;
        } else if (MODE_SECURE_ROUTER.equalsIgnoreCase(connectionTypeString)) {
            useNAT = false;
            if (ip.isEmpty()) {
                ip = KNXBindingConstants.DEFAULT_MULTICAST_IP;
            }
            ipConnectionType = IPClient.IpConnectionType.SECURE_ROUTER;

            if (!securityAvailable) {
                logger.warn("Bridge {} missing security configuration for secure routing", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.knx-secure-routing-config-missing");
                return;
            }
            if (secureRouting.backboneGroupKey.length != 16) {
                // failed to read shared backbone group key from config or keyring
                logger.warn("Bridge {} invalid security configuration for secure routing", thing.getUID());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/error.knx-secure-routing-backbonegroupkey-invalid");
                return;
            }
            logger.debug("KNX secure routing needs a few seconds to establish connection");
        } else {
            logger.debug("Bridge {} unknown connection type", thing.getUID());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    MessageFormat.format("@text/knx-unknown-ip-connection-type", connectionTypeString));
            return;
        }

        if (!config.getLocalIp().isEmpty()) {
            localEndPoint = new InetSocketAddress(config.getLocalIp(), 0);
        } else {
            NetworkAddressService localNetworkAddressService = networkAddressService;
            if (localNetworkAddressService == null) {
                logger.debug("NetworkAddressService not available, cannot create bridge {}", thing.getUID());
                updateStatus(ThingStatus.OFFLINE);
                return;
            } else {
                localEndPoint = new InetSocketAddress(localNetworkAddressService.getPrimaryIpv4HostAddress(), 0);
            }
        }

        updateStatus(ThingStatus.UNKNOWN);
        client = new IPClient(ipConnectionType, ip, localSource, port, localEndPoint, useNAT, autoReconnectPeriod,
                secureRouting.backboneGroupKey, secureRouting.latencyToleranceMs, secureTunnel.devKey,
                secureTunnel.user, secureTunnel.userKey, thing.getUID(), config.getResponseTimeout(),
                config.getReadingPause(), config.getReadRetriesLimit(), getScheduler(), getCommandExtensionData(),
                openhabSecurity, this);

        IPClient tmpClient = client;
        if (tmpClient != null) {
            tmpClient.initialize();
        }

        logger.trace("Bridge {} completed KNX scheduled initialization", thing.getUID());
    }

    @Override
    public void dispose() {
        Future<?> tmpInitJob = initJob;
        if (tmpInitJob != null) {
            if (!tmpInitJob.isDone()) {
                logger.trace("Bridge {}, shutdown during init, trying to cancel", thing.getUID());
                tmpInitJob.cancel(true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    logger.trace("Bridge {}, cancellation interrupted", thing.getUID());
                }
            }
            initJob = null;
        }
        IPClient tmpClient = client;
        if (tmpClient != null) {
            tmpClient.dispose();
            client = null;
        }
        super.dispose();
    }

    @Override
    protected KNXClient getClient() {
        KNXClient ret = client;
        if (ret == null) {
            return new NoOpClient();
        }
        return ret;
    }
}
