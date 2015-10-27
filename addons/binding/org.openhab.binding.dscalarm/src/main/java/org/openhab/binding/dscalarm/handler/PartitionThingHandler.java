/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dscalarm.handler;

import static org.openhab.binding.dscalarm.DSCAlarmBindingConstants.*;

import java.util.EventObject;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.binding.dscalarm.internal.DSCAlarmProperties.StateType;
import org.openhab.binding.dscalarm.internal.DSCAlarmProperties.TriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Partition type Thing.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class PartitionThingHandler extends DSCAlarmBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(PartitionThingHandler.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public PartitionThingHandler(Thing thing) {
        super(thing);
        setDSCAlarmThingType(DSCAlarmThingType.PARTITION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChannel(ChannelUID channelUID) {
        logger.debug("updateChannel(): Panel Channel UID: {}", channelUID);

        int state;
        String strStatus = "";
        boolean trigger;
        OnOffType onOffType;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case PARTITION_STATUS:
                    strStatus = properties.getStateDescription(StateType.GENERAL_STATE);
                    updateState(channelUID, new StringType(strStatus));
                    break;
                case PARTITION_ARM_MODE:
                    state = properties.getState(StateType.ARM_STATE);
                    updateState(channelUID, new DecimalType(state));
                    break;
                case PARTITION_ARMED:
                    trigger = properties.getTrigger(TriggerType.ARMED);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PARTITION_ENTRY_DELAY:
                    trigger = properties.getTrigger(TriggerType.ENTRY_DELAY);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PARTITION_EXIT_DELAY:
                    trigger = properties.getTrigger(TriggerType.EXIT_DELAY);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PARTITION_IN_ALARM:
                    trigger = properties.getTrigger(TriggerType.ALARMED);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case PARTITION_OPENING_CLOSING_MODE:
                    state = properties.getState(StateType.OPENING_CLOSING_STATE);
                    strStatus = properties.getStateDescription(StateType.OPENING_CLOSING_STATE);
                    updateState(channelUID, new StringType(strStatus));
                    break;
                default:
                    logger.debug("updateChannel(): Partition Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties(ChannelUID channelUID, int state, String description) {
        logger.debug("updateProperties(): Partition Channel UID: {}", channelUID);

        boolean trigger = state != 0 ? true : false;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case PARTITION_STATUS:
                    properties.setState(StateType.GENERAL_STATE, state, description);
                    break;
                case PARTITION_ARM_MODE:
                    properties.setState(StateType.ARM_STATE, state, description);
                    break;
                case PARTITION_ARMED:
                    properties.setTrigger(TriggerType.ARMED, trigger);
                    break;
                case PARTITION_ENTRY_DELAY:
                    properties.setTrigger(TriggerType.ENTRY_DELAY, trigger);
                    break;
                case PARTITION_EXIT_DELAY:
                    properties.setTrigger(TriggerType.EXIT_DELAY, trigger);
                    break;
                case PARTITION_IN_ALARM:
                    properties.setTrigger(TriggerType.ALARMED, trigger);
                    break;
                case PARTITION_OPENING_CLOSING_MODE:
                    properties.setState(StateType.OPENING_CLOSING_STATE, state, description);
                    break;
                default:
                    logger.debug("updateProperties(): Partition property not updated.");
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (dscAlarmBridgeHandler == null) {
            logger.warn("DSC Alarm bridge handler not available. Cannot handle command without bridge.");
            return;
        }

        if (dscAlarmBridgeHandler.isConnected()) {
            switch (channelUID.getId()) {
                case PARTITION_ARM_MODE:
                    int partitionNumber = getPartitionNumber();
                    if (command.toString().equals("0")) {
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.PartitionDisarmControl, String.valueOf(partitionNumber));
                        updateProperties(channelUID, 0, "Partition Disarmed");
                    } else if (command.toString().equals("1")) {
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.PartitionArmControlAway, String.valueOf(partitionNumber));
                        updateProperties(channelUID, 1, "Partition Armed (Away)");
                    } else if (command.toString().equals("2")) {
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.PartitionArmControlStay, String.valueOf(partitionNumber));
                        updateProperties(channelUID, 2, "Partition Armed (Stay)");
                    } else if (command.toString().equals("3")) {
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.PartitionArmControlZeroEntryDelay, String.valueOf(partitionNumber));
                        updateProperties(channelUID, 3, "Partition Armed (Away - No Entry Delay)");
                    } else if (command.toString().equals("4")) {
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.PartitionArmControlZeroEntryDelay, String.valueOf(partitionNumber));
                        updateProperties(channelUID, 4, "Partition Armed (Stay - No Entry Delay)");
                    } else if (command.toString().equals("5")) {
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.PartitionArmControlWithUserCode, String.valueOf(partitionNumber));
                        updateProperties(channelUID, 5, "Partition Armed (With User Code)");
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Method to set Partition Status.
     *
     * @param message
     */
    private void partitionStatus(String message) {
        updateState(new ChannelUID(getThing().getUID(), PARTITION_STATUS), new StringType(message));
    }

    /**
     * Method to set Partition Close Open Mode.
     *
     * @param event
     */
    private void partitionOpenCloseModeEventHandler(EventObject event) {
        DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
        DSCAlarmMessage apiMessage = dscAlarmEvent.getDSCAlarmMessage();
        DSCAlarmCode apiCode = DSCAlarmCode.getDSCAlarmCodeValue(apiMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
        ChannelUID channelUID = null;
        int state = 0; /* 0=None, 1=User Closing, 2=Special Closing, 3=Partial Closing, 4=User Opening, 5=Special Opening */

        String strStatus = apiMessage.getMessageInfo(DSCAlarmMessageInfoType.NAME);

        switch (apiCode) {
            case UserClosing: /* 700 */
                state = 1;
                break;
            case SpecialClosing: /* 701 */
                state = 2;
                break;
            case PartialClosing: /* 702 */
                state = 3;
                break;
            case UserOpening: /* 750 */
                state = 4;
                break;
            case SpecialOpening: /* 751 */
                state = 5;
                break;
            default:
                break;
        }

        channelUID = new ChannelUID(getThing().getUID(), PARTITION_OPENING_CLOSING_MODE);
        updateProperties(channelUID, state, strStatus);
        updateChannel(channelUID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dscAlarmEventReceived(EventObject event, Thing thing) {

        if (thing != null) {
            if (getThing() == thing) {
                DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
                DSCAlarmMessage apiMessage = dscAlarmEvent.getDSCAlarmMessage();

                ChannelUID channelUID = null;
                DSCAlarmCode apiCode = DSCAlarmCode.getDSCAlarmCodeValue(apiMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
                String apiName = apiMessage.getMessageInfo(DSCAlarmMessageInfoType.NAME);
                String apiMode = apiMessage.getMessageInfo(DSCAlarmMessageInfoType.MODE);

                logger.debug("dscAlarmEventRecieved(): Thing - {}   Command - {}", thing.getUID(), apiCode);

                switch (apiCode) {
                    case PartitionReady: /* 650 */
                    case PartitionNotReady: /* 651 */
                    case PartitionReadyForceArming: /* 653 */
                    case FailureToArm: /* 672 */
                    case SystemArmingInProgress: /* 674 */
                        partitionStatus(apiName);
                        break;
                    case PartitionArmed: /* 652 */
                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_ARMED);
                        updateProperties(channelUID, 1, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_ENTRY_DELAY);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_EXIT_DELAY);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        /* arm mode:0=disarmed, 1=away armed, 2=stay armed, 3=away no delay, 4=stay no delay, 5=with user code */
                        int armMode = Integer.parseInt(apiMode) + 1;
                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_ARM_MODE);
                        updateProperties(channelUID, armMode, "");
                        updateChannel(channelUID);

                        partitionStatus(apiName);
                        break;
                    case PartitionDisarmed: /* 655 */
                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_ARMED);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_ENTRY_DELAY);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_EXIT_DELAY);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_IN_ALARM);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_ARM_MODE);
                        updateProperties(channelUID, 0, "");
                        updateChannel(channelUID);

                        partitionStatus(apiName);
                        break;
                    case PartitionInAlarm: /* 654 */
                        channelUID = new ChannelUID(getThing().getUID(), PARTITION_IN_ALARM);
                        updateProperties(channelUID, 1, "");
                        updateChannel(channelUID);

                        partitionStatus(apiName);
                        break;
                    case UserClosing: /* 700 */
                    case SpecialClosing: /* 701 */
                    case PartialClosing: /* 702 */
                    case UserOpening: /* 750 */
                    case SpecialOpening: /* 751 */
                        partitionOpenCloseModeEventHandler(event);
                    default:
                        break;
                }
            }
        }
    }
}
