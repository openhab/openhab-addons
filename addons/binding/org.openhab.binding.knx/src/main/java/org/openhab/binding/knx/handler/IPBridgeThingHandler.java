/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.knx.KNXBindingConstants;
import org.openhab.binding.knx.internal.handler.BridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tuwien.auto.calimero.IndividualAddress;
import tuwien.auto.calimero.exception.KNXException;
import tuwien.auto.calimero.link.KNXNetworkLink;
import tuwien.auto.calimero.link.KNXNetworkLinkIP;
import tuwien.auto.calimero.link.medium.TPSettings;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX/IP Gateway, that either acts a a
 * conduit for other {@link KNXGenericThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 */
public class IPBridgeThingHandler extends KNXBridgeBaseThingHandler {

    private static final String MODE_ROUTER = "ROUTER";
    private static final String MODE_TUNNEL = "TUNNEL";

    private final Logger logger = LoggerFactory.getLogger(IPBridgeThingHandler.class);

    private int ipConnectionType;
    private String ip;
    private String localSource;
    private int port;
    private InetSocketAddress localEndPoint;
    private Boolean useNAT;

    public IPBridgeThingHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        localSource = (String) getConfig().get(LOCAL_SOURCE_ADDRESS);

        String connectionTypeString = (String) getConfig().get(IP_CONNECTION_TYPE);
        if (StringUtils.isNotBlank(connectionTypeString)) {
            if (MODE_TUNNEL.equalsIgnoreCase(connectionTypeString)) {
                ip = (String) getConfig().get(IP_ADDRESS);
                port = ((BigDecimal) getConfig().get(PORT_NUMBER)).intValue();
                ipConnectionType = KNXNetworkLinkIP.TUNNELING;
                useNAT = getConfigAs(BridgeConfiguration.class).getUseNAT();
            } else if (MODE_ROUTER.equalsIgnoreCase(connectionTypeString)) {
                ipConnectionType = KNXNetworkLinkIP.ROUTING;
                if (StringUtils.isBlank(ip)) {
                    ip = KNXBindingConstants.DEFAULT_MULTICAST_IP;
                }
                if (StringUtils.isBlank(localSource)) {
                    localSource = KNXBindingConstants.DEFAULT_LOCAL_SOURCE_ADDRESS;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        MessageFormat.format(
                                "Unknown IP connection type {0}. Known types are either 'TUNNEL' or 'ROUTER'",
                                connectionTypeString));
                return;
            }
        } else {
            ipConnectionType = KNXNetworkLinkIP.TUNNELING;
        }

        try {
            if (!useNAT) {
                if (StringUtils.isNotBlank((String) getConfig().get(LOCAL_IP))) {
                    localEndPoint = new InetSocketAddress((String) getConfig().get(LOCAL_IP), 0);
                } else {
                    InetAddress localHost = InetAddress.getLocalHost();
                    localEndPoint = new InetSocketAddress(localHost, 0);
                }
            }
        } catch (UnknownHostException uhe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Couldn't find an IP address for this host. Please check the .hosts configuration or use the 'localIp' parameter to configure a valid IP address.");
            return;
        }

        super.initialize();
    }

    @Override
    public KNXNetworkLink establishConnection() throws KNXException, InterruptedException {
        logger.debug("Establishing connection to KNX bus on {}:{} in mode {}.", ip, port, connectionTypeToString());
        TPSettings settings = new TPSettings(new IndividualAddress(localSource));
        return new KNXNetworkLinkIP(ipConnectionType, localEndPoint, new InetSocketAddress(ip, port), useNAT, settings);

    }

    private String connectionTypeToString() {
        return ipConnectionType == KNXNetworkLinkIP.ROUTING ? MODE_ROUTER : MODE_TUNNEL;
    }
}
