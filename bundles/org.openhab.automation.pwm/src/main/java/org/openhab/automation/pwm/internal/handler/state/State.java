/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.automation.pwm.internal.handler.state;

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The base class of all states.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public abstract class State {
    private final Logger logger = LoggerFactory.getLogger(State.class);
    protected StateMachine context;
    protected ScheduledExecutorService scheduler;

    public State(StateMachine context) {
        this.context = context;
        this.scheduler = context.getScheduler();
    }

    /**
     * Invoked when the duty cycle updated and changed.
     */
    public abstract void dutyCycleChanged();

    /**
     * Invoked when the duty cycle updated.
     */
    protected abstract void dutyCycleUpdated();

    public abstract void dispose();

    /**
     * Sets a new state in the state machine.
     */
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public synchronized void nextState(Function<StateMachine, ? extends State> nextState) {
        if (context.getState() != this) { // compare identity
            return;
        }

        context.getState().dispose();
        State newState = nextState.apply(context);

        logger.trace("{}: {} -> {}", context.getRuleUID(), context.getState().getClass().getSimpleName(),
                newState.getClass().getSimpleName());

        context.setState(newState);
    }

    /**
     * Calculates the ON duration by the duty cycle.
     *
     * @param dutyCycleInPercent the duty cycle in percent
     * @return the ON duration in ms
     */
    protected long calculateOnTimeMs(double dutyCycleInPercent) {
        return (long) (context.getPeriodMs() / 100 * dutyCycleInPercent);
    }

    /**
     * Switches the output on or off.
     *
     * @param on true, if the output shall be switched on.
     */
    protected void controlOutput(boolean on) {
        context.controlOutput(on);
    }
}
