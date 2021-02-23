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

    private final Logger logger = LoggerFactory.getLogger(QbusBridgeHandler.class);

    private @Nullable QbusCommunication qbusComm;

    protected @Nullable QbusConfiguration config;

    private @Nullable ScheduledFuture<?> refreshTimer;

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
        if (qbusComm != null) {
            qbusComm.setBridgeCallBack(this);
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

            qbusComm = new QbusCommunication();

            setBridgeCallBack();

            if (qbusComm != null) {
                try {
                    qbusComm.startCommunication();
                } catch (InterruptedException | IOException e) {
                    logger.warn("Error on restaring communication.");
                }
            }

            if (qbusComm != null) {
                if (!qbusComm.communicationActive()) {
                    qbusComm = null;
                    bridgeOffline("No communication with Qbus Server");
                    return;
                }
            }

            if (qbusComm != null) {
                if (!qbusComm.clientConnected()) {

                    qbusComm = null;
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
                    comm.restartCommunication();
                    if (!comm.communicationActive()) {
                        bridgeOffline("No connection with Qbus Server");
                        return;
                    }
                } else {
                    // Controller disconnected from Qbus client, try to reconnect controller
                    if (!comm.clientConnected()) {
                        comm.restartCommunication();
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
            comm.stopCommunication();
        }

        comm = null;
    }

    /**
     * Sets the configuration parameters
     */
    protected void readConfig() {
        this.config = getConfig().as(QbusConfiguration.class);
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
        if (this.config != null) {
            return this.config.addr;
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
        if (this.config != null) {
            return this.config.port;
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
        if (this.config != null) {
            return this.config.sn;
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
        if (this.config != null) {
            return this.config.refresh;
        } else {
            return null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
