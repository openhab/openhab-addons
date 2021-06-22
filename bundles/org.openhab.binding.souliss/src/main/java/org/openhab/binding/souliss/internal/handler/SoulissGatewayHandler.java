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
package org.openhab.binding.souliss.internal.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.config.GatewayConfig;
import org.openhab.binding.souliss.internal.discovery.SoulissGatewayDiscovery;
import org.openhab.binding.souliss.internal.protocol.CommonCommands;
import org.openhab.binding.souliss.internal.protocol.NetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SendDispatcherRunnable;
import org.openhab.binding.souliss.internal.protocol.UDPListenDiscoverRunnable;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.BridgeHandler;
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

    private final Logger logger = LoggerFactory.getLogger(SoulissGatewayHandler.class);

    private ExecutorService udpExecutorService = Executors
            .newSingleThreadExecutor(new NamedThreadFactory("binding-souliss"));

    private @Nullable UDPListenDiscoverRunnable udpServerDefaultPortRunnableClass;
    private @Nullable Future<?> eventListenerJob;

    private CommonCommands soulissCommands = new CommonCommands();

    boolean bGatewayDetected = false;

    private @Nullable SoulissGatewayDiscovery discoveryService;

    // private Configuration gwConfigurationMap = new Configuration();

    // public int pingRefreshInterval;
    // public int subscriptionRefreshInterval;
    public boolean thereIsAThingDetection = true;
    // public int healthRefreshInterval;
    // public int sendRefreshInterval;
    // public int sendTimeoutToRequeue;
    // public int sendTimeoutToRemovePacket;
    private Bridge bridge;
    // public int preferredLocalPort;
    // public int soulissGatewayPort;
    // public byte userIndex;
    // public byte nodeIndex;
    // public String ipAddressOnLAN = "";
    private int nodes;
    private int maxTypicalXnode;
    private int countPingKo = 0;

    public GatewayConfig gwConfig = new GatewayConfig();

    public SoulissGatewayHandler(Bridge br) {
        super(br);
        bridge = br;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {

        gwConfig = getConfigAs(GatewayConfig.class);

        // gwConfigurationMap = bridge.getConfiguration();
        // ipAddressOnLAN = (String) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS);
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT) != null) {
        // preferredLocalPort = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT))
        // .intValue();
        // logger.debug("Get Preferred Local Port: {}", preferredLocalPort);
        // }
        //
        // if (preferredLocalPort < 0 && preferredLocalPort > 65000) {
        // // local port to 0
        // bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_LOCAL_PORT, 0);
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT) != null) {
        // soulissGatewayPort = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT)).intValue();
        // logger.debug("Get Souliss Gateway Port: {}", soulissGatewayPort);
        // }
        // if (soulissGatewayPort < 0 && soulissGatewayPort > 65000) {
        // bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_PORT,
        // SoulissUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
        // logger.debug("Set Souliss Gateway Port to {}", SoulissUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX) != null) {
        // userIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX)).byteValue();
        // logger.debug("Get User Index: {}", userIndex);
        // if (userIndex < 0 && userIndex > 255) {
        // bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_USER_INDEX,
        // SoulissUDPConstants.SOULISS_DEFAULT_USER_INDEX);
        // logger.debug("Set User Index to {}", SoulissUDPConstants.SOULISS_DEFAULT_USER_INDEX);
        // }
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
        // nodeIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX)).byteValue();
        // logger.debug("Get Node Index: {}", nodeIndex);
        // }
        // if (nodeIndex < 0 && nodeIndex > 255) {
        // bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_NODE_INDEX,
        // SoulissUDPConstants.SOULISS_DEFAULT_NODE_INDEX);
        // logger.debug("Set Node Index to {}", SoulissUDPConstants.SOULISS_DEFAULT_NODE_INDEX);
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
        // nodeIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX)).byteValue();
        // logger.debug("Get Node Index: {}", nodeIndex);
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PING_REFRESH) != null) {
        // pingRefreshInterval = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PING_REFRESH))
        // .intValue();
        // logger.debug("Get ping refresh interval: {}", pingRefreshInterval);
        // }
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SUBSCRIPTION_REFRESH) != null) {
        // subscriptionRefreshInterval = ((BigDecimal) gwConfigurationMap
        // .get(SoulissBindingConstants.CONFIG_SUBSCRIPTION_REFRESH)).intValue();
        // logger.debug("Get ping refresh interval: {}", pingRefreshInterval);
        // }
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_HEALTHY_REFRESH) != null) {
        // healthRefreshInterval = ((BigDecimal) gwConfigurationMap
        // .get(SoulissBindingConstants.CONFIG_HEALTHY_REFRESH)).intValue();
        // logger.debug("Get health refresh interval: {}", healthRefreshInterval);
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SEND_REFRESH) != null) {
        // sendRefreshInterval = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_SEND_REFRESH))
        // .intValue();
        // logger.debug("Get send refresh interval: {}", sendRefreshInterval);
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REQUEUE) != null) {
        // sendTimeoutToRequeue = ((BigDecimal) gwConfigurationMap
        // .get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REQUEUE)).intValue();
        // logger.debug("Get send timeout to requeue: {}", sendTimeoutToRequeue);
        // }
        //
        // if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REMOVE_PACKET) != null) {
        // sendTimeoutToRemovePacket = ((BigDecimal) gwConfigurationMap
        // .get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REMOVE_PACKET)).intValue();
        // logger.debug("Get send timeout to requeue: {}", sendTimeoutToRequeue);
        // }

        if (udpServerDefaultPortRunnableClass == null) {
            logger.debug("Starting UDP server on Souliss Default Port for Topics (Publish&Subcribe)");

            // new runnable udp listener
            udpServerDefaultPortRunnableClass = new UDPListenDiscoverRunnable(NetworkParameters.discoverResult);
            // and exec on thread
            this.udpExecutorService.execute(udpServerDefaultPortRunnableClass);

            // JOB PING
            SoulissGatewayJobPing soulissGatewayJobPingRunnable = new SoulissGatewayJobPing(bridge);
            scheduler.scheduleWithFixedDelay(soulissGatewayJobPingRunnable, 2,
                    soulissGatewayJobPingRunnable.getPingRefreshInterval(), TimeUnit.SECONDS);
            // JOB SUBSCRIPTION
            SoulissGatewayJobSubscription soulissGatewayJobSubscriptionRunnable = new SoulissGatewayJobSubscription(
                    bridge);
            scheduler.scheduleWithFixedDelay(soulissGatewayJobSubscriptionRunnable, 0,
                    soulissGatewayJobSubscriptionRunnable.getSubscriptionRefreshInterval(), TimeUnit.MINUTES);

            // JOB HEALTH OF NODES
            SoulissGatewayJobHealthy soulissGatewayJobHealthyRunnable = new SoulissGatewayJobHealthy(bridge);
            scheduler.scheduleWithFixedDelay(soulissGatewayJobHealthyRunnable, 5,
                    soulissGatewayJobHealthyRunnable.gethealthRefreshInterval(), TimeUnit.SECONDS);

            // il ciclo Send è schedulato con la costante
            // SoulissBindingConstants.SEND_DISPATCHER_MIN_DELAY_cicleInMillis
            // internamente il ciclo viene rallentato al timer impostato da configurazione (PaperUI o File)
            SendDispatcherRunnable soulissSendDispatcherRunnable = new SendDispatcherRunnable(bridge);
            scheduler.scheduleWithFixedDelay(soulissSendDispatcherRunnable, 15,
                    SoulissBindingConstants.SEND_DISPATCHER_MIN_DELAY_CYCLE_IN_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private byte getGwIpByte() throws Exception {
        if (this.gwConfig.gatewayIpAddress != null) {
            return (byte) Integer.parseInt(this.gwConfig.gatewayIpAddress.split("\\.")[3]);
        }
        throw new Exception("ip gateway null!");
    }

    @Override
    public void handleRemoval() {
        try {
            NetworkParameters.removeGateway(getGwIpByte());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.warn("Ip Gateway null on HandleRemoval: {}", e.getLocalizedMessage());
        }
        logger.debug("Gateway handler removing");
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("Thing Updated: {}", thing.getThingTypeUID());
        try {
            NetworkParameters.removeGateway(getGwIpByte());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.warn("Ip Gateway null on ThingUpdated: {}", e.getLocalizedMessage());
        }
        this.thing = thing;
    }

    public void dbStructAnswerReceived() {
        if (this.gwConfig.gatewayIpAddress != null) {
            soulissCommands.sendTypicalRequestFrame(this.gwConfig.gatewayIpAddress, (byte) this.gwConfig.nodeIndex,
                    (byte) this.gwConfig.userIndex, nodes);
        }
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    int iPosNodeSlot = 2;

    public int getNodes() {
        int maxNode = 0;
        for (Thing thing : bridge.getThings()) {
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

    public void setMaxTypicalXnode(int maxTypicalXnode) {
        this.maxTypicalXnode = maxTypicalXnode;
    }

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
        updateStatus(ThingStatus.ONLINE);
        countPingKo = 0; // reset counter
    }

    public void pingSent() {
        if (++countPingKo > 3) {
            BridgeHandler bridgeHandler = bridge.getHandler();
            if (bridgeHandler != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Gateway " + bridgeHandler.getThing().getUID() + " do not respond to " + countPingKo + " ping");
            }
        }
    }

    public void sendSubscription() {
        if (this.gwConfig.gatewayIpAddress != null && this.gwConfig.gatewayIpAddress.length() > 0) {
            soulissCommands.sendSUBSCRIPTIONframe(this.gwConfig.gatewayIpAddress, (byte) this.gwConfig.nodeIndex,
                    (byte) this.gwConfig.userIndex, getNodes());

        }
        logger.debug("Sent subscription packet");
    }

    public void setThereIsAThingDetection() {
        thereIsAThingDetection = true;
    }

    public void resetThereIsAThingDetection() {
        thereIsAThingDetection = false;
    }

    public void setDiscoveryService(SoulissGatewayDiscovery discoveryService) {
        this.discoveryService = discoveryService;
    }

}
