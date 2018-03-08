/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.handler;

import java.net.InetSocketAddress;
import java.text.MessageFormat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.client.KNXClient;
import org.openhab.binding.knx.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.client.CustomKNXNetworkLinkIP;
import org.openhab.binding.knx.internal.client.IPClient;
import org.openhab.binding.knx.internal.client.NoOpClient;
import org.openhab.binding.knx.internal.config.IPBridgeConfiguration;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX/IP Gateway, that either acts a a
 * conduit for other {@link DeviceThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - Refactoring & cleanup
 */
@NonNullByDefault
public class IPBridgeThingHandler extends KNXBridgeBaseThingHandler {

    private static final String MODE_ROUTER = "ROUTER";
    private static final String MODE_TUNNEL = "TUNNEL";

    @Nullable
    private IPClient client;

    private final NetworkAddressService networkAddressService;

    public IPBridgeThingHandler(Bridge bridge, NetworkAddressService networkAddressService) {
        super(bridge);
        this.networkAddressService = networkAddressService;
    }

    @Override
    public void initialize() {
        IPBridgeConfiguration config = getConfigAs(IPBridgeConfiguration.class);
        String localSource = config.getLocalSourceAddr();
        String connectionTypeString = config.getType();
        int port = config.getPortNumber().intValue();
        String ip = config.getIpAddress();
        InetSocketAddress localEndPoint = null;
        boolean useNAT = false;
        int ipConnectionType;
        if (MODE_TUNNEL.equalsIgnoreCase(connectionTypeString)) {
            useNAT = config.getUseNAT() != null ? config.getUseNAT() : false;
            ipConnectionType = CustomKNXNetworkLinkIP.TUNNELING;
        } else if (MODE_ROUTER.equalsIgnoreCase(connectionTypeString)) {
            useNAT = false;
            if (ip == null || ip.isEmpty()) {
                ip = KNXBindingConstants.DEFAULT_MULTICAST_IP;
            }
            ipConnectionType = CustomKNXNetworkLinkIP.ROUTING;
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    MessageFormat.format("Unknown IP connection type {0}. Known types are either 'TUNNEL' or 'ROUTER'",
                            connectionTypeString));
            return;
        }
        if (ip == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "The 'ipAddress' of the gateway must be configured in 'TUNNEL' mode");
            return;
        }

        if (config.getLocalIp() != null && !config.getLocalIp().isEmpty()) {
            localEndPoint = new InetSocketAddress(config.getLocalIp(), 0);
        } else {
            localEndPoint = new InetSocketAddress(networkAddressService.getPrimaryIpv4HostAddress(), 0);
        }

        updateStatus(ThingStatus.UNKNOWN);
        client = new IPClient(ipConnectionType, ip, localSource, port, localEndPoint, useNAT,
                config.getAutoReconnectPeriod().intValue(), thing.getUID(), config.getResponseTimeout().intValue(),
                config.getReadingPause().intValue(), config.getReadRetriesLimit().intValue(), getScheduler(), this);

        client.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (client != null) {
            client.dispose();
            client = null;
        }
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
