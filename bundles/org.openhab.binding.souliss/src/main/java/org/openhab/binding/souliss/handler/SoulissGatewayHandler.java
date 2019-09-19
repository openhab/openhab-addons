/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.SoulissBindingUDPConstants;
import org.openhab.binding.souliss.internal.SoulissDatagramSocketFactory;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingNetworkParameters;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingSendDispatcherJob;
import org.openhab.binding.souliss.internal.protocol.SoulissBindingUDPServerJob;
import org.openhab.binding.souliss.internal.protocol.SoulissCommonCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissGatewayHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 */
public class SoulissGatewayHandler extends BaseBridgeHandler {

    private Logger logger = LoggerFactory.getLogger(SoulissGatewayHandler.class);
    public DatagramSocket datagramSocket_defaultPort;
    SoulissBindingUDPServerJob UDP_Server_DefaultPort_RunnableClass = null;

    boolean bGatewayDetected = false;

    Configuration gwConfigurationMap;

    public int pingRefreshInterval;
    public int subscriptionRefreshInterval;
    public boolean thereIsAThingDetection = true;
    public int healthRefreshInterval;
    public int sendRefreshInterval;
    public int sendTimeoutToRequeue;
    public int sendTimeoutToRemovePacket;
    private Bridge bridge;
    public int preferred_local_port;
    public int souliss_gateway_port;
    public byte userIndex;
    public byte nodeIndex;
    public String IPAddressOnLAN;
    private int nodes;
    private int maxTypicalXnode;
    private int countPING_KO = 0;
    private int maxnodes = 0;
    private int maxrequests = 0;

    private ScheduledFuture<?> UDPserverJob_DefaultPort;

