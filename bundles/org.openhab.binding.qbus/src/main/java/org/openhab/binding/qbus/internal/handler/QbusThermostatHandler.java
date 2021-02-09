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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusThermostat;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;

/**
 * The {@link QbusThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusThermostatHandler extends QbusGlobalHandler {

    public QbusThermostatHandler(Thing thing) {
        super(thing);
    }

    protected @NonNullByDefault({}) QbusThingsConfig config;

    int thermostatId;

    @Nullable
    String sn;

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        setConfig();
        thermostatId = getId();

        QbusCommunication QComm = getCommunication("Thermostat", thermostatId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Thermostat", thermostatId);
        if (QBridgeHandler == null) {
            return;
        }

        setSN();

        Map<Integer, QbusThermostat> thermostatComm = QComm.getThermostat();

        if (thermostatComm != null) {
            QbusThermostat QThermostat = thermostatComm.get(thermostatId);
            if (QThermostat != null) {
                QThermostat.setThingHandler(this);
                handleStateUpdate(QThermostat);
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
        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Thermostat", thermostatId);
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
        QbusCommunication QComm = getCommunication("Thermostat", thermostatId);

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for thermostat "
                            + thermostatId);
            return;
        }

        Map<Integer, QbusThermostat> thermostatComm = QComm.getThermostat();

        if (thermostatComm != null) {

            QbusThermostat QThermostat = thermostatComm.get(thermostatId);

            if (QThermostat == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for Scene "
                                + thermostatId);
                return;
            } else {

                scheduler.submit(() -> {
                    if (!QComm.communicationActive()) {
                        restartCommunication(QComm, "Thermostat", thermostatId);
                    }

                    if (QComm.communicationActive()) {

                        if (command == REFRESH) {
                            handleStateUpdate(QThermostat);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_MEASURED:
                                updateStatus(ThingStatus.ONLINE);
                                break;

                            case CHANNEL_MODE:
                                handleModeCommand(QThermostat, command);
                                updateStatus(ThingStatus.ONLINE);
                                break;

                            case CHANNEL_SETPOINT:
                                handleSetpointCommand(QThermostat, command);
                                updateStatus(ThingStatus.ONLINE);
                                break;

                            default:
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "Channel unknown " + channelUID.getId());
                        }
                    }
                });
            }
        }
    }

    /**
     * Puts thing offline
     *
     * @param message
     */
    public void thingOffline(String message) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, message);
    }

    /**
     * Executes the Mode command
     */
    private void handleModeCommand(QbusThermostat QThermostat, Command command) {
        @Nullable
        String snr = getSN();
        if (command instanceof DecimalType) {
            int mode = ((DecimalType) command).intValue();
            if (snr != null) {
                QThermostat.executeMode(mode, snr);
            } else {
                thingOffline("No serial number configured for  " + thermostatId);
            }
        }
    }

    /**
     * Executes the Setpoint command
     */
    private void handleSetpointCommand(QbusThermostat QThermostat, Command command) {
        @Nullable
        String snr = getSN();
        if (command instanceof QuantityType<?>) {
            QuantityType<?> s = (QuantityType<?>) command;
            double sp = s.doubleValue();
            if (snr != null) {
                QThermostat.executeSetpoint(sp, snr);
            } else {
                thingOffline("No serial number configured for  " + thermostatId);
            }
        }
    }

    /**
     * Method to update state of all channels, called from Qbus thermostat.
     *
     * @param QThermostat Qbus thermostat
     *
     */
    public void handleStateUpdate(QbusThermostat QThermostat) {

        updateState(CHANNEL_MEASURED, new QuantityType<>(QThermostat.getMeasured(), CELSIUS));

        updateState(CHANNEL_SETPOINT, new QuantityType<>(QThermostat.getSetpoint(), CELSIUS));

        updateState(CHANNEL_MODE, new DecimalType(QThermostat.getMode()));

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
     * @return dimmerId
     */
    public int getId() {
        if (config != null) {
            return config.thermostatId;
        } else {
            return 0;
        }
    }
}
