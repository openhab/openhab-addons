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
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.types.RefreshType.REFRESH;

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

    public QbusThermostatHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable QbusThingsConfig config;

    int thermostatId = 0;

    String sn = "";

    /**
     * Main initialization
     */
    @Override
    public void initialize() {
        setConfig();
        thermostatId = getId();

        QbusCommunication QComm = getCommunication("Thermostat", thermostatId);
        if (QComm == null) {
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Thermostat", thermostatId);
        if (QBridgeHandler == null) {
            return;
        }

        QbusThermostat QThermostat = QComm.getThermostats().get(thermostatId);

        sn = QBridgeHandler.getSn();

        if (QThermostat != null) {
            QThermostat.setThingHandler(this);
            handleStateUpdate(QThermostat);
            logger.info("Thermostat intialized {}", thermostatId);
        } else {
            logger.info("Thermostat not intialized {}", thermostatId);
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

        QbusThermostat QThermostat = QComm.getThermostats().get(thermostatId);

        if (QThermostat == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for Scene " + thermostatId);
            return;
        }

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

    /**
     * Executes the Mode command
     */
    private void handleModeCommand(QbusThermostat QThermostat, Command command) {

        if (command instanceof DecimalType) {
            QThermostat.executeMode(((DecimalType) command).intValue(), sn);
        }
    }

    /**
     * Executes the Setpoint command
     */
    private void handleSetpointCommand(QbusThermostat QThermostat, Command command) {

        if (command instanceof QuantityType<?>) {
            QuantityType<?> s = (QuantityType<?>) command;
            QThermostat.executeSetpoint(s.doubleValue(), sn);
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
    @SuppressWarnings("null")
    public int getId() {
        return config.thermostatId;
    }
}
