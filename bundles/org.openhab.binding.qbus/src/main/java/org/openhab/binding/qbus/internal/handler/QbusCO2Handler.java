/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCO2;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusCO2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */
@NonNullByDefault
public class QbusCO2Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusCO2Handler.class);

    // private volatile int prevCO2State;

    public QbusCO2Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer CO2Id = ((Number) this.getConfig().get(CONFIG_CO2_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute CO2 " + CO2Id);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute CO2 " + CO2Id);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Qbus: bridge communication not initialized when trying to execute CO2 " + CO2Id);
            return;
        }

        QbusCO2 QCO2 = QComm.getCo2().get(CO2Id);

        if (QComm.communicationActive()) {
            handleCommandSelection(QCO2, channelUID, command);
        } else {
            // We lost connection but the connection object is there, so was correctly started.
            // Try to restart communication.
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                QComm.restartCommunication();
                // If still not active, take thing offline and return.
                if (!QComm.communicationActive()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Qbus: communication socket error");
                    return;
                }
                // Also put the bridge back online
                QBridgeHandler.bridgeOnline();

                // And finally handle the command
                handleCommandSelection(QCO2, channelUID, command);
            });
        }
    }

    private void handleCommandSelection(QbusCO2 QCO2, ChannelUID channelUID, Command command) {
        logger.debug("Qbus: handle command {} for {}", command, channelUID);

        if (command == REFRESH) {
            handleStateUpdate(QCO2);
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer CO2Id = ((Number) config.get(CONFIG_CO2_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for CO2 " + CO2Id);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for CO2 " + CO2Id);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();
        if (QComm == null || !QComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Qbus: no connection with Qbus server, could not initialize CO2 " + CO2Id);
            return;
        }

        QbusCO2 QCO2 = QComm.getCo2().get(CO2Id);

        // int actionState = QCO2.getState();

        // this.prevCO2State = actionState;
        QCO2.setThingHandler(this);

        Map<String, String> properties = new HashMap<>();

        thing.setProperties(properties);

        handleStateUpdate(QCO2);

        logger.debug("Qbus: CO2 intialized {}", CO2Id);
    }

    /**
     * Method to update state of channel, called from Qbus CO2.
     */
    public void handleStateUpdate(QbusCO2 QCO2) {

        int CO2State = QCO2.getState();

        updateState(CHANNEL_CO2, new DecimalType(CO2State));
        updateStatus(ThingStatus.ONLINE);

        // this.prevCO2State = CO2State;
    }
}
