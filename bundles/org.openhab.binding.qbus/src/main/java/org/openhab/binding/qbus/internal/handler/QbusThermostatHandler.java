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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QThermostat;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
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
 * The {@link QbusThermostatHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */
@NonNullByDefault
public class QbusThermostatHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusThermostatHandler.class);

    public QbusThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer thermostatId = ((Number) this.getConfig().get(CONFIG_THERMOSTAT_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute thermostat command " + thermostatId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute thermostat command " + thermostatId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Qbus: bridge communication not initialized when trying to execute thermostat command "
                            + thermostatId);
            return;
        }

        QThermostat qThermostat = QComm.getThermostats().get(thermostatId);

        if (qThermostat != null) {
            if (QComm.communicationActive()) {
                handleCommandSelection(qThermostat, channelUID, command);
            } else {
                scheduler.submit(() -> {
                    QComm.restartCommunication();
                    if (!QComm.communicationActive()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Qbus: communication socket error");
                        return;
                    }
                    QBridgeHandler.bridgeOnline();
                    handleCommandSelection(qThermostat, channelUID, command);
                });
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Qbus: thermostatId " + thermostatId + " does not match a THERMOSTAT in the controller");
            return;
        }
    }

    @SuppressWarnings("unchecked")
    private void handleCommandSelection(QThermostat qThermostat, ChannelUID channelUID, Command command) {
        logger.debug("Qbus: handle command {} for {}", command, channelUID);
        @SuppressWarnings("null")
        String sn = getBridge().getConfiguration().get(CONFIG_SN).toString();

        if (REFRESH.equals(command)) {
            handleStateUpdate(qThermostat);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_MEASURED:
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_MODE:
                if (command instanceof DecimalType) {
                    qThermostat.executeMode(((DecimalType) command).intValue(), sn);
                }
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_SETPOINT:
                if (command instanceof QuantityType<?>) {
                    qThermostat.executeSetpoint(((QuantityType<Temperature>) command).doubleValue(), sn);
                }
                updateStatus(ThingStatus.ONLINE);

                break;

            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Qbus: channel unknown " + channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer thermostatId = ((Number) config.get(CONFIG_THERMOSTAT_ID)).intValue();

        Bridge QBrdige = getBridge();
        if (QBrdige == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for thermostat " + thermostatId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBrdige.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for thermostat " + thermostatId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();
        if (QComm == null || !QComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Qbus: no connection with Qbus server, could not initialize thermostat " + thermostatId);
            return;
        }

        QThermostat qThermostat = QComm.getThermostats().get(thermostatId);

        // Map<String, String> properties = new HashMap<>();

        // thing.setProperties(properties);

        if (qThermostat != null) {
            qThermostat.setThingHandler(this);
            handleStateUpdate(qThermostat);
            logger.info("Qbus: Thermostat intialized {}", thermostatId);
        } else {
            logger.info("Qbus: Thermostat not intialized {} - null", thermostatId);
        }
    }

    /**
     * Method to update state of all channels, called from Qbus thermostat.
     *
     * @param qThermostat Qbus thermostat
     *
     */
    public void handleStateUpdate(QThermostat qThermostat) {

        updateState(CHANNEL_MEASURED, new QuantityType<Temperature>(qThermostat.getMeasured(), CELSIUS));

        updateState(CHANNEL_SETPOINT, new QuantityType<Temperature>(qThermostat.getSetpoint(), CELSIUS));

        updateState(CHANNEL_MODE, new DecimalType(qThermostat.getMode()));

        updateStatus(ThingStatus.ONLINE);
    }
}