    public SoulissGatewayHandler(Bridge _bridge) {
        super(_bridge);
        bridge = _bridge;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        logger.debug("initializing server handler for thing {}", getThing());

        gwConfigurationMap = bridge.getConfiguration();
        IPAddressOnLAN = (String) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_IP_ADDRESS);

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT) != null) {
            preferred_local_port = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_LOCAL_PORT))
                    .intValue();
            logger.debug("Get Preferred Local Port: {}", preferred_local_port);
        }

        if (preferred_local_port < 0 && preferred_local_port > 65000) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_LOCAL_PORT, 0);
            logger.debug("Set Preferred Local Port to {}", 0);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT) != null) {
            souliss_gateway_port = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_PORT))
                    .intValue();
            logger.debug("Get Souliss Gateway Port: {}", souliss_gateway_port);
        }
        if (souliss_gateway_port < 0 && souliss_gateway_port > 65000)

        {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_PORT,
                    SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
            logger.debug("Set Souliss Gateway Port to {}", SoulissBindingUDPConstants.SOULISS_GATEWAY_DEFAULT_PORT);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX) != null)

        {
            userIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_USER_INDEX)).byteValue();
            logger.debug("Get User Index: {}", userIndex);
            if (userIndex < 0 && userIndex > 255) {
                bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_USER_INDEX,
                        SoulissBindingUDPConstants.SOULISS_DEFAULT_USER_INDEX);
                logger.debug("Set User Index to {}", SoulissBindingUDPConstants.SOULISS_DEFAULT_USER_INDEX);
            }
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
            nodeIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX)).byteValue();
            logger.debug("Get Node Index: {}", nodeIndex);
        }
        if (nodeIndex < 0 && nodeIndex > 255) {
            bridge.getConfiguration().put(SoulissBindingConstants.CONFIG_NODE_INDEX,
                    SoulissBindingUDPConstants.SOULISS_DEFAULT_NODE_INDEX);
            logger.debug("Set Node Index to {}", SoulissBindingUDPConstants.SOULISS_DEFAULT_NODE_INDEX);
        }

        if (gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX) != null) {
            nodeIndex = ((BigDecimal) gwConfigurationMap.get(SoulissBindingConstants.CONFIG_NODE_INDEX)).byteValue();
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
            sendTimeoutToRequeue = ((BigDecimal) gwConfigurationMap
                    .get(SoulissBindingConstants.CONFIG_TIMEOUT_TO_REMOVE_PACKET)).intValue();
            logger.debug("Get send timeout to requeue: {}", sendTimeoutToRequeue);
        }

        // START SERVER ON DEFAULT PORT - Used for topics
        if (UDP_Server_DefaultPort_RunnableClass == null) {
            logger.debug("Starting UDP server on Souliss Default Port for Topics (Publish&Subcribe)");
            datagramSocket_defaultPort = SoulissDatagramSocketFactory.getSocketDatagram(souliss_gateway_port);
            if (datagramSocket_defaultPort != null) {
                UDP_Server_DefaultPort_RunnableClass = new SoulissBindingUDPServerJob(datagramSocket_defaultPort,
                        SoulissBindingNetworkParameters.discoverResult);
                UDPserverJob_DefaultPort = scheduler.scheduleWithFixedDelay(UDP_Server_DefaultPort_RunnableClass, 100,
                        SoulissBindingConstants.SERVER_CICLE_IN_MILLIS, TimeUnit.MILLISECONDS);
            }
        }

        // START JOB PING

        SoulissGatewayJobPing soulissGatewayJobPingRunnable = new SoulissGatewayJobPing(bridge);
        scheduler.scheduleWithFixedDelay(soulissGatewayJobPingRunnable, 2,
                soulissGatewayJobPingRunnable.get_pingRefreshInterval(), TimeUnit.SECONDS);

        SoulissGatewayJobSubscription soulissGatewayJobSubscriptionRunnable = new SoulissGatewayJobSubscription(bridge);
        scheduler.scheduleWithFixedDelay(soulissGatewayJobSubscriptionRunnable, 0,
                soulissGatewayJobSubscriptionRunnable.get_subscriptionRefreshInterval(), TimeUnit.MINUTES);

        SoulissGatewayJobHealty soulissGatewayJobHealtyRunnable = new SoulissGatewayJobHealty(bridge);
        scheduler.scheduleWithFixedDelay(soulissGatewayJobHealtyRunnable, 5,
                soulissGatewayJobHealtyRunnable.get_healthRefreshInterval(), TimeUnit.SECONDS);

        // il ciclo Send è schedulato con la costante SoulissBindingConstants.SEND_DISPATCHER_MIN_DELAY_cicleInMillis
        // internamente il ciclo viene rallentato al timer impostato da configurazione (PaperUI o File)
        SoulissBindingSendDispatcherJob SoulissSendDispatcherRunnable = new SoulissBindingSendDispatcherJob(bridge);
        scheduler.scheduleWithFixedDelay(SoulissSendDispatcherRunnable, 15,
                SoulissBindingConstants.SEND_DISPATCHER_MIN_DELAY_cicleInMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handleRemoval() {
        SoulissBindingNetworkParameters.removeGateway(Byte.parseByte(IPAddressOnLAN.split("\\.")[3]));
        UDP_Server_DefaultPort_RunnableClass = null;
        logger.debug("Gateway handler removing");
    }

    @Override
    public void thingUpdated(Thing thing) {
        logger.debug("Thing Updated: {}", thing.getThingTypeUID());
        SoulissBindingNetworkParameters.removeGateway((Byte.parseByte((IPAddressOnLAN.split("\\.")[3]))));
        // .removeGateway((byte) (Byte.parseByte((IPAddressOnLAN.split("\\.")[3]) & (byte) 0xFF));
        this.thing = thing;
    }

    public void dbStructAnswerReceived() {
        SoulissCommonCommands.sendTYPICAL_REQUESTframe(SoulissBindingNetworkParameters.getDatagramSocket(),
                IPAddressOnLAN, nodeIndex, userIndex, nodes);
    }

    @SuppressWarnings({ "deprecation", "null" })
    public String getGatewayIP() {
        if (thing.getBridgeUID() != null) {
            return ((SoulissGatewayHandler) thingRegistry.get(thing.getBridgeUID()).getHandler()).IPAddressOnLAN;
        }
        return null;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    int iPosNode_Slot = 2;

    public int getNodes() {
        Thing _thing;
        int maxNode = 0;
        Iterator<Thing> _iterator = bridge.getThings().iterator();
        while (_iterator.hasNext()) {
            _thing = _iterator.next();
            String[] _uuidStrings = _thing.getUID().getAsString()
                    .split(SoulissBindingConstants.UUID_NODE_SLOT_SEPARATOR);
            String[] _uuidNodeNumber = _uuidStrings[0].split(SoulissBindingConstants.UUID_ELEMENTS_SEPARATOR);

            iPosNode_Slot = 2; // if uuid is of type souliss:gateway:[typical]:[node]-[slot] then node/slot is at
                               // position 2
            if (_uuidNodeNumber.length > 3) {
                iPosNode_Slot = 3;
            }
            if (Integer.parseInt(_uuidNodeNumber[iPosNode_Slot]) > maxNode) {
                maxNode = Integer.parseInt(_uuidNodeNumber[iPosNode_Slot]);
            }
            // alla fine la lunghezza della lista sarà uguale al numero di nodi presenti
        }
        return maxNode + 1;
    }

    public void setMaxnodes(int maxnodes) {
        this.maxnodes = maxnodes;
    }

    public void setMaxTypicalXnode(int maxTypicalXnode) {
        this.maxTypicalXnode = maxTypicalXnode;
    }

    public void setMaxrequests(int maxrequests) {
        this.maxrequests = maxrequests;
    }

    public int getMaxTypicalXnode() {
        return maxTypicalXnode;
    }

    /**
     * The {@link gatewayDetected} is used to notify that UDPServer decoded a Ping Response from gateway
     *
     * @author Tonino Fazio - Initial contribution
     */

    public void gatewayDetected() {
        logger.debug("Setting Gateway ONLINE");
        try {
            updateStatus(ThingStatus.ONLINE);
        } catch (Exception e) {
            logger.debug("Illegal status transition to ONLINE");
        }
        countPING_KO = 0; // reset counter
    }

    @SuppressWarnings("null")
    public void pingSent() {
        if (++countPING_KO > 3) {
            // if GW do not respond to ping it is setted to OFFLINE
            logger.debug("Gateway do not respond to {} ping packet - setting OFFLINE", countPING_KO);
            if (bridge.getHandler() != null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Gateway "
                        + bridge.getHandler().getThing().getUID() + " do not respond to " + countPING_KO + " ping");
            }
        }
    }

    public void sendSubscription() {
        // logger.debug("Sending subscription packet");
        if (IPAddressOnLAN.length() > 0) {
            SoulissCommonCommands.sendSUBSCRIPTIONframe(SoulissBindingNetworkParameters.getDatagramSocket(),
                    IPAddressOnLAN, nodeIndex, userIndex, getNodes());
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