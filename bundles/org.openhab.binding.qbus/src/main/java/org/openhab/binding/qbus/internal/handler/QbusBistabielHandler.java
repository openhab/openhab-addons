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
package org.openhab.binding.qbus.internal.handler;

import static org.openhab.binding.qbus.internal.QbusBindingConstants.CHANNEL_SWITCH;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusBistabiel;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * The {@link QbusBistabielHandler} is responsible for handling the Bistable outputs of Qbus
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusBistabielHandler extends QbusGlobalHandler {

    protected @Nullable QbusThingsConfig config;

    private @Nullable Integer bistabielId;

    private @Nullable String sn;

    private @Nullable ScheduledFuture<?> pollingJob;

    public QbusBistabielHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        readConfig();
        bistabielId = getId();

        QbusCommunication qComm = getCommunication("Bistabiel", bistabielId);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Bistabiel", bistabielId);
        if (qBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        setSN();

        Map<Integer, QbusBistabiel> bistabielComm = qComm.getBistabiel();

        if (bistabielComm != null) {
            QbusBistabiel qBistabiel = bistabielComm.get(bistabielId);
            if (qBistabiel != null) {
                qBistabiel.setThingHandler(this);
                handleStateUpdate(qBistabiel);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Error while initializing the thing.");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Error while initializing the thing.");
        }
    }

    /**
     * Returns the serial number of the controller
     *
     * @return the serial nr
     */
    public @Nullable String getSN() {
        return this.sn;
    }

    /**
     * Sets the serial number of the controller
     */
    public void setSN() {
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Bistabiel", bistabielId);
        if (qBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }
        this.sn = qBridgeHandler.getSn();
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Bistabiel", bistabielId);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for bistabiel " + bistabielId);
            return;
        }

        Map<Integer, QbusBistabiel> bistabielComm = qComm.getBistabiel();

        if (bistabielComm != null) {
            QbusBistabiel qBistabiel = bistabielComm.get(bistabielId);

            if (qBistabiel == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "bridge communication not initialized when trying to execute command for bistabiel "
                                + bistabielId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Bistabiel", bistabielId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qBistabiel);
                            return;
                        }

                        handleSwitchCommand(qBistabiel, channelUID, command);
                    }
                });
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Error while initializing the thing.");
        }
    }

    /**
     * Executes the switch command
     *
     * @throws InterruptedException
     */
    private void handleSwitchCommand(QbusBistabiel qBistabiel, ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            String snr = getSN();
            if (snr != null) {
                if (command == OnOffType.OFF) {
                    qBistabiel.execute(0, snr);
                } else {
                    qBistabiel.execute(100, snr);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "No serial number configured for  " + bistabielId);
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Bistabiel.
     */
    public void handleStateUpdate(QbusBistabiel qBistabiel) {
        Integer bistabielState = qBistabiel.getState();
        if (bistabielState != null) {
            updateState(CHANNEL_SWITCH, (bistabielState == 0) ? OnOffType.OFF : OnOffType.ON);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        final ScheduledFuture<?> localPollingJob = this.pollingJob;

        if (localPollingJob != null) {
            localPollingJob.cancel(true);
        }

        if (localPollingJob == null || localPollingJob.isCancelled()) {
            this.config = getConfig().as(QbusThingsConfig.class);
        }
    }

    /**
     * Returns the Id from the configuration
     *
     * @return
     */
    public @Nullable Integer getId() {
        QbusThingsConfig bistabielConfig = this.config;
        if (bistabielConfig != null) {
            if (bistabielConfig.bistabielId != null) {
                return bistabielConfig.bistabielId;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
