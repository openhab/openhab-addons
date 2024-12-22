package org.openhab.binding.enocean.internal.statemachine;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
 *
 * @author Sven Schad - Initial contribution
 * 
 */

public class STMStateMachine {

    private List<STMTransition> transitions;
    private HashMap<STMAction, Runnable> callbackActions;
    private STMState state;
    private STMState prevState;
    private Thing thing;
    private String channel;
    private Command command;

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

        if (command != null) {
            logger.debug("STM: ProcessCommand {}", command);
            Channel cmdChannel = thing.getChannel(channel);
            if (cmdChannel != null) {
                ChannelUID channelUID = cmdChannel.getUID();
                ThingHandler handler = thing.getHandler();
                if (handler != null) {
                    handler.handleCommand(channelUID, command);
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
