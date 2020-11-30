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
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusRol;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
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
 * The {@link QbusRolHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */
@NonNullByDefault
public class QbusRolHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusRolHandler.class);

    // private volatile int prevRolState;
    // private volatile int prevSlatState;

    public QbusRolHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer RolId = ((Number) this.getConfig().get(CONFIG_ROLLERSHUTTER_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute Slats " + RolId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute Slats " + RolId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Qbus: bridge communication not initialized when trying to execute Slats " + RolId);
            return;
        }

        QbusRol QRol = QComm.getRol().get(RolId);

        if (QComm.communicationActive()) {
            handleCommandSelection(QRol, channelUID, command);
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
                handleCommandSelection(QRol, channelUID, command);
            });
        }
    }

    private void handleCommandSelection(QbusRol qRol, ChannelUID channelUID, Command command) {
        logger.debug("Qbus: handle command {} for {}", command, channelUID);

        if (command == REFRESH) {
            handleStateUpdate(qRol);
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_ROLLERSHUTTER:
                handleBrightnessCommand(qRol, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_SLATS:
                handleSlatsCommand(qRol, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Qbus: channel unknown " + channelUID.getId());
        }
    }

    private void handleBrightnessCommand(QbusRol QRol, Command command) {
        @SuppressWarnings("null")
        String sn = getBridge().getConfiguration().get(CONFIG_SN).toString();

        if (command instanceof org.openhab.core.library.types.UpDownType) {
            org.openhab.core.library.types.UpDownType s = (org.openhab.core.library.types.UpDownType) command;
            if (s == org.openhab.core.library.types.UpDownType.DOWN) {
                QRol.execute(0, sn);
            } else {
                QRol.execute(100, sn);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            int currentValue = QRol.getState();
            int newValue;
            if (s == IncreaseDecreaseType.INCREASE) {
                newValue = currentValue + stepValue;
                // round down to step multiple
                newValue = newValue - newValue % stepValue;
                QRol.execute(newValue > 100 ? 100 : newValue, sn);
            } else {
                newValue = currentValue - stepValue;
                // round up to step multiple
                newValue = newValue + newValue % stepValue;
                QRol.execute(newValue < 0 ? 0 : newValue, sn);
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            if (p == PercentType.ZERO) {
                QRol.execute(0, sn);
            } else {
                QRol.execute(p.intValue(), sn);
            }
        }
    }

    private void handleSlatsCommand(QbusRol QRol, Command command) {
        @SuppressWarnings("null")
        String sn = getBridge().getConfiguration().get(CONFIG_SN).toString();

        if (command instanceof org.openhab.core.library.types.UpDownType) {
            org.openhab.core.library.types.UpDownType s = (org.openhab.core.library.types.UpDownType) command;
            if (s == org.openhab.core.library.types.UpDownType.DOWN) {
                QRol.executeSlats(0, sn);
            } else {
                QRol.executeSlats(100, sn);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            int currentValue = QRol.getState();
            int newValue;
            if (s == IncreaseDecreaseType.INCREASE) {
                newValue = currentValue + stepValue;
                // round down to step multiple
                newValue = newValue - newValue % stepValue;
                QRol.executeSlats(newValue > 100 ? 100 : newValue, sn);
            } else {
                newValue = currentValue - stepValue;
                // round up to step multiple
                newValue = newValue + newValue % stepValue;
                QRol.executeSlats(newValue < 0 ? 0 : newValue, sn);
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            if (p == PercentType.ZERO) {
                QRol.executeSlats(0, sn);
            } else {
                QRol.executeSlats(p.intValue(), sn);
            }
        }
    }

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer RolId = ((Number) config.get(CONFIG_ROLLERSHUTTER_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for Slats " + RolId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for Slats " + RolId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();
        if (QComm == null || !QComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Qbus: no connection with Qbus server, could not initialize Slats " + RolId);
            return;
        }

        QbusRol QRol = QComm.getRol().get(RolId);
        /*
         * int rolState = QRol.getState();
         * int slatState = QRol.getStateSlats();
         *
         * this.prevRolState = rolState;
         * this.prevSlatState = slatState;
         *
         */
        QRol.setThingHandler(this);

        Map<String, String> properties = new HashMap<>();

        thing.setProperties(properties);

        handleStateUpdate(QRol);

        logger.debug("Qbus: Slats intialized {}", RolId);
    }

    /**
     * Method to update state of channel, called from Qbus Slats.
     */
    public void handleStateUpdate(QbusRol qRol) {

        int rolState = qRol.getState().intValue();
        int slatState = qRol.getStateSlats().intValue();

        updateState(CHANNEL_ROLLERSHUTTER, new PercentType(rolState));
        updateState(CHANNEL_SLATS, new PercentType(slatState));
        updateStatus(ThingStatus.ONLINE);

        // this.prevRolState = rolState;
        // this.prevSlatState = slatState;
    }
}
