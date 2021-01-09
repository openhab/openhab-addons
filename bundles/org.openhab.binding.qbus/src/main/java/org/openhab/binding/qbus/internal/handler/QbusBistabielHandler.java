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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusBistabielHandler} is responsible for handling the Bistable outputs of Qbus
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusBistabielHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusBistabielHandler.class);

    public QbusBistabielHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable QbusThingsConfig config;

    int bistabielId = 0;

    String sn = "";

    /**
     * Main initialization
     */
    @Override
    public void initialize() {

        setConfig();
        bistabielId = getId();

        QbusCommunication QComm = getCommunication("Bistabiel", bistabielId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Bistabiel", bistabielId);
        if (QBridgeHandler == null) {
            return;
        }

        QbusBistabiel QBistabiel = QComm.getBistabiel().get(bistabielId);

        sn = QBridgeHandler.getSn();

        if (QBistabiel != null) {
            QBistabiel.setThingHandler(this);
            handleStateUpdate(QBistabiel);
            logger.info("Bistabiel intialized {}", bistabielId);
        } else {

            logger.warn("Bistabiel not intialized {}", bistabielId);
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
        QbusBistabiel QBistabiel = QComm.getBistabiel().get(bistabielId);

        if (QBistabiel == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for dimmer " + bistabielId);
            return;
        }

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

    /**
     * Executes the switch command
     */
    private void handleSwitchCommand(QbusBistabiel QBistabiel, ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;

            if (s == OnOffType.OFF) {
                QBistabiel.execute(0, sn);
            } else {
                QBistabiel.execute(100, sn);
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Bistabiel.
     */
    public void handleStateUpdate(QbusBistabiel QBistabiel) {

        int bistabielState = QBistabiel.getState();

        updateState(CHANNEL_SWITCH, (bistabielState == 0) ? OnOffType.OFF : OnOffType.ON);
        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Read the configuration
     */
    protected synchronized void setConfig() {
        config = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return bistabielId
     */
    @SuppressWarnings("null")
    public int getId() {
        return config.bistabielId;
    }
}
