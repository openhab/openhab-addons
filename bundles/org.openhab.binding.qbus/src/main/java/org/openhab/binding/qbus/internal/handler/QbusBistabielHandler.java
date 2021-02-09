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

    public QbusBistabielHandler(Thing thing) {
        super(thing);
    }

    protected @NonNullByDefault({}) QbusThingsConfig config;

    int bistabielId;

    @Nullable
    private String sn;

    /**
     * Main initialization
     */
    @Override
    public void initialize() {

        readConfig();
        bistabielId = getId();

        QbusCommunication QComm = getCommunication("Bistabiel", bistabielId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Bistabiel", bistabielId);
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        setSN();

        Map<Integer, QbusBistabiel> bistabielComm = QComm.getBistabiel();

        if (bistabielComm != null) {
            QbusBistabiel QBistabiel = bistabielComm.get(bistabielId);
            if (QBistabiel != null) {
                QBistabiel.setThingHandler(this);
                handleStateUpdate(QBistabiel);
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
        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Bistabiel", bistabielId);
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        } else {
            this.sn = QBridgeHandler.getSn();
        }
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication QComm = getCommunication("Bistabiel", bistabielId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for bistabiel " + bistabielId);
            return;
        }

        Map<Integer, QbusBistabiel> bistabielComm = QComm.getBistabiel();

        if (bistabielComm != null) {
            QbusBistabiel QBistabiel = bistabielComm.get(bistabielId);

            if (QBistabiel == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for bistabiel "
                                + bistabielId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!QComm.communicationActive()) {
                        restartCommunication(QComm, "Bistabiel", bistabielId);
                    }

                    if (QComm.communicationActive()) {

                        if (command == REFRESH) {
                            handleStateUpdate(QBistabiel);
                            return;
                        }

                        handleSwitchCommand(QBistabiel, channelUID, command);
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
     */
    private void handleSwitchCommand(QbusBistabiel QBistabiel, ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            @Nullable
            String snr = getSN();
            if (snr != null) {
                if (s == OnOffType.OFF) {
                    QBistabiel.execute(0, snr);
                } else {
                    QBistabiel.execute(100, snr);
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
    public void handleStateUpdate(QbusBistabiel QBistabiel) {
        Integer bistabielState = QBistabiel.getState();
        if (bistabielState != null) {
            updateState(CHANNEL_SWITCH, (bistabielState == 0) ? OnOffType.OFF : OnOffType.ON);
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        config = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return
     */
    public int getId() {
        if (config != null) {
            return config.bistabielId;
        } else {
            return 0;
        }
    }
}
