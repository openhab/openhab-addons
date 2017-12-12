/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler;

import java.net.InetSocketAddress;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.net.NetworkAddressService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.internal.client.IPClient;
import org.openhab.binding.knx.internal.client.KNXClient;
import org.openhab.binding.knx.internal.config.IPBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.link.KNXNetworkLinkIP;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX/IP Gateway, that either acts a a
 * conduit for other {@link KNXBasicThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class IPBridgeThingHandler extends KNXBridgeBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IPBridgeThingHandler.class);

    private static final String MODE_ROUTER = "ROUTER";
    private static final String MODE_TUNNEL = "TUNNEL";

    @NonNullByDefault({})
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
        String connectionTypeString = config.getIpConnectionType();
        String ip = "";
        int port = 0;
        InetSocketAddress localEndPoint = null;
        boolean useNAT = false;
        int ipConnectionType = MODE_ROUTER.equalsIgnoreCase(connectionTypeString) ? KNXNetworkLinkIP.ROUTING
                : KNXNetworkLinkIP.TUNNELING;
        if (StringUtils.isNotBlank(connectionTypeString)) {
            if (MODE_TUNNEL.equalsIgnoreCase(connectionTypeString)) {
                ip = config.getIpAddress();
                port = config.getPortNumber().intValue();
                useNAT = config.getUseNAT() != null ? config.getUseNAT() : false;
            } else if (MODE_ROUTER.equalsIgnoreCase(connectionTypeString)) {
                useNAT = false;
                if (StringUtils.isBlank(ip)) {
                    ip = KNXBindingConstants.DEFAULT_MULTICAST_IP;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        MessageFormat.format(
                                "Unknown IP connection type {0}. Known types are either 'TUNNEL' or 'ROUTER'",
                                connectionTypeString));
                return;
            }
        }

        if (StringUtils.isNotBlank(config.getLocalIp())) {
            localEndPoint = new InetSocketAddress(config.getLocalIp(), 0);
        } else {
            localEndPoint = new InetSocketAddress(networkAddressService.getPrimaryIpv4HostAddress(), 0);
        }

        client = new IPClient(ipConnectionType, ip, localSource, port, localEndPoint, useNAT,
                config.getAutoReconnectPeriod().intValue(), thing.getUID(), config.getResponseTimeout().intValue(),
                config.getReadingPause().intValue(), config.getReadRetriesLimit().intValue(), getScheduler(), this);

        client.initialize();
        updateStatus(ThingStatus.UNKNOWN);
    }

    @Override
    public void dispose() {
        super.dispose();
        client.dispose();
    }

    @Override
    protected KNXClient getClient() {
        return client;
    }

}
