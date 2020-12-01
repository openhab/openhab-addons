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
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.binding.qbus.internal.protocol.QbusCommunication;
import org.openhab.binding.qbus.internal.protocol.QbusDimmer;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
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
 * The {@link QbusDimmerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Koen Schockaert - Initial Contribution
 */
@NonNullByDefault
public class QbusDimmerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusDimmerHandler.class);

    public QbusDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer dimmerId = ((Number) this.getConfig().get(CONFIG_DIMMER_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute dimmer " + dimmerId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized when trying to execute dimmer " + dimmerId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();

        if (QComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Qbus: bridge communication not initialized when trying to execute dimmer " + dimmerId);
            return;
        }

        QbusDimmer QDimmer = QComm.getDimmer().get(dimmerId);

        if (QDimmer != null) {
            if (QComm.communicationActive()) {
                handleCommandSelection(QDimmer, channelUID, command);
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
                    handleCommandSelection(QDimmer, channelUID, command);
                });
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Qbus: dimmerId " + dimmerId + " does not match a DIMMER in the controller");
            return;
        }
    }

    private void handleCommandSelection(QbusDimmer QDimmer, ChannelUID channelUID, Command command) {
        logger.debug("Qbus: handle command {} for {}", command, channelUID);

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
                        "Qbus: channel unknown " + channelUID.getId());
        }
    }

    private void handleSwitchCommand(QbusDimmer QDimmer, Command command) {

        @SuppressWarnings("null")
        String sn = getBridge().getConfiguration().get(CONFIG_SN).toString();

        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                QDimmer.execute(0, sn);
            } else {
                QDimmer.execute(100, sn);
            }
        }
    }

    private void handleBrightnessCommand(QbusDimmer QDimmer, Command command) {
        // Bridge QBridge = getBridge();
        @SuppressWarnings("null")
        String sn = getBridge().getConfiguration().get(CONFIG_SN).toString();

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

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer dimmerId = ((Number) config.get(CONFIG_DIMMER_ID)).intValue();

        Bridge QBridge = getBridge();
        if (QBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for dimmer " + dimmerId);
            return;
        }
        QbusBridgeHandler QBridgeHandler = (QbusBridgeHandler) QBridge.getHandler();
        if (QBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Qbus: no bridge initialized for dimmer " + dimmerId);
            return;
        }
        QbusCommunication QComm = QBridgeHandler.getCommunication();
        if (QComm == null || !QComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Qbus: no connection with Qbus, could not initialize dimmer " + dimmerId);
            return;
        }

        QbusDimmer QDimmer = QComm.getDimmer().get(dimmerId);

        // Map<String, String> properties = new HashMap<>();

        // thing.setProperties(properties);

        if (QDimmer != null) {
            QDimmer.setThingHandler(this);
            handleStateUpdate(QDimmer);
            logger.info("Qbus: Dimmer intialized {}", dimmerId);
        } else {
            logger.info("Qbus: Dimmer not intialized {} - null", dimmerId);
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
}
