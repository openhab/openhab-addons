/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link QbusBridgeHandler} is the handler for a Qbus controller
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusBridgeHandler extends BaseBridgeHandler {

    private @Nullable QbusCommunication qbusComm;

    protected @Nullable QbusConfiguration bridgeConfig = new QbusConfiguration();

    private @Nullable ScheduledFuture<?> refreshTimer;

    private final Logger logger = LoggerFactory.getLogger(QbusBridgeHandler.class);

    public QbusBridgeHandler(Bridge Bridge) {
        super(Bridge);
    }

    /**
     * Initialize the bridge
     */
    @Override
    public void initialize() {
        readConfig();

        Integer serverCheck = getServerCheck();

        if (serverCheck != null) {
            this.setupRefreshTimer(serverCheck);
        }

        createCommunicationObject();
    }

    /**
     * Sets the Bridge call back
     */
    private void setBridgeCallBack() {
        QbusCommunication qbusCommunication = getQbusCommunication();
        if (qbusCommunication != null) {
            qbusCommunication.setBridgeCallBack(this);
        }
    }

    /**
     * Create communication object to Qbus server and start communication.
     *
     * @param addr : IP address of Qbus server
     * @param port : Communication port of QbusServer
     */
    private void createCommunicationObject() {
        scheduler.submit(() -> {

            setQbusCommunication(new QbusCommunication(thing));

            QbusCommunication qbusCommunication = getQbusCommunication();

            setBridgeCallBack();

            Integer serverCheck = getServerCheck();
            String sn = getSn();
            if (serverCheck != null) {
                if (sn != null) {
                    if (qbusCommunication != null) {
                        try {
                            qbusCommunication.startCommunication();
                        } catch (InterruptedException e) {
                            String msg = e.getMessage();
                            bridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Communication wit Qbus server could not be established, will try to reconnect every "
                                            + serverCheck + " minutes. InterruptedException: " + msg);
                            return;
                        } catch (IOException e) {
                            String msg = e.getMessage();
                            bridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Communication wit Qbus server could not be established, will try to reconnect every "
                                            + serverCheck + " minutes. IOException: " + msg);
                            return;
                        }

                        if (!qbusCommunication.communicationActive()) {
                            bridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                    "No communication with Qbus Server, will try to reconnect every " + serverCheck
                                            + " minutes");
                            return;
                        }

                        if (!qbusCommunication.clientConnected()) {
                            bridgePending("Waiting for Qbus client to come online");
                            return;
                        }

                    }
                }
            }
        });
    }

    /**
     * Updates offline status off the Bridge when an error occurs.
     *
     * @param detail
     * @param message
     */
    public void bridgeOffline(ThingStatusDetail detail, String message) {
        updateStatus(ThingStatus.OFFLINE, detail, message);
    }

    /**
     * Updates pending status off the Bridge (usualay when Qbus client id not connected)
     *
     * @param message
     */
    public void bridgePending(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING, message);
    }

    /**
     * Put bridge online when error in communication resolved.
     */
    public void bridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Initializes a timer that check the communication with Qbus server/client and tries to re-establish communication.
     *
     * @param refreshInterval Time before refresh in minutes.
     */
    private void setupRefreshTimer(int refreshInterval) {
        ScheduledFuture<?> timer = refreshTimer;

        if (timer != null) {
            timer.cancel(true);
            refreshTimer = null;
        }

        if (refreshInterval == 0) {
            return;
        }

        refreshTimer = scheduler.scheduleWithFixedDelay(() -> {
            logger.debug("Timer started");
            QbusCommunication comm = getCommunication();
            Integer serverCheck = getServerCheck();

            if (comm != null) {
                if (serverCheck != null) {
                    if (!comm.communicationActive()) {
                        // Disconnected from Qbus Server, restart communication
                        try {
                            comm.startCommunication();
                        } catch (InterruptedException e) {
                            String msg = e.getMessage();
                            bridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Communication wit Qbus server could not be established, will try to reconnect every "
                                            + serverCheck + " minutes. InterruptedException: " + msg);
                        } catch (IOException e) {
                            String msg = e.getMessage();
                            bridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Communication wit Qbus server could not be established, will try to reconnect every "
                                            + serverCheck + " minutes. IOException: " + msg);
                        }
                    }
                }
            }
        }, refreshInterval, refreshInterval, TimeUnit.MINUTES);
    }

    /**
     * Disposes the Bridge and stops communication with the Qbus server
     */
    @Override
    public void dispose() {
        ScheduledFuture<?> timer = refreshTimer;
        if (timer != null) {
            timer.cancel(true);
        }

        refreshTimer = null;

        QbusCommunication comm = getCommunication();

        if (comm != null) {
            try {
                comm.stopCommunication();
            } catch (IOException e) {
                String message = e.toString();
                logger.debug("Error on stopping communication.{} ", message);
            }
        }

        comm = null;
    }

    /**
     * Reconnect to Qbus server if controller is offline
     */
    public void ctdOffline() {
        bridgePending("Waiting for CTD connection");
    }

    /**
     * Get BridgeCommunication
     *
     * @return BridgeCommunication
     */
    public @Nullable QbusCommunication getQbusCommunication() {
        if (this.qbusComm != null) {
            return this.qbusComm;
        } else {
            return null;
        }
    }

    /**
     * Sets BridgeCommunication
     *
     * @param BridgeCommunication
     */
    void setQbusCommunication(QbusCommunication comm) {
        this.qbusComm = comm;
    }

    /**
     * Gets the status off the Bridge
     *
     * @return
     */
    public ThingStatus getStatus() {
        return thing.getStatus();
    }

    /**
     * Gets the status off the Bridge
     *
     * @return
     */
    public ThingStatusDetail getStatusDetails() {
        ThingStatusInfo status = thing.getStatusInfo();
        return status.getStatusDetail();
    }

    /**
     * Sets the configuration parameters
     */
    protected void readConfig() {
        bridgeConfig = getConfig().as(QbusConfiguration.class);
    }

    /**
     * Get the Qbus communication object.
     *
     * @return Qbus communication object
     */
    public @Nullable QbusCommunication getCommunication() {
        return this.qbusComm;
    }

    /**
     * Get the ip address of the Qbus server.
     *
     * @return the ip address
     */
    public @Nullable String getAddress() {
        QbusConfiguration localConfig = this.bridgeConfig;

        if (localConfig != null) {
            return localConfig.addr;
        } else {
            return null;
        }
    }

    /**
     * Get the listening port of the Qbus server.
     *
     * @return
     */
    public @Nullable Integer getPort() {
        QbusConfiguration localConfig = this.bridgeConfig;

        if (localConfig != null) {
            return localConfig.port;
        } else {
            return null;
        }
    }

    /**
     * Get the serial nr of the Qbus server.
     *
     * @return the serial nr of the controller
     */
    public @Nullable String getSn() {
        QbusConfiguration localConfig = this.bridgeConfig;

        if (localConfig != null) {
            return localConfig.sn;
        } else {
            return null;
        }
    }

    /**
     * Get the refresh interval.
     *
     * @return the refresh interval
     */
    public @Nullable Integer getServerCheck() {
        QbusConfiguration localConfig = this.bridgeConfig;

        if (localConfig != null) {
            return localConfig.serverCheck;
        } else {
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
