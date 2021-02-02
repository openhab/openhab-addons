/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.souliss.handler;

import java.math.BigDecimal;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingSendDispatcherJob;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingUDPServerJob;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissGatewayHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayHandler.class);
    public @Nullable DatagramSocket datagramSocketDefaultPort;
    private @Nullable SoulissBindingUDPServerJob udpServerDefaultPortRunnableClass;
    private SoulissCommonCommands soulissCommands = new SoulissCommonCommands();

    boolean bGatewayDetected = false;

    private @NonNullByDefault({}) Configuration gwConfigurationMap;

    public int pingRefreshInterval;
    public int subscriptionRefreshInterval;
    public boolean thereIsAThingDetection = true;
    public int healthRefreshInterval;
    public int sendRefreshInterval;
    public int sendTimeoutToRequeue;
    public int sendTimeoutToRemovePacket;
    private Bridge bridge;
    public int preferredLocalPort;
    public int soulissGatewayPort;
    public byte userIndex;
    public byte nodeIndex;
    public String ipAddressOnLAN = "";
    private int nodes;
    private int maxTypicalXnode;
    private int countPingKo = 0;
    // private int maxnodes = 0;
    // private int maxrequests = 0;

    public SoulissGatewayHandler(Bridge _bridge) {
        super(_bridge);
        bridge = _bridge;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("initializing server handler for thingGeneric {}", getThing());

        gwConfigurationMap = bridge.getConfiguration();
        ipAddressOnLAN = (String) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS);
        if (gwConfigurationMap == null) {
            logger.debug("Gateway Handler - Error in Configuration Map - Thing= {}", getThing());
        } else {
            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT) != null) {
                preferredLocalPort = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT))
                        .intValue();
                logger.debug("Get Preferred Local Port: {}", preferredLocalPort);
            }

            if (preferredLocalPort < 0 && preferredLocalPort > 65000) {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_LOCAL_PORT, 0);
                logger.debug("Set Preferred Local Port to {}", 0);
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT) != null) {
                soulissGatewayPort = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT))
                        .intValue();
                logger.debug("Get Souliss Gateway Port: {}", soulissGatewayPort);
            }
            if (soulissGatewayPort < 0 && soulissGatewayPort > 65000)

            {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_PORT,
                        SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
                logger.debug("Set Souliss Gateway Port to {}", SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX) != null)

            {
                userIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX))
                        .byteValue();
                logger.debug("Get User Index: {}", userIndex);
                if (userIndex < 0 && userIndex > 255) {
                    bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_USER_INDEX,
                            SoulissBindingUDPConstants.SOULISS_DEFAULT_USER_INDEX);
                    logger.debug("Set User Index to {}", SoulissBindingUDPConstants.SOULISS_DEFAULT_USER_INDEX);
                }
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
                nodeIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX))
                        .byteValue();
                logger.debug("Get Node Index: {}", nodeIndex);
            }
            if (nodeIndex < 0 && nodeIndex > 255) {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_NODE_INDEX,
                        SoulissBindingUDPConstants.SOULISS_DEFAULT_NODE_INDEX);
                logger.debug("Set Node Index to {}", SoulissBindingUDPConstants.SOULISS_DEFAULT_NODE_INDEX);
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
                nodeIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX))
                        .byteValue();
                logger.debug("Get Node Index: {}", nodeIndex);
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PING_REFRESH) != null) {
                pingRefreshInterval = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PING_REFRESH))
                        .intValue();
                logger.debug("Get ping refresh interval: {}", pingRefreshInterval);
            }
            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SUBSCRIPTION_REFRESH) != null) {
                subscriptionRefreshInterval = ((BigDecimal) gwConfigurationMap
                        .get(SoulissBindingConstants.CONFIG_SUBSCRIPTION_REFRESH)).intValue();
                logger.debug("Get ping refresh interval: {}", pingRefreshInterval);
            }
            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_HEALTHY_REFRESH) != null) {
                healthRefreshInterval = ((BigDecimal) gwConfigurationMap
                        .get(SoulissBindingConstants.CONFIG_HEALTHY_REFRESH)).intValue();
                logger.debug("Get health refresh interval: {}", healthRefreshInterval);
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SEND_REFRESH) != null) {
                sendRefreshInterval = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SEND_REFRESH))
                        .intValue();
                logger.debug("Get send refresh interval: {}", sendRefreshInterval);
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REQUEUE) != null) {
                sendTimeoutToRequeue = ((BigDecimal) gwConfigurationMap
                        .get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REQUEUE)).intValue();
                logger.debug("Get send timeout to requeue: {}", sendTimeoutToRequeue);
            }

            if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REMOVE_PACKET) != null) {
                sendTimeoutToRemovePacket = ((BigDecimal) gwConfigurationMap
                        .get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REMOVE_PACKET)).intValue();
                logger.debug("Get send timeout to requeue: {}", sendTimeoutToRequeue);
            }

            // START SERVER ON DEFAULT PORT - Used for topics
            if (udpServerDefaultPortRunnableClass == null) {
                logger.debug("Starting UDP server on Souliss Default Port for Topics (Publish&Subcribe)");
                datagramSocketDefaultPort = SoulissDatagramSocketFactory.getSocketDatagram(soulissGatewayPort,
                        this.logger);
                if (datagramSocketDefaultPort != null) {
                    udpServerDefaultPortRunnableClass = new SoulissBindingUDPServerJob(datagramSocketDefaultPort,
                            SoulissBindingNetworkParameters.discoverResult);
                    // Changes from scheduleAtFixedRate - Luca Calcaterra
                    scheduler.scheduleWithFixedDelay(udpServerDefaultPortRunnableClass, 100,
                            SoulissBindingConstants.SERVER_CICLE_IN_MILLIS, TimeUnit.MILLISECONDS);
                }
            }

            // START JOB PING

            SoulissGatewayJobPing soulissGatewayJobPingRunnable = new SoulissGatewayJobPing(bridge);
            // Changes from scheduleAtFixedRate - Luca Calcaterra
            scheduler.scheduleWithFixedDelay(soulissGatewayJobPingRunnable, 2,
                    soulissGatewayJobPingRunnable.getPingRefreshInterval(), TimeUnit.SECONDS);

            SoulissGatewayJobSubscription soulissGatewayJobSubscriptionRunnable = new SoulissGatewayJobSubscription(
                    bridge);
            // Changes from scheduleAtFixedRate - Luca Calcaterra
            scheduler.scheduleWithFixedDelay(soulissGatewayJobSubscriptionRunnable, 0,
                    soulissGatewayJobSubscriptionRunnable.getSubscriptionRefreshInterval(), TimeUnit.MINUTES);

            SoulissGatewayJobHealty soulissGatewayJobHealtyRunnable = new SoulissGatewayJobHealty(bridge);
            // Changes from scheduleAtFixedRate - Luca Calcaterra
            scheduler.scheduleWithFixedDelay(soulissGatewayJobHealtyRunnable, 5,
                    soulissGatewayJobHealtyRunnable.gethealthRefreshInterval(), TimeUnit.SECONDS);

            // il ciclo Send è schedulato con la costante
            // SoulissBindingConstants.SEND_DISPATCHER_MIN_DELAY_cicleInMillis
            // internamente il ciclo viene rallentato al timer impostato da configurazione (PaperUI o File)
            SoulissBindingSendDispatcherJob soulissSendDispatcherRunnable = new SoulissBindingSendDispatcherJob(bridge);
            scheduler.scheduleWithFixedDelay(soulissSendDispatcherRunnable, 15,
                    SoulissBindingConstants.SEND_DISPATCHER_MIN_DELAY_CYCLE_IN_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private int gwIpByte() {
        return Integer.parseInt(ipAddressOnLAN.split("\\.")[3]);
    }

    @Override
    public void handleRemoval() {
        // if (ipAddressOnLAN != null) {
        SoulissBindingNetworkParameters.removeGateway((byte) gwIpByte());
        // udpServerDefaultPortRunnableClass = null;
        logger.debug("Gateway handler removing");
        // }
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("Thing Updated: {}", thing.getThingTypeUID());
        // if (ipAddressOnLAN != null) {
        SoulissBindingNetworkParameters.removeGateway((byte) gwIpByte());
        // .removeGateway((byte) (Byte.parseByte((ipAddressOnLAN.split("\\.")[3]) & (byte) 0xFF));
        // }
        this.thing = thing;
    }

    public void dbStructAnswerReceived() {
        soulissCommands.sendTypicalRequestFrame(SoulissBindingNetworkParameters.getDatagramSocket(), ipAddressOnLAN,
                nodeIndex, userIndex, nodes);
    }

    /*
     * public String getGatewayIP() {
     * bridge = (Bridge) thingGeneric.getBridgeUID();
     * if (thingGeneric.getBridgeUID() != null) {
     * return ((SoulissGatewayHandler) (Bridge) this.getThingByUID(bridge.getUID()).getHandler()).ipAddressOnLAN;
     * }
     * return null;
     * }
     */

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    int iPosNodeSlot = 2;

    public int getNodes() {
        Thing thing;
        int maxNode = 0;
        Iterator<Thing> iterator = bridge.getThings().iterator();
        while (iterator.hasNext()) {
            thing = iterator.next();
            String[] uuidStrings = thing.getUID().getAsString().split(SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR);
            String[] uuidNodeNumber = uuidStrings[0].split(SoulissBindingConstants.UUID_ELEMENTS_SEPARATOR);

            iPosNodeSlot = 2; // if uuid is of type souliss:gateway:[typical]:[node]-[slot] then node/slot is at
                              // position 2
            if (uuidNodeNumber.length > 3) {
                iPosNodeSlot = 3;
            }
            if (Integer.parseInt(uuidNodeNumber[iPosNodeSlot]) > maxNode) {
                maxNode = Integer.parseInt(uuidNodeNumber[iPosNodeSlot]);
            }
            // alla fine la lunghezza della lista sarà uguale al numero di nodi presenti
        }
        return maxNode + 1;
    }

    // public void setMaxnodes(int maxnodes) {
    // this.maxnodes = maxnodes;
    // }

    public void setMaxTypicalXnode(int maxTypicalXnode) {
        this.maxTypicalXnode = maxTypicalXnode;
    }

    // public void setMaxrequests(int maxrequests) {
    // this.maxrequests = maxrequests;
    // }

    public int getMaxTypicalXnode() {
        return maxTypicalXnode;
    }

    /**
     * The {@link gatewayDetected} is used to notify that UDPServer decoded a Ping Response from gateway
     *
     * @author Tonino Fazio - Initial contribution
     * @author Luca Calcaterra - Refactor for OH3
     */

    public void gatewayDetected() {
        logger.debug("Setting Gateway ONLINE");
        try {
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Illegal status transition to ONLINE");
        }
        countPingKo = 0; // reset counter
    }

    @SuppressWarnings("null")
    public void pingSent() {
        if (++countPingKo > 3) {
            // if GW do not respond to ping it is setted to OFFLINE
            logger.debug("Gateway do not respond to {} ping packet - setting OFFLINE", countPingKo);
            if (bridge.getHandler() != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Gateway "
                        + bridge.getHandler().getThing().getUID() + " do not respond to " + countPingKo + " ping");
            }
        }
    }

    public void sendSubscription() {
        // logger.debug("Sending subscription packet");
        if ( /* ipAddressOnLAN != null && */ ipAddressOnLAN.length() > 0) {
            soulissCommands.sendSUBSCRIPTIONframe(SoulissBindingNetworkParameters.getDatagramSocket(), ipAddressOnLAN,
                    nodeIndex, userIndex, getNodes());
        }
        logger.debug("Sent subscription packet");
    }

    public void setThereIsAThingDetection() {
        thereIsAThingDetection = true;
    }

    public void resetThereIsAThingDetection() {
        thereIsAThingDetection = false;
    }
}
