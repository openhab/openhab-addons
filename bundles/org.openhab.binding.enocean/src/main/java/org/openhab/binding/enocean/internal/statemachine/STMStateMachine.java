/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.statemachine;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.EnOceanBindingConstants;
import org.openhab.binding.enocean.internal.handler.EnOceanBaseActuatorHandler;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * State machine for managing multi-stage blind movements (position + slat adjustment).
 * 
 * TODO: Architectural consideration - This state machine is currently shared between
 * A5_3F_7F_EltakoFSB (command sending) and PTM200Message (feedback processing).
 * Future improvement: Move to Handler level for better separation of concerns.
 * 
 * TODO: Persistence - State is currently lost on binding restart, requiring recalibration.
 * Future improvement: Store state in Thing properties for persistence across restarts.
 *
 * @author Sven Schad - Initial contribution
 * 
 */
@NonNullByDefault
public class STMStateMachine {

    private List<STMTransition> transitions;
    private HashMap<STMAction, Runnable> callbackActions;
    private STMState state;
    private STMState prevState;
    private Thing thing;
    private @Nullable String channel;
    private @Nullable Command command;

    private final ScheduledExecutorService scheduler;

    protected @Nullable ScheduledFuture<?> responseFuture = null;

    protected Logger logger = LoggerFactory.getLogger(STMStateMachine.class);

    public static STMStateMachine build(STMTransitionConfiguration config, STMState initState, Thing thing,
            ScheduledExecutorService scheduler) {

        return new STMStateMachine(config, initState, thing, scheduler);
    }

    public STMStateMachine register(STMAction action, Runnable callback) {

        this.callbackActions.put(action, callback);
        return this;
    }

    public void storeCommand(String channel, Command command) {

        this.channel = channel;
        this.command = command;
    }

    public void ProcessCommand() {

        Command cmd = command;
        String ch = channel;
        if (cmd != null && ch != null) {
            logger.debug("STM: ProcessCommand {}", cmd);
            Channel cmdChannel = thing.getChannel(ch);
            if (cmdChannel != null) {
                ChannelUID channelUID = cmdChannel.getUID();
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    handler.handleCommand(channelUID, cmd);
                }
            }
        }
    }

    @SuppressWarnings("null")
    public void EnqueueProcessCommand() {

        // Send response after 100ms
        if (responseFuture == null || responseFuture.isDone()) {
            this.responseFuture = scheduler.schedule(this::ProcessCommand, 100, TimeUnit.MILLISECONDS);
        }
    }

    private STMStateMachine(STMTransitionConfiguration config, STMState initState, Thing thing,
            ScheduledExecutorService scheduler) {
        this.callbackActions = new HashMap<STMAction, Runnable>();
        this.state = initState;
        this.prevState = initState;
        this.transitions = config.getTransitions();
        this.scheduler = scheduler;
        this.thing = thing;
    }

    public STMState getState() {
        return state;
    }

    public STMState getPrevState() {
        return prevState;
    }

    @SuppressWarnings("null")
    public STMStateMachine apply(STMAction action) {
        for (STMTransition transition : transitions) {
            boolean currentStateMatches = transition.from.equals(state);
            boolean conditionsMatch = transition.action.equals(action);

            if (currentStateMatches && conditionsMatch) {
                logger.debug("STM: State change from {} to {} by action {} , prevState {}", state, transition.to,
                        action, prevState);
                prevState = state;
                state = transition.to;
                if (callbackActions.containsKey(action)) {
                    callbackActions.get(action).run();
                }
                StringType stringCommand = new StringType(state.name());
                if (thing.getHandler() instanceof EnOceanBaseActuatorHandler myHandler) {
                    Channel channel = thing.getChannel(EnOceanBindingConstants.CHANNEL_STATEMACHINESTATE);
                    if (channel != null) {
                        ChannelUID channelUID = channel.getUID(); // get channelUID
                        myHandler.setState(channelUID, stringCommand); // Update Channel State
                    }
                }

                break;
            }
        }

        return this;
    }
}
