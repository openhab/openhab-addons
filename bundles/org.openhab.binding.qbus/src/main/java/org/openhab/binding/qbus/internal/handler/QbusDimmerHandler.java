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
import org.openhab.binding.qbus.internal.protocol.QbusDimmer;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link QbusDimmerHandler} is responsible for handling the dimmable outputs of Qbus
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public class QbusDimmerHandler extends QbusGlobalHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusDimmerHandler.class);

    public QbusDimmerHandler(Thing thing) {
        super(thing);
    }

    protected @Nullable QbusThingsConfig config;

    int dimmerId = 0;

    String sn = "";

    /**
     * Main initialisation
     */
    @Override
    public void initialize() {
        setConfig();
        dimmerId = getId();

        QbusCommunication QComm = getCommunication("Dimmer", dimmerId);
        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "No communication with Qbus Bridge!");
            return;
        }

        QbusBridgeHandler QBridgeHandler = getBridgeHandler("Dimmer", dimmerId);
        if (QBridgeHandler == null) {
            return;
        }

        QbusDimmer QDimmer = QComm.getDimmer().get(dimmerId);

        sn = QBridgeHandler.getSn();

        if (QDimmer != null) {
            QDimmer.setThingHandler(this);
            handleStateUpdate(QDimmer);
            logger.info("Dimmer intialized {}", dimmerId);
        } else {
            logger.warn("Dimmer not intialized {}", dimmerId);
        }
    }

    /**
     * Handle the status update from the thing
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        QbusCommunication QComm = getCommunication("Dimmer", dimmerId);

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for dimmer " + dimmerId);
            return;
        }

        QbusDimmer QDimmer = QComm.getDimmer().get(dimmerId);

        if (QDimmer == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Bridge communication not initialized when trying to execute command for dimmer " + dimmerId);
            return;
        }

        scheduler.submit(() -> {
            if (!QComm.communicationActive()) {
                restartCommunication(QComm, "Dimmer", dimmerId);
            }

            if (QComm.communicationActive()) {

                if (command == REFRESH) {
                    handleStateUpdate(QDimmer);
                    return;
                }

                switch (channelUID.getId()) {
                    case CHANNEL_SWITCH:
                        handleSwitchCommand(QDimmer, command);
                        updateStatus(ThingStatus.ONLINE);
                        break;

                    case CHANNEL_BRIGHTNESS:
                        handleBrightnessCommand(QDimmer, command);
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
     * Executes the switch command
     */
    private void handleSwitchCommand(QbusDimmer QDimmer, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                QDimmer.execute(0, sn);
            } else {
                QDimmer.execute(100, sn);
            }
        }
    }

    /**
     * Executes the brightness command
     */
    private void handleBrightnessCommand(QbusDimmer QDimmer, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                QDimmer.execute(0, sn);
            } else {
                QDimmer.execute(100, sn);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            int currentValue = QDimmer.getState();
            int newValue;
            if (s == IncreaseDecreaseType.INCREASE) {
                newValue = currentValue + stepValue;
                // round down to step multiple
                newValue = newValue - newValue % stepValue;
                QDimmer.execute(newValue > 100 ? 100 : newValue, sn);
            } else {
                newValue = currentValue - stepValue;
                // round up to step multiple
                newValue = newValue + newValue % stepValue;
                QDimmer.execute(newValue < 0 ? 0 : newValue, sn);
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            if (p == PercentType.ZERO) {
                QDimmer.execute(0, sn);
            } else {
                QDimmer.execute(p.intValue(), sn);
            }
        }
    }

    /**
     * Method to update state of channel, called from Qbus Dimmer.
     */
    public void handleStateUpdate(QbusDimmer QDimmer) {

        int dimmerState = QDimmer.getState();

        updateState(CHANNEL_BRIGHTNESS, new PercentType(dimmerState));

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
        return config.dimmerId;
    }
}
