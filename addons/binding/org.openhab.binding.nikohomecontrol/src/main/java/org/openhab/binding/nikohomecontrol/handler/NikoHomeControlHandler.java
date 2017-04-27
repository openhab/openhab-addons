/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

    private NikoHomeControlBridgeHandler nhcBridgeHandler;
    private NikoHomeControlCommunication nhcComm;

    public NikoHomeControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Niko Home Control: no connection with Niko Home Control, could not handle command " + command
                            + " for " + channelUID);
            return;
        }

        Integer actionId = ((Number) this.getConfig().get(CONFIG_ACTION_ID)).intValue();

        if (nhcComm.getActions().contains(actionId)) {

            logger.debug("Niko Home Control: handle command {} for {}", command, channelUID);

            switch (channelUID.getId()) {

                case CHANNEL_SWITCH:
                    handleSwitchCommand(actionId, command);
                    updateStatus(ThingStatus.ONLINE);
                    break;

                case CHANNEL_BRIGHTNESS:
                    handleBrightnessCommand(actionId, command);
                    updateStatus(ThingStatus.ONLINE);
                    break;

                case CHANNEL_ROLLERSHUTTER:
                    handleRollershutterCommand(actionId, command);
                    updateStatus(ThingStatus.ONLINE);
                    break;

                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Niko Home Control: channel unknown " + channelUID.getId());
            }

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: ACTIONID does not match an action in the controller " + actionId);
        }
    }

    private void handleSwitchCommand(Integer actionId, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                nhcComm.executeAction(actionId, 0);
            } else {
                nhcComm.executeAction(actionId, 100);
            }
        }
    }

    private void handleBrightnessCommand(Integer actionId, Command command) {
        if (command instanceof OnOffType) {
            OnOffType s = (OnOffType) command;
            if (s == OnOffType.OFF) {
                nhcComm.executeAction(actionId, 255);
            } else {
                nhcComm.executeAction(actionId, 254);
            }
        } else if (command instanceof IncreaseDecreaseType) {
            IncreaseDecreaseType s = (IncreaseDecreaseType) command;
            int stepValue = ((Number) this.getConfig().get(CONFIG_STEP_VALUE)).intValue();
            int currentValue = nhcComm.getActionState(actionId);
            int newValue;
            if (s == IncreaseDecreaseType.INCREASE) {
                newValue = currentValue + stepValue;
                // round down to step multiple
                newValue = newValue - newValue % stepValue;
                nhcComm.executeAction(actionId, (newValue > 100 ? 100 : newValue));
            } else {
                newValue = currentValue - stepValue;
                // round up to step multiple
                newValue = newValue + newValue % stepValue;
                nhcComm.executeAction(actionId, (newValue < 0 ? 0 : newValue));
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            nhcComm.executeAction(actionId, p.intValue());
        }
    }

    private void handleRollershutterCommand(Integer actionId, Command command) {
        if (command instanceof UpDownType) {
            UpDownType s = (UpDownType) command;
            if (s == UpDownType.UP) {
                nhcComm.executeAction(actionId, 255);
                logger.debug("Niko Home Control: rollershutter {} up pressed", actionId);
            } else {
                nhcComm.executeAction(actionId, 254);
                logger.debug("Niko Home Control: rollershutter {} down pressed", actionId);
            }
        } else if (command instanceof StopMoveType) {
            StopMoveType s = (StopMoveType) command;
            if (s == StopMoveType.STOP) {
                nhcComm.executeAction(actionId, 253);
                logger.debug("Niko Home Control: rollershutter {} stop pressed", actionId);
            } else {
                logger.debug("Niko Home Control: rollershutter {} move pressed", actionId);
            }
        } else if (command instanceof PercentType) {
            PercentType p = (PercentType) command;
            nhcComm.executeAction(actionId, p.intValue());
            logger.debug("Niko Home Control: rollershutter {} percent {}", actionId, p.intValue());
        }
    }

    @Override
    public void initialize() {

        Configuration config = this.getConfig();

        Integer actionId = null;
        if (config.containsKey(CONFIG_ACTION_ID)) {
            actionId = ((Number) config.get(CONFIG_ACTION_ID)).intValue();
        }
        if (actionId == null) {
            // By default try the ID of the thing, discovery would have set this to the Niko Home Control ID.
            actionId = Integer.parseInt(thing.getUID().getId());
            config.put(CONFIG_ACTION_ID, actionId);
        }

        nhcBridgeHandler = (NikoHomeControlBridgeHandler) getBridge().getHandler();
        nhcComm = nhcBridgeHandler.getCommunication();

        if (nhcComm == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Niko Home Control: no connection with Niko Home Control, could not initialize action " + actionId);
            return;
        }

        if (nhcComm.getActions().contains(actionId)) {

            int actionState = nhcComm.getActionState(actionId);
            int actionType = nhcComm.getActionType(actionId);
            String actionLocation = nhcComm.getLocationName(nhcComm.getActionLocation(actionId));

            nhcComm.setActionThingHandler(actionId, this);

            switch (actionType) {
                case 0:
                case 1:
                    updateState(CHANNEL_SWITCH, (actionState == 0) ? OnOffType.OFF : OnOffType.ON);
                    logger.debug("Niko Home Control: switch intialized {}", actionId);
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case 2:
                    updateState(CHANNEL_BRIGHTNESS, new PercentType(actionState));
                    logger.debug("Niko Home Control: dimmer intialized {}", actionId);
                    updateStatus(ThingStatus.ONLINE);
                    break;
                case 4:
                case 5:
                    updateState(CHANNEL_ROLLERSHUTTER, new PercentType(actionState));
                    logger.debug("Niko Home Control: rollershutter intialized {}", actionId);
                    updateStatus(ThingStatus.ONLINE);
                    break;
                default:
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Niko Home Control: unknown action type " + actionType);
            }
            thing.setLocation(actionLocation);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Niko Home Control: ACTIONID does not match an action in the controller " + actionId);
        }

    }

    public void handleStateUpdate(int actionType, int actionState) {

        logger.debug("Niko Home Control: handle state update {} for {}", actionState,
                this.getConfig().get(CONFIG_ACTION_ID));

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
