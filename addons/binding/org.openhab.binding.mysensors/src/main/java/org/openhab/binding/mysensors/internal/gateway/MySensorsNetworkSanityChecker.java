/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.internal.event.MySensorsEventRegister;
import org.openhab.binding.mysensors.internal.event.MySensorsGatewayEventListener;
import org.openhab.binding.mysensors.internal.protocol.MySensorsAbstractConnection;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageAck;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageType;
import org.openhab.binding.mysensors.internal.sensors.MySensorsChild;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNode;
import org.openhab.binding.mysensors.internal.sensors.MySensorsNodeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regulary checks the status of the link to the gateway to the MySensors network.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsNetworkSanityChecker implements MySensorsGatewayEventListener, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SEND_DELAY = 3000;

    private MySensorsEventRegister myEventRegister;
    private MySensorsAbstractConnection myCon;
    private MySensorsGateway myGateway;

    private final int scheduleMinuteDelay;
    private final int maxAttemptsBeforeDisconnecting;

    private final boolean sendHeartbeat;
    private final int maxHeartbeatAttemptsBeforeDisconnecting;

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> futureSanityChk;

    private Integer missedIVersionMessages = 0;
    private boolean iVersionMessageArrived;

    private Map<Integer, Integer> missingHearbeatsMap;

    public MySensorsNetworkSanityChecker(MySensorsGateway myGateway, MySensorsEventRegister myEventRegister,
            MySensorsAbstractConnection myCon) {
        this.myGateway = myGateway;
        this.myCon = myCon;
        this.myEventRegister = myEventRegister;
        this.scheduleMinuteDelay = myGateway.getConfiguration().getSanityCheckerInterval();
        this.maxAttemptsBeforeDisconnecting = myGateway.getConfiguration().getSanCheckConnectionFailAttempts();
        this.sendHeartbeat = myGateway.getConfiguration().getSanCheckSendHeartbeat();
        this.maxHeartbeatAttemptsBeforeDisconnecting = myGateway.getConfiguration()
                .getSanCheckSendHeartbeatFailAttempts();
    }

    /**
     * Starts the sanity check of the network.
     * Tests if the connection to the bridge is still alive.
     */
    public void start() {
        reset();

        if (futureSanityChk == null && scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            futureSanityChk = scheduler.scheduleWithFixedDelay(this, scheduleMinuteDelay, scheduleMinuteDelay,
                    TimeUnit.MINUTES);
        } else {
            logger.warn("Network Sanity Checker is alredy running");
        }
    }

    /**
     * Stops the sanity check of the network.
     */
    public void stop() {
        logger.info("Network Sanity Checker thread stopped");

        if (futureSanityChk != null) {
            futureSanityChk.cancel(true);
            futureSanityChk = null;
        }

        if (scheduler != null) {
            scheduler.shutdown();
            scheduler.shutdownNow();
            scheduler = null;
        }

    }

    @Override
    public void run() {
        Thread.currentThread().setName(MySensorsNetworkSanityChecker.class.getName());

        try {

            myEventRegister.addEventListener(this);

            if (checkConnectionStatus()) { // Connection is ok, let's go on with other check

                if (sendHeartbeat) {
                    logger.debug("Sending I_HEARTBEAT_REQUESTs");
                    sendHeartbeatRequest();
                    Thread.sleep(SEND_DELAY);
                    checkHeartbeatsResponse();
                }

                checkExpectedUpdate();

            }

        } catch (Exception e) {
            logger.error("Exception in network sanity thread checker", e);
        } finally {
            myEventRegister.removeEventListener(this);
        }
    }

    private void checkExpectedUpdate() {
        for (Integer nodeId : myGateway.getGivenIds()) {
            MySensorsNode node = myGateway.getNode(nodeId);
            if (node != null) {
                Optional<MySensorsNodeConfig> c = node.getNodeConfig();
                if (c.isPresent()) {
                    MySensorsNodeConfig nodeConfig = c.get();
                    int minutesTimeout = nodeConfig.getExpectUpdateTimeout();
                    if (minutesTimeout > 0) {
                        if (!nodeConfig.getRequestHeartbeatResponse()) {
                            long nodeLastUpdate = node.getLastUpdate().getTime();
                            long minutesTimeoutMillis = minutesTimeout * 60 * 1000;
                            logger.debug("Node {} request update every {} minutes, current: {}", nodeId, minutesTimeout,
                                    (System.currentTimeMillis() - nodeLastUpdate) / (1000 * 60));
                            if (((System.currentTimeMillis() - nodeLastUpdate) > minutesTimeoutMillis)
                                    && node.isReachable()) {
                                logger.warn("Node {} not receive excpected update", nodeId);
                                node.setReachable(false);
                                myEventRegister.notifyNodeReachEvent(node, false);
                            }
                        } else {
                            logger.warn(
                                    "Check expected update can't be performed on node {} if request heartbeat is active.",
                                    nodeId);
                        }
                    }
                }
            }
        }
    }

    private void checkHeartbeatsResponse() {
        for (Integer nodeId : myGateway.getGivenIds()) {
            MySensorsNode node = myGateway.getNode(nodeId);
            if (node != null) {
                Optional<MySensorsNodeConfig> c = node.getNodeConfig();
                if (c.isPresent()) {
                    MySensorsNodeConfig nodeConfig = c.get();
                    if (nodeConfig.getRequestHeartbeatResponse()) {
                        synchronized (missingHearbeatsMap) {
                            Integer missingHearbeat = missingHearbeatsMap.getOrDefault(nodeId, 0);

                            if (missingHearbeat == -1) { // Heartbeat response received
                                if (!node.isReachable()) {
                                    logger.debug("Node {} received heartbeat response. Reconnecting it", nodeId);
                                    node.setReachable(true);
                                    myEventRegister.notifyNodeReachEvent(node, true);
                                } else {
                                    logger.debug("Node {} received heartbeat response, ok...", nodeId);
                                }
                            } else { // No heartbeat response received
                                missingHearbeat++;

                                if (missingHearbeat >= maxHeartbeatAttemptsBeforeDisconnecting) {
                                    if (node.isReachable()) {
                                        logger.warn(
                                                "Node {} is not receiving heartbeat response so it will be disconnected",
                                                nodeId);
                                        node.setReachable(false);
                                        myEventRegister.notifyNodeReachEvent(node, false);
                                    }
                                } else {
                                    missingHearbeatsMap.put(nodeId, missingHearbeat);
                                    logger.debug("Node {} is not receiving hearbeat response (miss {} of {})", nodeId,
                                            missingHearbeat, maxHeartbeatAttemptsBeforeDisconnecting);
                                }

                            }
                        }
                    }
                }

            }
        }
    }

    private void sendHeartbeatRequest() {
        synchronized (missingHearbeatsMap) {
            for (Integer nodeId : myGateway.getGivenIds()) {
                if (nodeId != null) {
                    MySensorsMessage msg = new MySensorsMessage(nodeId, MySensorsChild.MYSENSORS_CHILD_ID_RESERVED_255,
                            MySensorsMessageType.INTERNAL, MySensorsMessageAck.FALSE, false,
                            MySensorsMessageSubType.I_HEARTBEAT_REQUEST, "");
                    myGateway.sendMessage(msg);

                }
            }
        }
    }

    /**
     * Check connection status based on the {@link MySensorsGatewayConfig}
     *
     * @return true if connection is ok and no request for disconnection done
     *
     * @throws InterruptedException see Thread.sleep for more info
     */
    private boolean checkConnectionStatus() throws InterruptedException {
        boolean ret = true;
        myGateway.sendMessage(MySensorsMessage.I_VERSION_MESSAGE);

        Thread.sleep(SEND_DELAY);

        synchronized (missedIVersionMessages) {

            if (!iVersionMessageArrived) {
                logger.warn("I_VERSION message response is not arrived. Remained attempts before disconnection {}",
                        maxAttemptsBeforeDisconnecting - missedIVersionMessages);

                if ((maxAttemptsBeforeDisconnecting - missedIVersionMessages) <= 0) {
                    logger.error("Retry period expired, gateway is down. Disconneting bridge...");

                    myCon.requestDisconnection(true);
                    ret = false;

                } else {
                    missedIVersionMessages++;
                }
            } else {
                logger.debug("Network sanity check: PASSED");
                missedIVersionMessages = 0;
            }

            iVersionMessageArrived = false;
        }

        return ret;
    }

    private void reset() {
        synchronized (missedIVersionMessages) {
            iVersionMessageArrived = false;
            missedIVersionMessages = 0;
            missingHearbeatsMap = new HashMap<>();
        }
    }

    @Override
    public void messageReceived(MySensorsMessage message) throws Exception {
        synchronized (missedIVersionMessages) {
            if (!iVersionMessageArrived) {
                if (message.isIVersionMessage()) {
                    iVersionMessageArrived = true;
                }
            }
        }

        synchronized (missingHearbeatsMap) {
            if (message.isHeartbeatResponseMessage()) {
                // -1 in means missingHearbeatsMap means: "ok, heartbeat response received"
                missingHearbeatsMap.put(message.getNodeId(), -1);
            }
        }
    }
}
