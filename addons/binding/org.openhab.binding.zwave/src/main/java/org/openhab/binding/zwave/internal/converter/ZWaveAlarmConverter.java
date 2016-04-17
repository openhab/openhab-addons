/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zwave.internal.converter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.zwave.handler.ZWaveThingChannel;
import org.openhab.binding.zwave.internal.protocol.SerialMessage;
import org.openhab.binding.zwave.internal.protocol.ZWaveController;
import org.openhab.binding.zwave.internal.protocol.ZWaveNode;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.AlarmType;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveAlarmCommandClass.ZWaveAlarmValueEvent;
import org.openhab.binding.zwave.internal.protocol.commandclass.ZWaveCommandClass;
import org.openhab.binding.zwave.internal.protocol.event.ZWaveCommandClassValueEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZWaveAlarmConverter class. Converter for communication with the {@link ZWaveAlarmCommandClass}. Implements polling of
 * the alarm status and receiving of alarm events.
 *
 * @author Chris Jackson
 */
public class ZWaveAlarmConverter extends ZWaveCommandClassConverter {

    private static final Logger logger = LoggerFactory.getLogger(ZWaveAlarmConverter.class);

    /**
     * Constructor. Creates a new instance of the {@link ZWaveAlarmConverter} class.
     *
     * @param controller the {@link ZWaveController} to use for sending messages.
     */
    public ZWaveAlarmConverter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SerialMessage> executeRefresh(ZWaveThingChannel channel, ZWaveNode node) {
        ZWaveAlarmCommandClass commandClass = (ZWaveAlarmCommandClass) node
                .resolveCommandClass(ZWaveCommandClass.CommandClass.ALARM, channel.getEndpoint());
        if (commandClass == null) {
            return null;
        }

        String alarmType = channel.getArguments().get("type");
        logger.debug("NODE {}: Generating poll message for {}, endpoint {}, alarm {}", node.getNodeId(),
                commandClass.getCommandClass().getLabel(), channel.getEndpoint(), alarmType);

        SerialMessage serialMessage;
        if (alarmType != null) {
            serialMessage = node.encapsulate(commandClass.getMessage(AlarmType.valueOf(alarmType)), commandClass,
                    channel.getEndpoint());
        } else {
            serialMessage = node.encapsulate(commandClass.getValueMessage(), commandClass, channel.getEndpoint());
        }

        if (serialMessage == null) {
            return null;
        }

        List<SerialMessage> response = new ArrayList<SerialMessage>();
        response.add(serialMessage);
        return response;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public State handleEvent(ZWaveThingChannel channel, ZWaveCommandClassValueEvent event) {
        ZWaveAlarmValueEvent eventAlarm = (ZWaveAlarmValueEvent) event;
        switch (eventAlarm.getReportType()) {
            case ALARM_REPORT:
                return handleAlarmReport(channel, eventAlarm);
            case NOTIFICATION_REPORT:
                return handleNotifictionReport(channel, eventAlarm);
        }
        return null;
    }

    private State handleAlarmReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm) {
        String alarmType = channel.getArguments().get("type");
        // Don't trigger event if this item is bound to another alarm type
        if (alarmType != null) {
            // alarmType should be a number
            Integer alarmTypeInt = null;
            try {
                alarmTypeInt = Integer.valueOf(alarmType);
                if (alarmTypeInt != eventAlarm.getAlarmType()) {
                    return null;
                }
            } catch (NumberFormatException e) {
                // try to interpret it as a enum value
                if (AlarmType.valueOf(alarmType).getKey() != eventAlarm.getAlarmType()) {
                    return null;
                }
            }
        }

        State state = null;
        switch (channel.getDataType()) {
            case OnOffType:
                state = eventAlarm.getValue() == 0 ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                state = eventAlarm.getValue() == 0 ? OpenClosedType.CLOSED : OpenClosedType.OPEN;
                break;
            default:
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }
        return state;

    }

    private State handleNotifictionReport(ZWaveThingChannel channel, ZWaveAlarmValueEvent eventAlarm) {
        String alarmType = channel.getArguments().get("type");
        String alarmEvent = channel.getArguments().get("event");

        // Don't trigger event if this item is bound to another alarm type
        if (alarmType != null && AlarmType.valueOf(alarmType) != eventAlarm.getZwaveAlarmType()) {
            return null;
        }

        // Check the event type( this will only report the alarm activation. Not deactivation.
        if (alarmEvent != null && Integer.parseInt(alarmEvent) != eventAlarm.getAlarmEvent()) {
            return null;
        }

        // handle event 0xfe as 'clear the event'
        int event = eventAlarm.getAlarmEvent() == 0xfe ? 0 : eventAlarm.getAlarmEvent();

        State state = null;
        switch (channel.getDataType()) {
            case OnOffType:
                state = event == 0 ? OnOffType.OFF : OnOffType.ON;
                break;
            case OpenClosedType:
                if (AlarmType.ACCESS_CONTROL.equals(eventAlarm.getZwaveAlarmType())) {
                    switch (event) {
                        case 22: // Window/Door is open
                            state = OpenClosedType.OPEN;
                            break;
                        case 23: // Window/Door is closed
                            state = OpenClosedType.CLOSED;
                            break;
                        default:
                            logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(),
                                    channel.getDataType());
                    }
                } else {
                    logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                }
                break;
            case DecimalType:
                state = new DecimalType(event);
                break;
            default:
                logger.warn("No conversion in {} to {}", this.getClass().getSimpleName(), channel.getDataType());
                break;
        }
        return state;
    }
}
