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
import java.util.concurrent.ScheduledFuture;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusThermostatHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusThermostatHandler.class);

    protected @Nullable QbusThingsConfig config;

    private @Nullable Integer thermostatId;

    private @Nullable String sn;

    private @Nullable ScheduledFuture<?> pollingJob;

    public QbusThermostatHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        setConfig();
        thermostatId = getId();

        QbusCommunication qComm = getCommunication("Thermostat", thermostatId);
        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Thermostat", thermostatId);
        if (qBridgeHandler == null) {
            return;
        }

        setSN();

        Map<Integer, QbusThermostat> thermostatComm = qComm.getThermostat();

        if (thermostatComm != null) {
            QbusThermostat qThermostat = thermostatComm.get(thermostatId);
            if (qThermostat != null) {
                qThermostat.setThingHandler(this);
                handleStateUpdate(qThermostat);
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
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Thermostat", thermostatId);
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
        QbusCommunication qComm = getCommunication("Thermostat", thermostatId);

        if (qComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for thermostat "
                            + thermostatId);
            return;
        }

        Map<Integer, QbusThermostat> thermostatComm = qComm.getThermostat();

        if (thermostatComm != null) {
            QbusThermostat qThermostat = thermostatComm.get(thermostatId);

            if (qThermostat == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge communication not initialized when trying to execute command for Scene "
                                + thermostatId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Thermostat", thermostatId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qThermostat);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_MODE:
                                handleModeCommand(qThermostat, command);
                                break;

                            case CHANNEL_SETPOINT:
                                handleSetpointCommand(qThermostat, command);
                                break;

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
    private void handleModeCommand(QbusThermostat qThermostat, Command command) {
        String snr = getSN();
        if (command instanceof DecimalType) {
            int mode = ((DecimalType) command).intValue();
            if (snr != null) {
                qThermostat.executeMode(mode, snr);
            } else {
                thingOffline("No serial number configured for  " + thermostatId);
            }
        }
    }

    /**
     * Executes the Setpoint command
     */
    private void handleSetpointCommand(QbusThermostat qThermostat, Command command) {
        String snr = getSN();
        if (command instanceof QuantityType<?>) {
            QuantityType<?> s = (QuantityType<?>) command;
            double sp = s.doubleValue();
            QuantityType<?> spCelcius = s.toUnit(CELSIUS);

            if (snr != null) {
                if (spCelcius != null) {
                    qThermostat.executeSetpoint(sp, snr);
                } else {
                    logger.debug("Could not set setpoint for thermostat (conversion failed)  {}", thermostatId);
                }
            } else {
                thingOffline("No serial number configured for  " + thermostatId);
            }
        }
    }

    /**
     * Method to update state of all channels, called from Qbus thermostat.
     *
     * @param qThermostat Qbus thermostat
     *
     */
    public void handleStateUpdate(QbusThermostat qThermostat) {
        updateState(CHANNEL_MEASURED, new QuantityType<>(qThermostat.getMeasured(), CELSIUS));

        updateState(CHANNEL_SETPOINT, new QuantityType<>(qThermostat.getSetpoint(), CELSIUS));

        updateState(CHANNEL_MODE, new DecimalType(qThermostat.getMode()));

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Read the configuration
     */
    protected synchronized void setConfig() {
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
     * @return thermostatId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig thConfig = this.config;
        if (thConfig != null) {
            if (thConfig.thermostatId != null) {
                return thConfig.thermostatId;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }
}
