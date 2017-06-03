/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors.internal.protocol;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.mysensors.MySensorsBindingConstants;
import org.openhab.binding.mysensors.internal.event.MySensorsEventType;
import org.openhab.binding.mysensors.internal.event.MySensorsStatusUpdateEvent;
import org.openhab.binding.mysensors.internal.event.MySensorsUpdateListener;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Regulary checks the status of the link to the gateway to the MySensors network.
 *
 * @author Andrea Cioni
 *
 */
public class MySensorsNetworkSanityChecker implements MySensorsUpdateListener, Runnable {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static final int SHEDULE_MINUTES_DELAY = 3; // only for test will be: 3
    private static final int MAX_ATTEMPTS_BEFORE_DISCONNECT = 3; // only for test will be: 3

    private MySensorsBridgeConnection bridgeConnection = null;

    private ScheduledExecutorService scheduler = null;
    private ScheduledFuture<?> future = null;

    private Integer iVersionMessageMissing = 0;
    private boolean iVersionMessageArrived = false;

    public MySensorsNetworkSanityChecker(MySensorsBridgeConnection bridgeConnection) {
        this.bridgeConnection = bridgeConnection;
    }

    private void reset() {
        synchronized (iVersionMessageMissing) {
            iVersionMessageArrived = false;
            iVersionMessageMissing = 0;
        }
    }

    /**
     * Starts the sanity check of the network.
     * Tests if the connection to the bridge is still alive.
     */
    public void start() {
        reset();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        future = scheduler.scheduleWithFixedDelay(this, SHEDULE_MINUTES_DELAY, SHEDULE_MINUTES_DELAY, TimeUnit.MINUTES);

    }

    /**
     * Stops the sanity check of the network.
     */
    public void stop() {
        logger.info("Network Sanity Checker thread stopped");

        if (future != null) {
            future.cancel(true);
            future = null;
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
            bridgeConnection.addEventListener(this);

            bridgeConnection.addMySensorsOutboundMessage(MySensorsBindingConstants.I_VERSION_MESSAGE);

            Thread.sleep(3000);

            synchronized (iVersionMessageMissing) {
                if (!iVersionMessageArrived) {
                    logger.warn("I_VERSION message response is not arrived. Remained attempts before disconnection {}",
                            MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing);

                    if ((MAX_ATTEMPTS_BEFORE_DISCONNECT - iVersionMessageMissing) <= 0) {
                        logger.error("Retry period expired, gateway is down. Disconneting bridge...");

                        bridgeConnection.requestDisconnection(true);

                    } else {
                        iVersionMessageMissing++;
                    }
                } else {
                    logger.debug("Network sanity check: PASSED");
                    iVersionMessageMissing = 0;
                }

                iVersionMessageArrived = false;
            }

        } catch (InterruptedException e) {
            logger.error("interrupted exception in network sanity thread checker");
        } finally {
            bridgeConnection.removeEventListener(this);
        }
    }

    @Override
    public void statusUpdateReceived(MySensorsStatusUpdateEvent event) {
        if (event.getEventType() == MySensorsEventType.INCOMING_MESSAGE) {
            synchronized (iVersionMessageMissing) {
                if (!iVersionMessageArrived) {
                    iVersionMessageArrived = ((MySensorsMessage) event.getData()).isIVersionMessage();
                }
            }
        }
    }
}