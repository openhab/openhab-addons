/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.handler;

import static org.openhab.binding.nikohomecontrol.NikoHomeControlBindingConstants.*;

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

    private NikoHomeControlBridgeHandler nhcBridgeHandler = null;
    private NikoHomeControlCommunication nhcComm = null;

    public NikoHomeControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Niko Home Control: communication object is null, could not handle command " + command + " for "
                            + channelUID);
            return;
        }

        int actionID = (Integer) thing.getConfiguration().get(CONFIG_ACTION_ID);

        logger.debug("Niko Home Control: handle command {} for {}", command, channelUID);

        switch (channelUID.getId()) {

            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    if (s == OnOffType.OFF) {
                        nhcComm.executeAction(actionID, 0);
                    } else {
                        nhcComm.executeAction(actionID, 100);
                    }
                }
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_BRIGHTNESS:
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    if (s == OnOffType.OFF) {
                        nhcComm.executeAction(actionID, 255);
                    } else {
                        nhcComm.executeAction(actionID, 254);
                    }
                } else if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType s = (IncreaseDecreaseType) command;
                    int stepValue = (Integer) thing.getConfiguration().get(CONFIG_STEP_VALUE);
                    int currentValue = nhcComm.getActionState(actionID);
                    int newValue;
                    if (s == IncreaseDecreaseType.INCREASE) {
                        newValue = currentValue + stepValue;
                        // round down to nearest step multiple
                        newValue = newValue - newValue % stepValue;
                        nhcComm.executeAction(actionID, (newValue > 100 ? 100 : newValue));
                    } else {
                        newValue = currentValue - stepValue;
                        // round up to nearest step multiple
                        newValue = newValue + newValue % stepValue;
                        nhcComm.executeAction(actionID, (newValue < 0 ? 0 : newValue));
                    }
                } else if (command instanceof PercentType) {
                    PercentType p = (PercentType) command;
                    nhcComm.executeAction(actionID, p.intValue());
                }
                updateStatus(ThingStatus.ONLINE);
                break;

            case CHANNEL_ROLLERSHUTTER:
                if (command instanceof UpDownType) {
                    UpDownType s = (UpDownType) command;
                    if (s == UpDownType.UP) {
                        nhcComm.executeAction(actionID, 255);
                        logger.debug("Niko Home Control: rollershutter {} up pressed", actionID);
                    } else {
                        nhcComm.executeAction(actionID, 254);
                        logger.debug("Niko Home Control: rollershutter {} down pressed", actionID);
                    }
                } else if (command instanceof StopMoveType) {
                    StopMoveType s = (StopMoveType) command;
                    if (s == StopMoveType.STOP) {
                        nhcComm.executeAction(actionID, 253);
                        logger.debug("Niko Home Control: rollershutter {} stop pressed", actionID);
                    } else {
                        logger.debug("Niko Home Control: rollershutter {} move pressed", actionID);
                    }
                } else if (command instanceof PercentType) {
                    PercentType p = (PercentType) command;
                    nhcComm.executeAction(actionID, p.intValue());
                    logger.debug("Niko Home Control: rollershutter {} percent {}", actionID, p.intValue());
                }
                updateStatus(ThingStatus.ONLINE);
                break;

            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Niko Home Control: channel unknown " + channelUID.getId());
        }
    }

    @Override
    public void initialize() {

        int actionID;
        Integer actionIdInteger = null;
        if (thing.getConfiguration().containsKey(CONFIG_ACTION_ID)) {
            actionIdInteger = (Integer) thing.getConfiguration().get(CONFIG_ACTION_ID);
        }
        if (actionIdInteger != null) {
            actionID = actionIdInteger;
        } else {
            // by default try the ID of the thing, discovery would set this to the Niko Home Control ID
            actionID = Integer.parseInt(thing.getUID().getId());
            thing.getConfiguration().put(CONFIG_ACTION_ID, actionID);
        }

        nhcBridgeHandler = (NikoHomeControlBridgeHandler) getBridge().getHandler();
        nhcComm = nhcBridgeHandler.getCommunication();

        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Niko Home Control: communication object is null, could not initialize action " + actionID);
            return;
        }

        if (nhcComm.getActions().contains(actionID)) {

            Integer actionState = nhcComm.getActionState(actionID);
            Integer actionType = nhcComm.getActionType(actionID);
            String actionLocation = nhcComm.getLocationName(nhcComm.getActionLocation(actionID));

            nhcComm.setActionThingHandler(actionID, this);

            switch (actionType) {
                case 0:
                case 1:
                    updateState(CHANNEL_SWITCH, (actionState == 0) ? OnOffType.OFF : OnOffType.ON);
                    logger.debug("Niko Home Control: switch intialized {}", actionID);
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case 2:
                    updateState(CHANNEL_BRIGHTNESS, new PercentType(actionState));
                    logger.debug("Niko Home Control: dimmer intialized {}", actionID);
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case 4:
                case 5:
                    updateState(CHANNEL_ROLLERSHUTTER, new PercentType(actionState));
                    logger.debug("Niko Home Control: rollershutter intialized {}", actionID);
                    updateStatus(ThingStatus.ONLINE);
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Niko Home Control: unknown action type " + actionType);
            }
            thing.setLocation(actionLocation);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: thing ID does not match an action in the controller " + actionID);
        }

    }

    public void handleStateUpdate(int actionType, int actionState) {

        logger.debug("Niko Home Control: handle state update {} for {}", actionState,
                thing.getConfiguration().get(CONFIG_ACTION_ID));

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
