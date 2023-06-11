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
package org.openhab.binding.satel.internal.handler;

import static org.openhab.binding.satel.internal.SatelBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.command.ClearTroublesCommand;
import org.openhab.binding.satel.internal.command.IntegraStatusCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.command.SetClockCommand;
import org.openhab.binding.satel.internal.event.IntegraStatusEvent;
import org.openhab.binding.satel.internal.event.NewStatesEvent;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelSystemHandler} is responsible for handling commands, which are
 * sent to one of the system channels.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class SatelSystemHandler extends SatelStateThingHandler {

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
        if (CHANNEL_USER_CODE.equals(channelUID.getId()) && command instanceof StringType) {
            withBridgeHandlerPresent(bridgeHandler -> {
                bridgeHandler.setUserCode(command.toFullString());
            });
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void incomingEvent(IntegraStatusEvent event) {
        logger.trace("Handling incoming event: {}", event);
        if (getThingConfig().isCommandOnly()) {
            return;
        }
        updateState(CHANNEL_DATE_TIME,
                event.getIntegraTime().map(dt -> (State) new DateTimeType(dt.atZone(getBridgeHandler().getZoneId())))
                        .orElse(UnDefType.UNDEF));
        updateSwitch(CHANNEL_SERVICE_MODE, event.inServiceMode());
        updateSwitch(CHANNEL_TROUBLES, event.troublesPresent());
        updateSwitch(CHANNEL_TROUBLES_MEMORY, event.troublesMemory());
        updateSwitch(CHANNEL_ACU100_PRESENT, event.isAcu100Present());
        updateSwitch(CHANNEL_INTRX_PRESENT, event.isIntRxPresent());
        updateSwitch(CHANNEL_GRADE23_SET, event.isGrade23Set());
    }

    @Override
    protected StateType getStateType(String channelId) {
        return StateType.NONE;
    }

    @Override
    protected Optional<SatelCommand> convertCommand(ChannelUID channel, Command command) {
        final SatelBridgeHandler bridgeHandler = getBridgeHandler();
        switch (channel.getId()) {
            case CHANNEL_TROUBLES:
            case CHANNEL_TROUBLES_MEMORY:
                if (command == OnOffType.ON) {
                    return Optional.empty();
                } else {
                    return Optional.of(new ClearTroublesCommand(bridgeHandler.getUserCode()));
                }
            case CHANNEL_DATE_TIME:
                DateTimeType dateTime = null;
                if (command instanceof StringType) {
                    dateTime = DateTimeType.valueOf(command.toString());
                } else if (command instanceof DateTimeType) {
                    dateTime = (DateTimeType) command;
                }
                if (dateTime != null) {
                    return Optional.of(new SetClockCommand(dateTime.getZonedDateTime()
                            .withZoneSameInstant(bridgeHandler.getZoneId()).toLocalDateTime(),
                            bridgeHandler.getUserCode()));
                }
                break;
            default:
                // do nothing for other types of status
                break;
        }

        return Optional.empty();
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
