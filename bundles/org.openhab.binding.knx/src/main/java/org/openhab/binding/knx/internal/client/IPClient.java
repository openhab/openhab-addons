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
package org.openhab.binding.knx.internal.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.KNXMediumSettings;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * IP specific {@link AbstractKNXClient} implementation.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public class IPClient extends AbstractKNXClient {

    private final Logger logger = LoggerFactory.getLogger(IPClient.class);

    private static final String MODE_ROUTER = "ROUTER";
    private static final String MODE_TUNNEL = "TUNNEL";

    private final int ipConnectionType;
    private final String ip;
    private final String localSource;
    private final int port;
    @Nullable
    private final InetSocketAddress localEndPoint;
    private final boolean useNAT;

    public IPClient(int ipConnectionType, String ip, String localSource, int port,
            @Nullable InetSocketAddress localEndPoint, boolean useNAT, int autoReconnectPeriod, ThingUID thingUID,
            int responseTimeout, int readingPause, int readRetriesLimit, ScheduledExecutorService knxScheduler,
            StatusUpdateCallback statusUpdateCallback) {
        super(autoReconnectPeriod, thingUID, responseTimeout, readingPause, readRetriesLimit, knxScheduler,
                statusUpdateCallback);
        this.ipConnectionType = ipConnectionType;
        this.ip = ip;
        this.localSource = localSource;
        this.port = port;
        this.localEndPoint = localEndPoint;
        this.useNAT = useNAT;
    }

    @Override
    protected KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        logger.debug("Establishing connection to KNX bus on {}:{} in mode {}.", ip, port, connectionTypeToString());
        TPSettings settings = new TPSettings(new IndividualAddress(localSource));
        return createKNXNetworkLinkIP(ipConnectionType, localEndPoint, new InetSocketAddress(ip, port), useNAT,
                settings);
    }

    private String connectionTypeToString() {
        return ipConnectionType == CustomKNXNetworkLinkIP.ROUTING ? MODE_ROUTER : MODE_TUNNEL;
    }

    private KNXNetworkLinkIP createKNXNetworkLinkIP(int serviceMode, @Nullable InetSocketAddress localEP,
            @Nullable InetSocketAddress remoteEP, boolean useNAT, KNXMediumSettings settings)
            throws KNXException, InterruptedException {
        // creating the connection here as a workaround for
        // https://github.com/calimero-project/calimero-core/issues/57
        KNXnetIPConnection conn = getConnection(serviceMode, localEP, remoteEP, useNAT);
        return new CustomKNXNetworkLinkIP(serviceMode, conn, settings);
    }

    private KNXnetIPConnection getConnection(int serviceMode, @Nullable InetSocketAddress localEP,
            @Nullable InetSocketAddress remoteEP, boolean useNAT) throws KNXException, InterruptedException {
        KNXnetIPConnection conn;
        switch (serviceMode) {
            case CustomKNXNetworkLinkIP.TUNNELING:
                InetSocketAddress local = localEP;
                if (local == null) {
                    try {
                        local = new InetSocketAddress(InetAddress.getLocalHost(), 0);
                    } catch (final UnknownHostException e) {
                        throw new KNXException("no local host available");
                    }
                }
                conn = new KNXnetIPTunnel(TunnelingLayer.LinkLayer, local, remoteEP, useNAT);
                break;
            case CustomKNXNetworkLinkIP.ROUTING:
                NetworkInterface netIf = null;
                if (localEP != null && !localEP.isUnresolved()) {
                    try {
                        netIf = NetworkInterface.getByInetAddress(localEP.getAddress());
                    } catch (final SocketException e) {
                        throw new KNXException("error getting network interface: " + e.getMessage());
                    }
                }
                final InetAddress mcast = remoteEP != null ? remoteEP.getAddress() : null;
                conn = new KNXnetIPRouting(netIf, mcast);
                break;
            default:
                throw new KNXIllegalArgumentException("unknown service mode");
        }
        return conn;
    }
}
