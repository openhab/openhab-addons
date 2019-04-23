/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.handler;

import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.*;
import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcActionEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcAction2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlActionHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlActionHandler extends BaseThingHandler implements NhcActionEvent {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlActionHandler.class);

    private volatile @NonNullByDefault({}) NhcAction nhcAction;

    private String actionId = "";
    private int stepValue;

    public NikoHomeControlActionHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Bridge nhcBridge = getBridge();
        if (nhcBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized when trying to execute action " + actionId);
            return;
        }
        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) nhcBridge.getHandler();
        if (nhcBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized when trying to execute action " + actionId);
            return;
        }
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();

        if (nhcComm == null || !nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    "Niko Home Control: bridge communication not initialized when trying to execute action "
                            + actionId);
            return;
        }

        if (nhcAction == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: actionId " + actionId + " does not match an action in the controller");
            return;
        }

        if (nhcComm.communicationActive()) {
            handleCommandSelection(channelUID, command);
        } else {
            // We lost connection but the connection object is there, so was correctly started.
            // Try to restart communication.
            // This can be expensive, therefore do it in a job.
            scheduler.submit(() -> {
                nhcComm.restartCommunication();
                // If still not active, take thing offline and return.
                if (!nhcComm.communicationActive()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                            "Niko Home Control: communication error");
                    return;
                }
                // Also put the bridge back online
                nhcBridgeHandler.bridgeOnline();

                // And finally handle the command
                handleCommandSelection(channelUID, command);
            });
        }
    }

    private void handleCommandSelection(ChannelUID channelUID, Command command) {
        logger.debug("Niko Home Control: handle command {} for {}", command, channelUID);

        if (command == REFRESH) {
            actionEvent(nhcAction.getState());
            return;
        }

        switch (channelUID.getId()) {
            case CHANNEL_BUTTON:
            case CHANNEL_SWITCH:
                handleSwitchCommand(command);
                updateStatus(ThingStatus.ONLINE);
                break;
            case CHANNEL_BRIGHTNESS:
                handleBrightnessCommand(command);
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_ROLLERSHUTTER:
                handleRollershutterCommand(command);
                updateStatus(ThingStatus.ONLINE);
                break;

            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Niko Home Control: channel unknown " + channelUID.getId());
        }
    }

    private void handleSwitchCommand(Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (OnOffType.OFF.equals(s)) {
                nhcAction.execute(NHCOFF);
            } else {
                nhcAction.execute(NHCON);
            }
        }
    }

    private void handleBrightnessCommand(Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (OnOffType.OFF.equals(s)) {
                nhcAction.execute(NHCOFF);
            } else {
                nhcAction.execute(NHCON);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int currentValue = nhcAction.getState();
            int newValue;
            if (IncreaseDecreaseType.INCREASE.equals(s)) {
                newValue = currentValue + stepValue;
                // round down to step multiple
                newValue = newValue - newValue % stepValue;
                nhcAction.execute(Integer.toString(newValue > 100 ? 100 : newValue));
            } else {
                newValue = currentValue - stepValue;
                // round up to step multiple
                newValue = newValue + newValue % stepValue;
                if (newValue <= 0) {
                    nhcAction.execute(NHCOFF);
                } else {
                    nhcAction.execute(Integer.toString(newValue));
                }
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            if (PercentType.ZERO.equals(p)) {
                nhcAction.execute(NHCOFF);
            } else {
                nhcAction.execute(Integer.toString(p.intValue()));
            }
        }
    }

    private void handleRollershutterCommand(Command command) {
        if (command instanceof UpDownType) {
            UpDownType s = (UpDownType) command;
            if (UpDownType.UP.equals(s)) {
                nhcAction.execute(NHCUP);
            } else {
                nhcAction.execute(NHCDOWN);
            }
        } else if (command instanceof StopMoveType) {
            nhcAction.execute(NHCSTOP);
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            nhcAction.execute(Integer.toString(100 - p.intValue()));
        }
    }

    @Override
    public void initialize() {
        NikoHomeControlActionConfig config;
        if (thing.getThingTypeUID().equals(THING_TYPE_DIMMABLE_LIGHT)) {
            config = getConfig().as(NikoHomeControlActionDimmerConfig.class);
            stepValue = ((NikoHomeControlActionDimmerConfig) config).step;
        } else {
            config = getConfig().as(NikoHomeControlActionConfig.class);
        }
        actionId = config.actionId;

        Bridge nhcBridge = getBridge();
        if (nhcBridge == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized for action " + actionId);
            return;
        }
        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) nhcBridge.getHandler();
        if (nhcBridgeHandler == null) {
            updateStatus(ThingStatus.UNINITIALIZED, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "Niko Home Control: no bridge initialized for action " + actionId);
            return;
        }
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();

        // We need to do this in a separate thread because we may have to wait for the communication to become active
        scheduler.submit(() -> {
            if (nhcComm == null || !nhcComm.communicationActive()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Niko Home Control: no connection with Niko Home Control, could not initialize action "
                                + actionId);
                return;
            }

            nhcAction = nhcComm.getActions().get(actionId);
            if (nhcAction == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Niko Home Control: actionId does not match an action in the controller " + actionId);
                return;
            }

            String actionLocation = nhcAction.getLocation();

            nhcAction.setEventHandler(this);

            updateProperties();

            if (thing.getLocation() == null) {
                thing.setLocation(actionLocation);
            }

            actionEvent(nhcAction.getState());

            logger.debug("Niko Home Control: action initialized {}", actionId);
        });
    }

    private void updateProperties() {
        Map<String, String> properties = new HashMap<>();
        properties.put("type", String.valueOf(nhcAction.getType()));
        if (getThing().getThingTypeUID() == THING_TYPE_BLIND) {
            properties.put("timeToOpen", String.valueOf(nhcAction.getOpenTime()));
            properties.put("timeToClose", String.valueOf(nhcAction.getCloseTime()));
        }

        if (nhcAction instanceof NhcAction2) {
            NhcAction2 action = (NhcAction2) nhcAction;
            properties.put("model", action.getModel());
            properties.put("technology", action.getTechnology());
        }

        thing.setProperties(properties);
    }

    @Override
    public void actionEvent(int actionState) {
        ActionType actionType = nhcAction.getType();

        switch (actionType) {
            case TRIGGER:
                updateState(CHANNEL_BUTTON, (actionState == 0) ? OnOffType.OFF : OnOffType.ON);
                updateStatus(ThingStatus.ONLINE);
            case RELAY:
                updateState(CHANNEL_SWITCH, (actionState == 0) ? OnOffType.OFF : OnOffType.ON);
                updateStatus(ThingStatus.ONLINE);
                break;
            case DIMMER:
                updateState(CHANNEL_BRIGHTNESS, new PercentType(actionState));
                updateStatus(ThingStatus.ONLINE);
                break;
            case ROLLERSHUTTER:
                updateState(CHANNEL_ROLLERSHUTTER, new PercentType(actionState));
                updateStatus(ThingStatus.ONLINE);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Niko Home Control: unknown action type " + actionType);
        }
    }
}
