/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.handler;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NikoHomeControlHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Mark Herwege
 */
public class NikoHomeControlHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlHandler.class);

    public NikoHomeControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Integer actionId = ((Number) this.getConfig().get(CONFIG_ACTION_ID)).intValue();

        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) getBridge().getHandler();
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();

        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: bridge communication not initialized when trying to execute action "
                            + actionId);
            return;
        }

        NhcAction nhcAction = nhcComm.getActions().get(actionId);
        if (nhcAction == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: actionId does not match an action in the controller " + actionId);
            return;
        }

        if (nhcComm.communicationActive()) {
            handleCommandSelection(nhcAction, channelUID, command);
        } else {
            // We lost connection but the connection object is there, so was correctly started.
            // Try to restart communication.
            // This can be expensive, therefore do it in a job.
            scheduler.submit(new Runnable() {

                @Override
                public void run() {
                    nhcComm.restartCommunication();
                    // If still not active, take thing offline and return.
                    if (!nhcComm.communicationActive()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Niko Home Control: communication socket error");
                        return;
                    }
                    // Also put the bridge back online
                    nhcBridgeHandler.bridgeOnline();

                    // And finally handle the command
                    handleCommandSelection(nhcAction, channelUID, command);
                }
            });
        }
    }

    private void handleCommandSelection(NhcAction nhcAction, ChannelUID channelUID, Command command) {
        logger.debug("Niko Home Control: handle command {} for {}", command, channelUID);

        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                handleSwitchCommand(nhcAction, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_BRIGHTNESS:
                handleBrightnessCommand(nhcAction, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_ROLLERSHUTTER:
                handleRollershutterCommand(nhcAction, command);
                updateStatus(ThingStatus.ONLINE);
                break;

            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Niko Home Control: channel unknown " + channelUID.getId());
        }
    }

    private void handleSwitchCommand(NhcAction nhcAction, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                nhcAction.execute(0);
            } else {
                nhcAction.execute(100);
            }
        }
    }

    private void handleBrightnessCommand(NhcAction nhcAction, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                nhcAction.execute(255);
            } else {
                nhcAction.execute(254);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            int currentValue = nhcAction.getState();
            int newValue;
            if (s == IncreaseDecreaseType.INCREASE) {
                newValue = currentValue + stepValue;
                // round down to step multiple
                newValue = newValue - newValue % stepValue;
                nhcAction.execute(newValue > 100 ? 100 : newValue);
            } else {
                newValue = currentValue - stepValue;
                // round up to step multiple
                newValue = newValue + newValue % stepValue;
                nhcAction.execute(newValue < 0 ? 0 : newValue);
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            nhcAction.execute(p.intValue());
        }
    }

    private void handleRollershutterCommand(NhcAction nhcAction, Command command) {
        if (command instanceof UpDownType) {
            UpDownType s = (UpDownType) command;
            if (s == UpDownType.UP) {
                nhcAction.execute(255);
            } else {
                nhcAction.execute(254);
            }
        } else if (command instanceof StopMoveType) {
            StopMoveType s = (StopMoveType) command;
            if (s == StopMoveType.STOP) {
                nhcAction.execute(253);
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            int currentState = nhcAction.getState();
            if (currentState < p.intValue()) {
                nhcAction.execute(255);
            } else if (currentState > p.intValue()) {
                nhcAction.execute(254);
            }
        }
    }

    @Override
    public void initialize() {
        Configuration config = this.getConfig();

        Integer actionId = ((Number) config.get(CONFIG_ACTION_ID)).intValue();

        NikoHomeControlBridgeHandler nhcBridgeHandler = (NikoHomeControlBridgeHandler) getBridge().getHandler();
        NikoHomeControlCommunication nhcComm = nhcBridgeHandler.getCommunication();
        if (nhcComm == null || !nhcComm.communicationActive()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Niko Home Control: no connection with Niko Home Control, could not initialize action " + actionId);
            return;
        }

        NhcAction nhcAction = nhcComm.getActions().get(actionId);
        if (nhcAction == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: actionId does not match an action in the controller " + actionId);
            return;
        }

        int actionState = nhcAction.getState();
        int actionType = nhcAction.getType();
        String actionLocation = nhcAction.getLocation();

        nhcAction.setThingHandler(this);

        handleStateUpdate(actionType, actionState);
        logger.debug("Niko Home Control: action intialized {}", actionId);

        if (thing.getLocation() == null) {
            thing.setLocation(actionLocation);
        }
    }

    public void handleStateUpdate(int actionType, int actionState) {
        switch (actionType) {
            case 0:
            case 1:
                updateState(CHANNEL_SWITCH, (actionState == 0) ? OnOffType.OFF : OnOffType.ON);
                updateStatus(ThingStatus.ONLINE);
                break;
            case 2:
                updateState(CHANNEL_BRIGHTNESS, new PercentType(actionState));
                updateStatus(ThingStatus.ONLINE);
                break;
            case 4:
            case 5:
                updateState(CHANNEL_ROLLERSHUTTER, new PercentType(actionState));
                updateStatus(ThingStatus.ONLINE);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Niko Home Control: unknown action type " + actionType);
        }
    }
}
