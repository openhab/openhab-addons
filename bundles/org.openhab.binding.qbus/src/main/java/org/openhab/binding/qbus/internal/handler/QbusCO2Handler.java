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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.CHANNEL_CO2;
import static org.openhab.core.types.RefreshType.REFRESH;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCO2;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
public class QbusCO2Handler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusCO2Handler.class);

    public QbusCO2Handler(Thing thing) {
        super(thing);
    }

    protected QbusThingsConfig config = getConfig().as(QbusThingsConfig.class);;

    int co2Id = 0;

    String sn = "";

    /**
     * Main initialization
     */
    @Override
    public void initialize() {

        setConfig();
        co2Id = getId();

        QbusCommunication QComm = getCommunication("CO2", co2Id);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("CO2", co2Id);
        if (QBridgeHandler == null) {
            return;
        }

        QbusCO2 QCo2 = QComm.getCo2().get(co2Id);

        sn = QBridgeHandler.getSn();

        if (QCo2 != null) {
            QCo2.setThingHandler(this);
            handleStateUpdate(QCo2);
            logger.info("CO2 intialized {}", co2Id);
        } else {
            logger.info("CO2 not intialized {}", co2Id);
        }
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication QComm = getCommunication("CO2", co2Id);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for CO2 " + co2Id);
            return;
        }

        QbusCO2 QCo2 = QComm.getCo2().get(co2Id);

        if (QCo2 == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for CO2 " + co2Id);
            return;
        }

        scheduler.submit(() -> {
            if (!QComm.communicationActive()) {
                restartCommunication(QComm, "CO2", co2Id);
            }

            if (QComm.communicationActive()) {
                if (command == REFRESH) {
                    handleStateUpdate(QCo2);
                    return;
                }

            }
        });
    }

    /**
     * Method to update state of channel, called from Qbus CO2.
     */
    public void handleStateUpdate(QbusCO2 QCo2) {

        int CO2State = QCo2.getState();

        updateState(CHANNEL_CO2, new DecimalType(CO2State));
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
     * @return co2Id
     */

    public int getId() {
        return config.co2Id;
    }
}
