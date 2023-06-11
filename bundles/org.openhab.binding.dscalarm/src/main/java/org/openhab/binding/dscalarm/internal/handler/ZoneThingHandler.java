/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.dscalarm.internal.handler;

import static org.openhab.binding.dscalarm.internal.DSCAlarmBindingConstants.*;

import java.util.EventObject;

import org.openhab.binding.dscalarm.internal.DSCAlarmCode;
import org.openhab.binding.dscalarm.internal.DSCAlarmEvent;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage;
import org.openhab.binding.dscalarm.internal.DSCAlarmMessage.DSCAlarmMessageInfoType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Zone type Thing.
 *
 * @author Russell Stephens - Initial Contribution
 */
public class ZoneThingHandler extends DSCAlarmBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ZoneThingHandler.class);

    /**
     * Constructor.
     *
     * @param thing
     */
    public ZoneThingHandler(Thing thing) {
        super(thing);
        setDSCAlarmThingType(DSCAlarmThingType.ZONE);
    }

    @Override
    public void updateChannel(ChannelUID channelUID, int state, String description) {
        logger.debug("updateChannel(): Zone Channel UID: {}", channelUID);

        boolean trigger;
        OnOffType onOffType;
        OpenClosedType openClosedType;

        if (channelUID != null) {
            switch (channelUID.getId()) {
                case ZONE_MESSAGE:
                    updateState(channelUID, new StringType(description));
                    break;
                case ZONE_STATUS:
                    openClosedType = (state > 0) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
                    updateState(channelUID, openClosedType);
                    break;
                case ZONE_BYPASS_MODE:
                    onOffType = (state > 0) ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_IN_ALARM:
                    trigger = state != 0;
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_TAMPER:
                    trigger = state != 0;
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_FAULT:
                    trigger = state != 0;
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case ZONE_TRIPPED:
                    trigger = state != 0;
                    onOffType = trigger ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                default:
                    logger.debug("updateChannel(): Zone Channel not updated - {}.", channelUID);
                    break;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        if (command instanceof RefreshType) {
            return;
        }

        if (dscAlarmBridgeHandler != null && dscAlarmBridgeHandler.isConnected()
                && channelUID.getId().equals(ZONE_BYPASS_MODE)) {
            String data = String.valueOf(getPartitionNumber()) + "*1" + String.format("%02d", getZoneNumber()) + "#";

            dscAlarmBridgeHandler.sendCommand(DSCAlarmCode.KeySequence, data);
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

    @Override
    public void dscAlarmEventReceived(EventObject event, Thing thing) {
        if (thing != null) {
            if (getThing().equals(thing)) {
                DSCAlarmEvent dscAlarmEvent = (DSCAlarmEvent) event;
                DSCAlarmMessage dscAlarmMessage = dscAlarmEvent.getDSCAlarmMessage();

                ChannelUID channelUID = null;
                DSCAlarmCode dscAlarmCode = DSCAlarmCode
                        .getDSCAlarmCodeValue(dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.CODE));
                logger.debug("dscAlarmEventRecieved(): Thing - {}   Command - {}", thing.getUID(), dscAlarmCode);

                int state = 0;
                String status = "";

                switch (dscAlarmCode) {
                    case ZoneAlarm: /* 601 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneAlarmRestore: /* 602 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_IN_ALARM);
                        updateChannel(channelUID, state, "");
                        zoneMessage(status);
                        break;
                    case ZoneTamper: /* 603 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneTamperRestore: /* 604 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_TAMPER);
                        updateChannel(channelUID, state, "");
                        zoneMessage(status);
                        break;
                    case ZoneFault: /* 605 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneFaultRestore: /* 606 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_FAULT);
                        updateChannel(channelUID, state, "");
                        zoneMessage(status);
                        break;
                    case ZoneOpen: /* 609 */
                        state = 1;
                        status = dscAlarmMessage.getMessageInfo(DSCAlarmMessageInfoType.DESCRIPTION);
                    case ZoneRestored: /* 610 */
                        channelUID = new ChannelUID(getThing().getUID(), ZONE_TRIPPED);
                        updateChannel(channelUID, state, "");

                        channelUID = new ChannelUID(getThing().getUID(), ZONE_STATUS);
                        updateChannel(channelUID, state, "");
                        zoneMessage(status);
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
