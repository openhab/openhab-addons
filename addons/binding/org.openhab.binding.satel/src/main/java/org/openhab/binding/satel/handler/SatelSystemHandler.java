/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.satel.handler;

import static org.openhab.binding.satel.SatelBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.satel.internal.command.ClearTroublesCommand;
import org.openhab.binding.satel.internal.command.IntegraStatusCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.command.SetClockCommand;
import org.openhab.binding.satel.internal.event.IntegraStatusEvent;
import org.openhab.binding.satel.internal.event.NewStatesEvent;
import org.openhab.binding.satel.internal.event.SatelEvent;
import org.openhab.binding.satel.internal.types.StateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelSystemHandler} is responsible for handling commands, which are
 * sent to one of the system channels.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
public class SatelSystemHandler extends SatelThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_SYSTEM);

    private static final Set<String> STATUS_CHANNELS = Stream
            .of(CHANNEL_DATE_TIME, CHANNEL_SERVICE_MODE, CHANNEL_TROUBLES, CHANNEL_TROUBLES_MEMORY,
                    CHANNEL_ACU100_PRESENT, CHANNEL_INTRX_PRESENT, CHANNEL_GRADE23_SET)
            .collect(Collectors.toSet());

    private final Logger logger = LoggerFactory.getLogger(SatelSystemHandler.class);

    public SatelSystemHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (bridgeHandler != null && CHANNEL_USER_CODE.equals(channelUID.getId()) && command instanceof StringType) {
            bridgeHandler.setUserCode(command.toFullString());
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void incomingEvent(SatelEvent event) {
        if (event instanceof IntegraStatusEvent) {
            logger.trace("Handling incoming event: {}", event);
            IntegraStatusEvent statusEvent = (IntegraStatusEvent) event;
            if (thingConfig.isCommandOnly()) {
                return;
            }
            updateState(CHANNEL_DATE_TIME, new DateTimeType(statusEvent.getIntegraTime()));
            updateSwitch(CHANNEL_SERVICE_MODE, statusEvent.inServiceMode());
            updateSwitch(CHANNEL_TROUBLES, statusEvent.troublesPresent());
            updateSwitch(CHANNEL_TROUBLES_MEMORY, statusEvent.troublesMemory());
            updateSwitch(CHANNEL_ACU100_PRESENT, statusEvent.isAcu100Present());
            updateSwitch(CHANNEL_INTRX_PRESENT, statusEvent.isIntRxPresent());
            updateSwitch(CHANNEL_GRADE23_SET, statusEvent.isGrade23Set());
        } else {
            super.incomingEvent(event);
        }
    }

    @Override
    protected StateType getStateType(String channelId) {
        return null;
    }

    @Override
    protected SatelCommand convertCommand(ChannelUID channel, Command command) {
        switch (channel.getId()) {
            case CHANNEL_TROUBLES:
            case CHANNEL_TROUBLES_MEMORY:
                if (command == OnOffType.ON) {
                    return null;
                } else {
                    return new ClearTroublesCommand(bridgeHandler.getUserCode());
                }
            case CHANNEL_DATE_TIME:
                DateTimeType dateTime = null;
                if (command instanceof StringType) {
                    dateTime = DateTimeType.valueOf(command.toString());
                } else if (command instanceof DateTimeType) {
                    dateTime = (DateTimeType) command;
                }
                if (dateTime != null) {
                    return new SetClockCommand(dateTime.getCalendar(), bridgeHandler.getUserCode());
                }
                break;
            default:
                // do nothing for other types of status
                break;
        }

        return null;
    }

    @Override
    protected Collection<SatelCommand> getRefreshCommands(NewStatesEvent event) {
        Collection<SatelCommand> result = new LinkedList<>();
        boolean anyStatusChannelLinked = getThing().getChannels().stream()
                .filter(channel -> STATUS_CHANNELS.contains(channel.getUID().getId()))
                .anyMatch(channel -> isLinked(channel.getUID().getId()));
        boolean needRefresh = anyStatusChannelLinked
                && (requiresRefresh() || isLinked(CHANNEL_DATE_TIME) || event.isNew(IntegraStatusCommand.COMMAND_CODE));
        if (needRefresh) {
            result.add(new IntegraStatusCommand());
        }

        return result;
    }

}
