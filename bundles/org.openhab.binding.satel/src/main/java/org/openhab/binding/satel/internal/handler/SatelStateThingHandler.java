/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.satel.internal.command.IntegraStateCommand;
import org.openhab.binding.satel.internal.command.SatelCommand;
import org.openhab.binding.satel.internal.event.ConnectionStatusEvent;
import org.openhab.binding.satel.internal.event.IntegraStateEvent;
import org.openhab.binding.satel.internal.event.NewStatesEvent;
import org.openhab.binding.satel.internal.types.StateType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SatelStateThingHandler} is base thing handler class for all state holding things.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public abstract class SatelStateThingHandler extends SatelThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SatelStateThingHandler.class);

    private final AtomicBoolean requiresRefresh = new AtomicBoolean(true);

    public SatelStateThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("New command for {}: {}", channelUID, command);

        if (command == RefreshType.REFRESH) {
            this.requiresRefresh.set(true);
        } else {
            withBridgeHandlerPresent(bridgeHandler -> {
                if (bridgeHandler.getUserCode().isEmpty()) {
                    logger.info("Cannot control devices without providing valid user code. Command has not been sent.");
                } else {
                    convertCommand(channelUID, command)
                            .ifPresent(satelCommand -> bridgeHandler.sendCommand(satelCommand, true));
                }
            });
        }
    }

    @Override
    public void incomingEvent(ConnectionStatusEvent event) {
        logger.trace("Handling incoming event: {}", event);
        // we have just connected, change thing's status and force refreshing
        if (event.isConnected()) {
            updateStatus(ThingStatus.ONLINE);
            requiresRefresh.set(true);
        }
    }

    @Override
    public void incomingEvent(NewStatesEvent event) {
        logger.trace("Handling incoming event: {}", event);
        // refresh all states that have changed
        withBridgeHandlerPresent(bridgeHandler -> {
            for (SatelCommand command : getRefreshCommands(event)) {
                bridgeHandler.sendCommand(command, true);
            }
        });
    }

    @Override
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public void incomingEvent(IntegraStateEvent event) {
        logger.trace("Handling incoming event: {}", event);
        // update thing's state unless it should accept commands only
        if (getThingConfig().isCommandOnly()) {
            return;
        }
        for (Channel channel : getThing().getChannels()) {
            ChannelUID channelUID = channel.getUID();
            if (isLinked(channel.getUID())) {
                StateType stateType = getStateType(channelUID.getId());
                if (stateType != StateType.NONE && event.hasDataForState(stateType)) {
                    int bitNbr = getStateBitNbr(stateType);
                    boolean invertState = getThingConfig().isStateInverted();
                    updateSwitch(channelUID, event.isSet(stateType, bitNbr) ^ invertState);
                }
            }
        }
    }

    /**
     * Returns bit number of given state type for this thing.
     * This number addresses the bit in bit set sent to or received from alarm system.
     * Usually this number is the device identifier.
     *
     * @param stateType state type
     * @return bit number in state bit set
     */
    protected int getStateBitNbr(StateType stateType) {
        return getThingConfig().getId() - 1;
    }

    /**
     * Converts openHAB command sent to a channel into Satel message.
     *
     * @param channel channel the command was sent to
     * @param command sent command
     * @return Satel message that reflects sent command
     */
    protected abstract Optional<SatelCommand> convertCommand(ChannelUID channel, Command command);

    /**
     * Derived handlers must return appropriate state type for channels they support.
     * If given channel is not supported by a handler, it should return {@linkplain StateType#NONE}.
     *
     * @param channelId channel identifier to get state type for
     * @return object that represents state type
     * @see #getChannel(StateType)
     */
    protected abstract StateType getStateType(String channelId);

    /**
     * Returns channel for given state type. Usually channels have the same ID as state type name they represent.
     *
     * @param stateType state type to get channel for
     * @return channel object
     * @see #getStateType(String)
     */
    protected @Nullable Channel getChannel(StateType stateType) {
        final String channelId = stateType.toString().toLowerCase();
        final Channel channel = getThing().getChannel(channelId);
        if (channel == null) {
            logger.debug("Missing channel for {}", stateType);
        }
        return channel;
    }

    /**
     * Returns list of commands required to update thing state basing on event describing changes since last refresh.
     *
     * @param event list of state changes since last refresh
     * @return collection of {@link IntegraStateCommand}
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected Collection<SatelCommand> getRefreshCommands(NewStatesEvent event) {
        final boolean hasExtPayload = getBridgeHandler().getIntegraType().hasExtPayload();
        final Collection<SatelCommand> result = new LinkedList<>();
        final boolean forceRefresh = requiresRefresh();
        for (Channel channel : getThing().getChannels()) {
            StateType stateType = getStateType(channel.getUID().getId());
            if (stateType != StateType.NONE && isLinked(channel.getUID())) {
                if (forceRefresh || event.isNew(stateType.getRefreshCommand())) {
                    result.add(new IntegraStateCommand(stateType, hasExtPayload));
                }
            }
        }
        return result;
    }

    /**
     * Checks if this thing requires unconditional refresh of all its channels.
     * Clears the flag afterwards.
     *
     * @return if <code>true</code> this thing requires full refresh
     */
    protected boolean requiresRefresh() {
        return requiresRefresh.getAndSet(false);
    }
}
