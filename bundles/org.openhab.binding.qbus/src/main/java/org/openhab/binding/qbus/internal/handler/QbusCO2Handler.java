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

    protected @Nullable QbusThingsConfig co2Config = new QbusThingsConfig();

    private @Nullable Integer co2Id;

    private @Nullable String sn;

    public QbusCO2Handler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        readConfig();

        this.co2Id = getId();

        setSN();

        scheduler.submit(() -> {
            QbusCommunication controllerComm;

            if (this.co2Id != null) {
                controllerComm = getCommunication("CO2", this.co2Id);
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for CO2 no set! " + this.co2Id);
                return;
            }

            if (controllerComm == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for CO2 not known in controller " + this.co2Id);
                return;
            }

            Map<Integer, QbusCO2> co2CommLocal = controllerComm.getCo2();

            QbusCO2 outputLocal = co2CommLocal.get(this.co2Id);

            if (outputLocal == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Bridge could not initialize CO2 ID " + this.co2Id);
                return;
            }

            outputLocal.setThingHandler(this);
            handleStateUpdate(outputLocal);

            QbusBridgeHandler qBridgeHandler = getBridgeHandler("CO2", this.co2Id);

            if (qBridgeHandler != null) {
                if (qBridgeHandler.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Bridge offline for CO2 ID " + this.co2Id);
                }
            }
        });
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("CO2", this.co2Id);

        if (qComm == null) {
            thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for CO2 not known in controller " + this.co2Id);
            return;
        } else {
            Map<Integer, QbusCO2> co2Comm = qComm.getCo2();

            QbusCO2 qCo2 = co2Comm.get(this.co2Id);

            if (qCo2 == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for CO2 not known in controller " + this.co2Id);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "CO2", this.co2Id);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qCo2);
                            return;
                        }

                        switch (channelUID.getId()) {
                            default:
                                thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Unknown Channel " + channelUID.getId());
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
        }
    }

    /**
     * Returns the serial number of the controller
     *
     * @return the serial nr
     */
    public @Nullable String getSN() {
        return sn;
    }

    /**
     * Sets the serial number of the controller
     */
    public void setSN() {
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("CO2", this.co2Id);
        if (qBridgeHandler == null) {
            thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication with Qbus Bridge for CO2 " + this.co2Id);
            return;
        }
        sn = qBridgeHandler.getSn();
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        co2Config = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return outputId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig localConfig = this.co2Config;
        if (localConfig != null) {
            return localConfig.co2Id;
        } else {
            return null;
        }
    }
}
