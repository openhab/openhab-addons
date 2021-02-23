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
    protected @Nullable QbusThingsConfig config;

    private int co2Id;

    private @Nullable String sn;

    protected @Nullable QbusThingsConfig config;

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        setConfig();
        co2Id = getId();

        QbusCommunication qComm = getCommunication("CO2", co2Id);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler qBridgeHandler = getBridgeHandler("CO2", co2Id);
        if (qBridgeHandler == null) {
            return;
        }

        setSN();

        Map<Integer, QbusCO2> co2Comm = qComm.getCo2();

        if (co2Comm != null) {
            QbusCO2 qCo2 = co2Comm.get(co2Id);
            if (qCo2 != null) {
                qCo2.setThingHandler(this);
                handleStateUpdate(qCo2);
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
        QbusCommunication qComm = getCommunication("CO2", co2Id);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for CO2 " + co2Id);
            return;
        }

        Map<Integer, QbusCO2> co2Comm = qComm.getCo2();

        if (co2Comm != null) {
            QbusCO2 qCo2 = co2Comm.get(co2Id);
            if (qCo2 == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for CO2 " + co2Id);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "CO2", co2Id);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qCo2);
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
    public void handleStateUpdate(QbusCO2 qCo2) {
        Integer co2State = qCo2.getState();
        if (co2State != null) {
            updateState(CHANNEL_CO2, new DecimalType(co2State));
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
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("CO2", co2Id);
        if (qBridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }
        this.sn = qBridgeHandler.getSn();
    }

    /**
     * Read the configuration
     */
    protected synchronized void setConfig() {
        this.config = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return co2Id
     */
    public int getId() {
        if (this.config != null) {
            return this.config.co2Id;
        } else {
            return 0;
        }
    }
}
