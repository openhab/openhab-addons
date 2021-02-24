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

package org.openhab.binding.qbus.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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

    protected @Nullable QbusConfiguration config;

    private @Nullable ScheduledFuture<?> refreshTimer;

    private @Nullable ScheduledFuture<?> pollingJob;

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

        InetAddress addr;
        Integer port = getPort();
        Integer refresh = getRefresh();

        if (port == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No port defined for Qbus Server");
            return;
        }

        try {
            addr = InetAddress.getByName(getAddress());
            if (addr == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "No ip address defined for Qbus Server");
                return;
            } else {
                createCommunicationObject(addr, port);
            }
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Incorrect ip address set for Qbus Server");
        }

        if (refresh != null) {
            this.setupRefreshTimer(refresh);
        }
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
    private void createCommunicationObject(InetAddress addr, int port) {
        scheduler.submit(() -> {

            setQbusCommunication(new QbusCommunication(thing));

            QbusCommunication qbusCommunication = getQbusCommunication();

            setBridgeCallBack();

            if (qbusCommunication != null) {
                try {
                    qbusCommunication.startCommunication();
                } catch (InterruptedException | IOException e) {
                    bridgeOffline("Communication could not be established {}" + e.getMessage());
                    return;
                }

                if (!qbusCommunication.communicationActive()) {
                    bridgeOffline("No communication with Qbus Server");
                    return;
                }

                if (!qbusCommunication.clientConnected()) {
                    bridgeOffline("No communication with Qbus Client");
                    return;
                }
            }

            updateStatus(ThingStatus.ONLINE);

        });
    }

    /**
     * Take bridge offline when error in communication with Qbus server. This method can also be
     * called directly from {@link QbusCommunication} object.
     */
    public void bridgeOffline(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
    }

    /**
     * Put bridge online when error in communication resolved.
     */
    public void bridgeOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Schedule future communication refresh.
     *
     * @param interval_config Time before refresh in minutes.
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

        // This timer will check connection with server and client periodically
        refreshTimer = scheduler.scheduleWithFixedDelay(() -> {
            QbusCommunication comm = getCommunication();

            if (comm != null) {
                if (!comm.communicationActive()) {
                    // Disconnected from Qbus Server, try to reconnect
                    try {
                        comm.restartCommunication();
                    } catch (InterruptedException e) {
                        bridgeOffline("No connection with Qbus Server" + e.toString());
                    } catch (IOException e) {
                        bridgeOffline("No connection with Qbus Server" + e.toString());
                    }
                    if (!comm.communicationActive()) {
                        bridgeOffline("No connection with Qbus Server");
                        return;
                    }
                } else {
                    // Controller disconnected from Qbus client, try to reconnect controller
                    if (!comm.clientConnected()) {
                        try {
                            comm.restartCommunication();
                        } catch (InterruptedException e) {
                            bridgeOffline("No connection with Qbus Server" + e.toString());
                        } catch (IOException e) {
                            bridgeOffline("No connection with Qbus Server" + e.toString());
                        }
                        if (!comm.clientConnected()) {
                            bridgeOffline("No connection with Qbus Client");
                            return;
                        }
                    }
                }
            }
            updateStatus(ThingStatus.ONLINE);

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
                logger.error("Error on stopping communication.{} ", message);
            }
        }

        comm = null;
    }

    /**
     * Sets the configuration parameters
     */
    protected void readConfig() {
        final ScheduledFuture<?> localPollingJob = this.pollingJob;

        if (localPollingJob != null) {
            localPollingJob.cancel(true);
        }

        if (localPollingJob == null || localPollingJob.isCancelled()) {
            this.config = getConfig().as(QbusConfiguration.class);
        }
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
        QbusConfiguration bridgeConfig = this.config;
        if (bridgeConfig != null) {
            if (bridgeConfig.addr != null) {
                return bridgeConfig.addr;
            } else {
                return "";
            }
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
        QbusConfiguration bridgeConfig = this.config;
        if (bridgeConfig != null) {
            if (bridgeConfig.port != null) {
                return bridgeConfig.port;
            } else {
                return 0;
            }
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
        QbusConfiguration bridgeConfig = this.config;
        if (bridgeConfig != null) {
            if (bridgeConfig.sn != null) {
                return bridgeConfig.sn;
            } else {
                return "";
            }
        } else {
            return null;
        }
    }

    /**
     * Get the refresh interval.
     *
     * @return the refresh interval
     */
    public @Nullable Integer getRefresh() {
        QbusConfiguration bridgeConfig = this.config;
        if (bridgeConfig != null) {
            if (bridgeConfig.refresh != null) {
                return bridgeConfig.refresh;
            } else {
                return 0;
            }
        } else {
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Get state of bistabiel.
     *
     * @return bistabiel state
     */
    public @Nullable QbusCommunication getQbusCommunication() {
        if (this.qbusComm != null) {
            return this.qbusComm;
        } else {
            return null;
        }
    }

    /**
     * Sets state of bistabiel.
     *
     * @param bistabiel state
     */
    void setQbusCommunication(QbusCommunication comm) {
        this.qbusComm = comm;
    }
}
