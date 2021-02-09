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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCO2;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * The {@link QbusCO2Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusCO2Handler extends QbusGlobalHandler {

    public QbusCO2Handler(Thing thing) {
        super(thing);
    }

    protected @NonNullByDefault({}) QbusThingsConfig config;

    int co2Id = 0;

    @Nullable
    String sn;

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

        setSN();

        Map<Integer, QbusCO2> co2Comm = QComm.getCo2();

        if (co2Comm != null) {
            QbusCO2 QCo2 = co2Comm.get(co2Id);
            if (QCo2 != null) {
                QCo2.setThingHandler(this);
                handleStateUpdate(QCo2);
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

        Map<Integer, QbusCO2> co2Comm = QComm.getCo2();

        if (co2Comm != null) {
            QbusCO2 QCo2 = co2Comm.get(co2Id);
            if (QCo2 == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for CO2 " + co2Id);
                return;
            } else {

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
        }
    }

    /**
     * Method to update state of channel, called from Qbus CO2.
     */
    public void handleStateUpdate(QbusCO2 QCo2) {
        Integer CO2State = QCo2.getState();
        if (CO2State != null) {
            updateState(CHANNEL_CO2, new DecimalType(CO2State));
            updateStatus(ThingStatus.ONLINE);
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
        QbusBridgeHandler QBridgeHandler = getBridgeHandler("CO2", co2Id);
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        } else {
            this.sn = QBridgeHandler.getSn();
        }
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
        if (config != null) {
            return config.co2Id;
        } else {
            return 0;
        }
    }
}
