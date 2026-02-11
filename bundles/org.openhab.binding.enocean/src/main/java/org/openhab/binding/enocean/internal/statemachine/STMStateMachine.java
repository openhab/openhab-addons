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
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic state machine for managing multi-stage device movements.
 * <p>
 * This state machine is decoupled from specific device handlers and uses
 * a callback mechanism to notify state changes. The caller is responsible
 * for persisting state and updating channels.
 *
 * @param <A> the action type (enum)
 * @param <S> the state type (enum)
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public class STMStateMachine<A extends Enum<A>, S extends Enum<S>> {

    private final List<STMTransition<A, S>> transitions;
    private final HashMap<A, Runnable> callbackActions;
    private final ScheduledExecutorService scheduler;
    private final @Nullable Consumer<S> stateChangeCallback;

    private S state;
    private S prevState;
    private @Nullable String storedChannel;
    private @Nullable Command storedCommand;

    protected @Nullable ScheduledFuture<?> responseFuture = null;

    protected Logger logger = LoggerFactory.getLogger(STMStateMachine.class);

    /**
     * Creates a new state machine instance.
     *
     * @param config the transition configuration to use
     * @param initState the initial state
     * @param scheduler the scheduler for delayed operations
     * @param stateChangeCallback optional callback invoked on state changes
     * @return new state machine instance
     */
    public static <A extends Enum<A>, S extends Enum<S>> STMStateMachine<A, S> build(
            STMTransitionConfiguration<A, S> config, S initState, ScheduledExecutorService scheduler,
            @Nullable Consumer<S> stateChangeCallback) {
        return new STMStateMachine<>(config, initState, scheduler, stateChangeCallback);
    }

    private STMStateMachine(STMTransitionConfiguration<A, S> config, S initState, ScheduledExecutorService scheduler,
            @Nullable Consumer<S> stateChangeCallback) {
        this.callbackActions = new HashMap<>();
        this.state = initState;
        this.prevState = initState;
        this.transitions = config.getTransitions();
        this.scheduler = scheduler;
        this.stateChangeCallback = stateChangeCallback;
    }

    /**
     * Registers a callback to be executed when a specific action is applied.
     *
     * @param action the action that triggers the callback
     * @param callback the callback to execute
     * @return this state machine for fluent configuration
     */
    public synchronized STMStateMachine<A, S> register(A action, Runnable callback) {
        this.callbackActions.put(action, callback);
        return this;
    }

    /**
     * Gets the current state.
     *
     * @return current state
     */
    public synchronized S getState() {
        return state;
    }

    /**
     * Gets the previous state before the last transition.
     *
     * @return previous state
     */
    public synchronized S getPrevState() {
        return prevState;
    }

    /**
     * Restores a previously persisted state.
     * <p>
     * This method is intended to be called during handler initialization
     * to restore state from Thing properties. No callback is triggered.
     *
     * @param restoredState the state to restore
     */
    public synchronized void restoreState(S restoredState) {
        this.prevState = this.state;
        this.state = restoredState;
        logger.debug("STM: State restored to {}", restoredState);
    }

    /**
     * Applies an action to trigger a state transition.
     * <p>
     * If a matching transition is found, the state changes and registered
     * callbacks are invoked.
     *
     * @param action the action to apply
     * @return this state machine for fluent usage
     */
    @SuppressWarnings("null")
    public synchronized STMStateMachine<A, S> apply(A action) {
        for (STMTransition<A, S> transition : transitions) {
            boolean currentStateMatches = transition.from.equals(state);
            boolean conditionsMatch = transition.action.equals(action);

            if (currentStateMatches && conditionsMatch) {
                logger.debug("STM: State change from {} to {} by action {}, prevState {}", state, transition.to, action,
                        prevState);
                prevState = state;
                state = transition.to;

                // Execute action-specific callback if registered
                if (callbackActions.containsKey(action)) {
                    callbackActions.get(action).run();
                }

                // Notify state change via callback
                if (stateChangeCallback != null) {
                    stateChangeCallback.accept(state);
                }

                break;
            }
        }

        return this;
    }

    /**
     * Schedules a runnable to be executed after a delay.
     * <p>
     * Useful for scheduling follow-up commands after movement completion.
     *
     * @param runnable the task to execute
     * @param delayMs delay in milliseconds
     */
    @SuppressWarnings("null")
    public void scheduleDelayed(Runnable runnable, long delayMs) {
        if (responseFuture == null || responseFuture.isDone()) {
            this.responseFuture = scheduler.schedule(runnable, delayMs, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Stores a command for later processing.
     * <p>
     * This is used when a command cannot be executed immediately (e.g., during calibration)
     * and needs to be processed after a state transition completes.
     *
     * @param channel the channel ID for the command
     * @param command the command to store
     */
    public synchronized void storeCommand(String channel, Command command) {
        this.storedChannel = channel;
        this.storedCommand = command;
        logger.debug("STM: Stored command {} for channel {}", command, channel);
    }

    /**
     * Gets the stored channel ID.
     *
     * @return the stored channel ID, or null if none stored
     */
    public synchronized @Nullable String getStoredChannel() {
        return storedChannel;
    }

    /**
     * Gets the stored command.
     *
     * @return the stored command, or null if none stored
     */
    public synchronized @Nullable Command getStoredCommand() {
        return storedCommand;
    }

    /**
     * Clears the stored command.
     */
    public synchronized void clearStoredCommand() {
        this.storedChannel = null;
        this.storedCommand = null;
    }
}
