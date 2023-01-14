/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nobohub.internal.connection;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nobohub.internal.NoboHubBindingConstants;
import org.openhab.binding.nobohub.internal.NoboHubBridgeHandler;
import org.openhab.binding.nobohub.internal.model.NoboCommunicationException;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread that reads from the Nobø Hub and sends HANDSHAKEs to keep the connection open.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class HubCommunicationThread extends Thread {

    private enum HubCommunicationThreadState {
        STARTING(null, null, ""),
        CONNECTED(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""),
        DISCONNECTED(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/message.bridge.status.failed"),
        STOPPED(null, null, "");

        private final @Nullable ThingStatus status;
        private final @Nullable ThingStatusDetail statusDetail;
        private final String errorMessage;

        HubCommunicationThreadState(@Nullable ThingStatus status, @Nullable ThingStatusDetail statusDetail,
                String errorMessage) {
            this.status = status;
            this.statusDetail = statusDetail;
            this.errorMessage = errorMessage;
        }

        public @Nullable ThingStatus getThingStatus() {
            return status;
        }

        public @Nullable ThingStatusDetail getThingStatusDetail() {
            return statusDetail;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(HubCommunicationThread.class);

    private final HubConnection hubConnection;
    private final NoboHubBridgeHandler hubHandler;
    private final Duration timeout;
    private Instant lastTimeFullScan;

    private volatile boolean stopped = false;
    private HubCommunicationThreadState currentState = HubCommunicationThreadState.STARTING;

    public HubCommunicationThread(HubConnection hubConnection, NoboHubBridgeHandler hubHandler, Duration timeout) {
        this.hubConnection = hubConnection;
        this.hubHandler = hubHandler;
        this.timeout = timeout;
        this.lastTimeFullScan = Instant.now();
    }

    public void stopNow() {
        stopped = true;
    }

    @Override
    public void run() {
        while (!stopped) {
            switch (currentState) {
                case STARTING:
                    try {
                        hubConnection.refreshAll();
                        lastTimeFullScan = Instant.now();
                        setNextState(HubCommunicationThreadState.CONNECTED);
                    } catch (NoboCommunicationException nce) {
                        logger.debug("Communication error with Hub", nce);
                        setNextState(HubCommunicationThreadState.DISCONNECTED);
                    }
                    break;

                case CONNECTED:
                    try {
                        if (hubConnection.hasData()) {
                            hubConnection.processReads(timeout);
                        }

                        if (Instant.now()
                                .isAfter(lastTimeFullScan.plus(NoboHubBindingConstants.TIME_BETWEEN_FULL_SCANS))) {
                            hubConnection.refreshAll();
                            lastTimeFullScan = Instant.now();
                        } else {
                            hubConnection.handshake();
                        }

                        hubConnection.processReads(timeout);
                    } catch (NoboCommunicationException nce) {
                        logger.debug("Communication error with Hub", nce);
                        setNextState(HubCommunicationThreadState.DISCONNECTED);
                    }
                    break;

                case DISCONNECTED:
                    try {
                        Thread.sleep(NoboHubBindingConstants.TIME_BETWEEN_RETRIES_ON_ERROR.toMillis());
                        try {
                            logger.debug("Trying to do a hard reconnect");
                            hubConnection.hardReconnect();
                            setNextState(HubCommunicationThreadState.CONNECTED);
                        } catch (NoboCommunicationException nce2) {
                            logger.debug("Failed to reconnect connection", nce2);
                        }
                    } catch (InterruptedException ie) {
                        logger.debug("Interrupted from sleep after error");
                        Thread.currentThread().interrupt();
                    }
                    break;

                case STOPPED:
                    break;
            }
        }

        if (stopped) {
            logger.debug("HubCommunicationThread is stopped, disconnecting from Hub");
            setNextState(HubCommunicationThreadState.STOPPED);
            try {
                hubConnection.disconnect();
            } catch (NoboCommunicationException nce) {
                logger.debug("Error disconnecting from Hub", nce);
            }
        }
    }

    public HubConnection getConnection() {
        return hubConnection;
    }

    private void setNextState(HubCommunicationThreadState newState) {
        currentState = newState;
        ThingStatus stateThingStatus = newState.getThingStatus();
        ThingStatusDetail stateThingStatusDetail = newState.getThingStatusDetail();
        if (null != stateThingStatus && null != stateThingStatusDetail) {
            hubHandler.setStatusInfo(stateThingStatus, stateThingStatusDetail, newState.getErrorMessage());
        }
    }
}
