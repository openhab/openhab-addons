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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusRol;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
public class QbusRolHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusRolHandler.class);

    public QbusRolHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable QbusThingsConfig config;

    int rolId = 0;

    String sn = "";

    /**
     * Main initialization
     */
    @Override
    public void initialize() {

        setConfig();
        rolId = getId();

        QbusCommunication QComm = getCommunication("Screen/Store", rolId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Screen/Store", rolId);
        if (QBridgeHandler == null) {
            return;
        }

        QbusRol QRol = QComm.getRol().get(rolId);

        sn = QBridgeHandler.getSn();

        if (QRol != null) {
            QRol.setThingHandler(this);
            handleStateUpdate(QRol);
            logger.info("Screen/Store intialized {}", rolId);
        } else {
            logger.warn("Screen/Store not intialized {}", rolId);
        }
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication QComm = getCommunication("Screen/Store", rolId);

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for Screen/Store " + rolId);
            return;
        }

        QbusRol QRol = QComm.getRol().get(rolId);

        if (QRol == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for ROL " + rolId);
            return;
        }

        scheduler.submit(() -> {
            if (!QComm.communicationActive()) {
                restartCommunication(QComm, "Screen/Store", rolId);
            }

            if (QComm.communicationActive()) {

                if (command == REFRESH) {
                    handleStateUpdate(QRol);
                    return;
                }

                switch (channelUID.getId()) {
                    case CHANNEL_ROLLERSHUTTER:
                        handleScreenposCommand(QRol, command);
                        updateStatus(ThingStatus.ONLINE);
                        break;

                    case CHANNEL_SLATS:
                        handleSlatsposCommand(QRol, command);
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
     * Executes the command for screen up/down position
     */
    private void handleScreenposCommand(QbusRol QRol, Command command) {

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

    /**
     * Executes the command for screen slats position
     */
    private void handleSlatsposCommand(QbusRol QRol, Command command) {

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

    /**
     * Method to update state of channel, called from Qbus Screen/Store.
     */
    public void handleStateUpdate(QbusRol qRol) {

        int rolState = qRol.getState().intValue();
        int slatState = qRol.getStateSlats().intValue();

        updateState(CHANNEL_ROLLERSHUTTER, new PercentType(rolState));
        updateState(CHANNEL_SLATS, new PercentType(slatState));

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
     * @return rolId
     */
    @SuppressWarnings("null")
    public int getId() {
        return config.rolId;
    }
}
