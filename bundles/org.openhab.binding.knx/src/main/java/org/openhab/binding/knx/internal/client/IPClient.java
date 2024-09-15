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
package org.openhab.binding.knx.internal.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler.CommandExtensionData;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.KNXException;
import tuwien.auto.calimero.KNXIllegalArgumentException;
import tuwien.auto.calimero.knxnetip.KNXnetIPConnection;
import tuwien.auto.calimero.knxnetip.KNXnetIPRouting;
import tuwien.auto.calimero.knxnetip.KNXnetIPTunnel;
import tuwien.auto.calimero.knxnetip.KNXnetIPTunnel.TunnelingLayer;
import tuwien.auto.calimero.knxnetip.SecureConnection;
import tuwien.auto.calimero.knxnetip.TcpConnection;
import tuwien.auto.calimero.knxnetip.TcpConnection.SecureSession;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.TPSettings;
import tuwien.auto.calimero.secure.Security;

/**
 * IP specific {@link AbstractKNXClient} implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class IPClient extends AbstractKNXClient {

    public enum IpConnectionType {
        TUNNEL,
        ROUTER,
        SECURE_TUNNEL,
        SECURE_ROUTER
    }

    private final Logger logger = LoggerFactory.getLogger(IPClient.class);

    private static final String MODE_ROUTER = "ROUTER";
    private static final String MODE_TUNNEL = "TUNNEL";
    private static final String MODE_SECURE_ROUTER = "SECURE ROUTER";
    private static final String MODE_SECURE_TUNNEL = "SECURE TUNNEL";
    private static final long PAUSE_ON_TCP_SESSION_CLOSE_MS = 1000;

    private final IpConnectionType ipConnectionType;
    private final String ip;
    private final String localSource;
    private final int port;
    @Nullable
    private final InetSocketAddress localEndPoint;
    private final boolean useNAT;
    private final byte[] secureRoutingBackboneGroupKey;
    private final long secureRoutingLatencyToleranceMs;
    private final byte[] secureTunnelDevKey;
    private final int secureTunnelUser;
    private final byte[] secureTunnelUserKey;
    private final ThingUID thingUID;

    @Nullable
    SecureSession tcpSession;

    public IPClient(IpConnectionType ipConnectionType, String ip, String localSource, int port,
            @Nullable InetSocketAddress localEndPoint, boolean useNAT, int autoReconnectPeriod,
            byte[] secureRoutingBackboneGroupKey, long secureRoutingLatencyToleranceMs, byte[] secureTunnelDevKey,
            int secureTunnelUser, byte[] secureTunnelUserKey, ThingUID thingUID, int responseTimeout, int readingPause,
            int readRetriesLimit, ScheduledExecutorService knxScheduler, CommandExtensionData commandExtensionData,
            Security openhabSecurity, StatusUpdateCallback statusUpdateCallback) {
        super(autoReconnectPeriod, thingUID, responseTimeout, readingPause, readRetriesLimit, knxScheduler,
                commandExtensionData, openhabSecurity, statusUpdateCallback);
        this.ipConnectionType = ipConnectionType;
        this.ip = ip;
        this.localSource = localSource;
        this.port = port;
        this.localEndPoint = localEndPoint;
        this.useNAT = useNAT;
        this.secureRoutingBackboneGroupKey = secureRoutingBackboneGroupKey;
        this.secureRoutingLatencyToleranceMs = secureRoutingLatencyToleranceMs;
        this.secureTunnelDevKey = secureTunnelDevKey;
        this.secureTunnelUser = secureTunnelUser;
        this.secureTunnelUserKey = secureTunnelUserKey;
        this.thingUID = thingUID;
        tcpSession = null;
    }

    @Override
    protected KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        logger.debug("Establishing connection to KNX bus on {}:{} in mode {}.", ip, port, connectionTypeToString());
        TPSettings settings = new TPSettings(new IndividualAddress(localSource));
        return createKNXNetworkLinkIP(ipConnectionType, localEndPoint, new InetSocketAddress(ip, port), useNAT,
                settings);
    }

    private String connectionTypeToString() {
        if (ipConnectionType == IpConnectionType.ROUTER) {
            return MODE_ROUTER;
        }
        if (ipConnectionType == IpConnectionType.TUNNEL) {
            return MODE_TUNNEL;
        }
        if (ipConnectionType == IpConnectionType.SECURE_ROUTER) {
            return MODE_SECURE_ROUTER;
        }
        if (ipConnectionType == IpConnectionType.SECURE_TUNNEL) {
            return MODE_SECURE_TUNNEL;
        }
        return "unknown connection type";
    }

    private KNXNetworkLinkIP createKNXNetworkLinkIP(IpConnectionType ipConnectionType,
            @Nullable InetSocketAddress localEP, @Nullable InetSocketAddress remoteEP, boolean useNAT,
            KNXMediumSettings settings) throws KNXException, InterruptedException {
        // Calimero service mode, ROUTING for both classic and secure routing
        int serviceMode = CustomKNXNetworkLinkIP.ROUTING;
        if (ipConnectionType == IpConnectionType.TUNNEL) {
            serviceMode = CustomKNXNetworkLinkIP.TUNNELING;
        } else if (ipConnectionType == IpConnectionType.SECURE_TUNNEL) {
            serviceMode = CustomKNXNetworkLinkIP.TUNNELINGV2;
        }

        // creating the connection here as a workaround for
        // https://github.com/calimero-project/calimero-core/issues/57
        KNXnetIPConnection conn = getConnection(ipConnectionType, localEP, remoteEP, useNAT);
        return new CustomKNXNetworkLinkIP(serviceMode, conn, settings);
    }

    private KNXnetIPConnection getConnection(IpConnectionType ipConnectionType, @Nullable InetSocketAddress localEP,
            @Nullable InetSocketAddress remoteEP, boolean useNAT) throws KNXException, InterruptedException {
        KNXnetIPConnection conn;
        switch (ipConnectionType) {
            case TUNNEL:
            case SECURE_TUNNEL:
                InetSocketAddress local = localEP;
                if (local == null) {
                    try {
                        local = new InetSocketAddress(InetAddress.getLocalHost(), 0);
                    } catch (final UnknownHostException e) {
                        throw new KNXException("no local host available");
                    }
                }
                if (ipConnectionType == IpConnectionType.SECURE_TUNNEL) {
                    logger.trace("creating new TCP connection");
                    if (tcpSession != null) {
                        logger.debug("tcpSession might still be open");
                    }
                    // using .clone for the keys is essential - otherwise Calimero clears the array and a reconnect will
                    // fail
                    tcpSession = TcpConnection.newTcpConnection(localEP, remoteEP).newSecureSession(secureTunnelUser,
                            secureTunnelUserKey.clone(), secureTunnelDevKey.clone());
                    conn = SecureConnection.newTunneling(TunnelingLayer.LinkLayer, tcpSession,
                            new IndividualAddress(localSource));
                } else {
                    conn = new KNXnetIPTunnel(TunnelingLayer.LinkLayer, local, remoteEP, useNAT);
                }
                break;
            case ROUTER:
            case SECURE_ROUTER:
                NetworkInterface netIf = null;
                if (localEP != null && !localEP.isUnresolved()) {
                    try {
                        netIf = NetworkInterface.getByInetAddress(localEP.getAddress());
                    } catch (final SocketException e) {
                        throw new KNXException("error getting network interface: " + e.getMessage());
                    }
                }
                final InetAddress mcast = remoteEP != null ? remoteEP.getAddress() : null;
                if (ipConnectionType == IpConnectionType.SECURE_ROUTER) {
                    conn = SecureConnection.newRouting(netIf, mcast, secureRoutingBackboneGroupKey,
                            Duration.ofMillis(secureRoutingLatencyToleranceMs));
                } else {
                    conn = new KNXnetIPRouting(netIf, mcast);
                }
                break;
            default:
                throw new KNXIllegalArgumentException("unknown service mode");
        }
        return conn;
    }

    private void closeTcpConnection() {
        final SecureSession toBeClosed = tcpSession;
        if (toBeClosed != null) {
            tcpSession = null;
            logger.debug("Bridge {} closing TCP connection", thingUID);
            try {
                toBeClosed.close();
                try {
                    Thread.sleep(PAUSE_ON_TCP_SESSION_CLOSE_MS);
                } catch (InterruptedException e) {
                }
            } catch (Exception e) {
                logger.debug("closing TCP connection failed: {}", e.getMessage());
            }
        }
    }

    @Override
    protected void releaseConnection() {
        closeTcpConnection();
        super.releaseConnection();
    }
}
