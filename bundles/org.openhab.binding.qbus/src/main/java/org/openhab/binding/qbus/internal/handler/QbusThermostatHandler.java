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

import static org.openhab.binding.qbus.internal.QbusBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusThermostatHandler} is responsible for handling the Thermostat outputs of Qbus
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusThermostatHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusThermostatHandler.class);

    protected @Nullable QbusThingsConfig thermostatConfig = new QbusThingsConfig();

    private @Nullable Integer thermostatId;

    private @Nullable String sn;

    public QbusThermostatHandler(Thing thing) {
        super(thing);
    }

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        readConfig();

        this.thermostatId = getId();

        setSN();

        scheduler.submit(() -> {
            QbusCommunication controllerComm;

            if (this.thermostatId != null) {
                controllerComm = getCommunication("Thermostat", this.thermostatId);
            } else {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR, "ID for THERMOSTAT no set! " + this.thermostatId);
                return;
            }

            if (controllerComm == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for THERMOSTAT not known in controller " + this.thermostatId);
                return;
            }

            Map<Integer, QbusThermostat> thermostatlCommLocal = controllerComm.getThermostat();

            QbusThermostat outputLocal = thermostatlCommLocal.get(this.thermostatId);

            if (outputLocal == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "Bridge could not initialize THERMOSTAT ID " + this.thermostatId);
                return;
            }

            outputLocal.setThingHandler(this);
            handleStateUpdate(outputLocal);

            QbusBridgeHandler qBridgeHandler = getBridgeHandler("Thermostat", this.thermostatId);

            if (qBridgeHandler != null) {
                if (qBridgeHandler.getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Bridge offline for THERMOSTAT ID " + this.thermostatId);
                }
            }
        });
    }

    /**
     * Handle the status update from the thermostat
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication qComm = getCommunication("Thermostat", this.thermostatId);

        if (qComm == null) {
            thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                    "ID for THERMOSTAT not known in controller " + this.thermostatId);
            return;
        } else {
            Map<Integer, QbusThermostat> thermostatComm = qComm.getThermostat();

            QbusThermostat qThermostat = thermostatComm.get(this.thermostatId);

            if (qThermostat == null) {
                thingOffline(ThingStatusDetail.CONFIGURATION_ERROR,
                        "ID for THERMOSTAT not known in controller " + this.thermostatId);
                return;
            } else {
                scheduler.submit(() -> {
                    if (!qComm.communicationActive()) {
                        restartCommunication(qComm, "Thermostat", this.thermostatId);
                    }

                    if (qComm.communicationActive()) {
                        if (command == REFRESH) {
                            handleStateUpdate(qThermostat);
                            return;
                        }

                        switch (channelUID.getId()) {
                            case CHANNEL_MODE:
                                try {
                                    handleModeCommand(qThermostat, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Mode for thermostat ID {}. IOException: {} ",
                                            this.thermostatId, message);
                                } catch (InterruptedException e) {
                                    String message = e.getMessage();
                                    logger.warn(
                                            "Error on executing Mode for thermostat ID {}. Interruptedexception {} ",
                                            this.thermostatId, message);
                                }
                                break;

                            case CHANNEL_SETPOINT:
                                try {
                                    handleSetpointCommand(qThermostat, command);
                                } catch (IOException e) {
                                    String message = e.getMessage();
                                    logger.warn("Error on executing Setpoint for thermostat ID {}. IOException: {} ",
                                            this.thermostatId, message);
                                } catch (InterruptedException e) {
                                    String message = e.getMessage();
                                    logger.warn(
                                            "Error on executing Setpoint for thermostat ID {}. Interruptedexception {} ",
                                            this.thermostatId, message);
                                }
                                break;

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
     * Executes the Mode command
     *
     * @param qThermostat
     * @param command
     * @param snr
     * @throws InterruptedException
     * @throws IOException
     */
    private void handleModeCommand(QbusThermostat qThermostat, Command command)
            throws InterruptedException, IOException {
        String snr = getSN();
        if (snr != null) {
            if (command instanceof DecimalType decimalCommand) {
                int mode = decimalCommand.intValue();
                qThermostat.executeMode(mode, snr);
            }
        }
    }

    /**
     * Executes the Setpoint command
     *
     * @param qThermostat
     * @param command
     * @param snr
     * @throws InterruptedException
     * @throws IOException
     */
    private void handleSetpointCommand(QbusThermostat qThermostat, Command command)
            throws InterruptedException, IOException {
        String snr = getSN();
        if (snr != null) {
            if (command instanceof QuantityType<?> quantityCommand) {
                double sp = quantityCommand.doubleValue();
                QuantityType<?> spCelcius = quantityCommand.toUnit(CELSIUS);

                if (spCelcius != null) {
                    qThermostat.executeSetpoint(sp, snr);
                } else {
                    logger.warn("Could not set setpoint for thermostat (conversion failed)  {}", this.thermostatId);
                }
            }
        }
    }

    /**
     * Method to update state of all channels, called from Qbus thermostat.
     *
     * @param qThermostat
     */
    public void handleStateUpdate(QbusThermostat qThermostat) {
        Double measured = qThermostat.getMeasured();
        if (measured != null) {
            updateState(CHANNEL_MEASURED, new QuantityType<>(measured, CELSIUS));
        }

        Double setpoint = qThermostat.getSetpoint();
        if (setpoint != null) {
            updateState(CHANNEL_SETPOINT, new QuantityType<>(setpoint, CELSIUS));
        }

        Integer mode = qThermostat.getMode();
        if (mode != null) {
            updateState(CHANNEL_MODE, new DecimalType(mode));
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
        QbusBridgeHandler qBridgeHandler = getBridgeHandler("Thermostsat", this.thermostatId);
        if (qBridgeHandler == null) {
            thingOffline(ThingStatusDetail.COMMUNICATION_ERROR,
                    "No communication with Qbus Bridge for THERMOSTAT " + this.thermostatId);
            return;
        }
        sn = qBridgeHandler.getSn();
    }

    /**
     * Read the configuration
     */
    protected synchronized void readConfig() {
        thermostatConfig = getConfig().as(QbusThingsConfig.class);
    }

    /**
     * Returns the Id from the configuration
     *
     * @return outputId
     */
    public @Nullable Integer getId() {
        QbusThingsConfig localConfig = thermostatConfig;
        if (localConfig != null) {
            return localConfig.thermostatId;
        } else {
            return null;
        }
    }
}
