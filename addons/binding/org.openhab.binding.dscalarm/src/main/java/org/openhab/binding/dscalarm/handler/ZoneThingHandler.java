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

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
 * This is a class for handling a Zone type Thing.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class ZoneThingHandler extends DSCAlarmBaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(ZoneThingHandler.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public ZoneThingHandler(Thing thing) {
        super(thing);
        setDSCAlarmThingType(DSCAlarmThingType.ZONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateChannel(ChannelUID channelUID) {
        logger.debug("updateChannel(): Zone Channel UID: {}", channelUID);

        int state;
        String strStatus = "";
        boolean trigger;
        OnOffType onOffType;
        OpenClosedType openClosedType;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case ZONE_MESSAGE:
                    strStatus = properties.getZoneMessage();
                    updateState(channelUID, new StringType(strStatus));
                    break;
                case ZONE_STATUS:
                    state = properties.getState(StateType.GENERAL_STATE);
                    openClosedType = (state > 0) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                    updateState(channelUID, openClosedType);
                    break;
                case ZONE_BYPASS_MODE:
                    state = properties.getState(StateType.ARM_STATE);
                    onOffType = (state > 0) ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_IN_ALARM:
                    trigger = properties.getTrigger(TriggerType.ALARMED);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_TAMPER:
                    trigger = properties.getTrigger(TriggerType.TAMPERED);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_FAULT:
                    trigger = properties.getTrigger(TriggerType.FAULTED);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_TRIPPED:
                    trigger = properties.getTrigger(TriggerType.TRIPPED);
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                default:
                    logger.debug("updateChannel(): Zone Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateProperties(ChannelUID channelUID, int state, String description) {
        logger.debug("updateProperties(): Panel Channel UID: {}", channelUID);

        boolean trigger = state != 0 ? true : false;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case ZONE_MESSAGE:
                    properties.setZoneMessage(description);
                    break;
                case ZONE_STATUS:
                    properties.setState(StateType.GENERAL_STATE, state, description);
                    break;
                case ZONE_BYPASS_MODE:
                    properties.setState(StateType.ARM_STATE, state, description);
                    break;
                case ZONE_IN_ALARM:
                    properties.setState(StateType.ALARM_STATE, state, description);
                    properties.setTrigger(TriggerType.ALARMED, trigger);
                    break;
                case ZONE_TAMPER:
                    properties.setState(StateType.TAMPER_STATE, state, description);
                    properties.setTrigger(TriggerType.TAMPERED, trigger);
                    break;
                case ZONE_FAULT:
                    properties.setState(StateType.FAULT_STATE, state, description);
                    properties.setTrigger(TriggerType.FAULTED, trigger);
                    break;
                case ZONE_TRIPPED:
                    properties.setTrigger(TriggerType.TRIPPED, trigger);
                    break;
                default:
                    logger.debug("updateProperties(): Zone property not updated.");
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
                case ZONE_BYPASS_MODE:
                    if (command == OnOffType.OFF) {
                        String data = String.valueOf(getPartitionNumber()) + "*1" + String.format("%02d", getZoneNumber()) + "#";
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.KeySequence, data);
                    } else if (command == OnOffType.ON) {
                        String data = String.valueOf(getPartitionNumber()) + "*1" + String.format("%02d", getZoneNumber()) + "#";
                        dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.KeySequence, data);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Method to set Zone Message.
     *
     * @param message
     */
    private void zoneMessage(String message) {
        updateState(new ChannelUID(getThing().getUID(), ZONE_MESSAGE), new StringType(message));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dscAlarmEventReceived(EventObject event, Thing thing) {

        if (thing != null) {
            if (getThing().equals(thing)) {
                DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
                DSCAlarmMessage dscAlarmMessage = dscAlarmEvent.getDSCAlarmMessage();

                ChannelUID channelUID = null;
                DSCAlarmCode apiCode = DSCAlarmCode.getDSCAlarmCodeValue(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
                logger.debug("dscAlarmEventRecieved(): Thing - {}   Command - {}", thing.getUID(), apiCode);

                int state = 0;
                String status = "";

                switch (apiCode) {
                    case ZoneAlarm: /* 601 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneAlarmRestore: /* 602 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_IN_ALARM);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        zoneMessage(status);
                        break;
                    case ZoneTamper: /* 603 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneTamperRestore: /* 604 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_TAMPER);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        zoneMessage(status);
                        break;
                    case ZoneFault: /* 605 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneFaultRestore: /* 606 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_FAULT);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        zoneMessage(status);
                        break;
                    case ZoneOpen: /* 609 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneRestored: /* 610 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_TRIPPED);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);

                        channelUID = new ChannelUID(getThing().getUID(), ZONE_STATUS);
                        updateProperties(channelUID, state, "");
                        updateChannel(channelUID);
                        zoneMessage(status);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
