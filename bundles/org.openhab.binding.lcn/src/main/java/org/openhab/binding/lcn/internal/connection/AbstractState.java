/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for usage for states with {@link StateMachine}.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractState {
    private final List<ScheduledFuture<?>> usedTimers = Collections.synchronizedList(new ArrayList<>());
    protected final StateContext context;

    public AbstractState(StateContext context) {
        this.context = context;
    }

    /**
     * Must be invoked when the State shall start its actions.
     */
    abstract void startWorking();

    /**
     * Stops all timers, the State has been started.
     */
    void cancelAllTimers() {
        synchronized (usedTimers) {
            usedTimers.forEach(t -> t.cancel(true));
        }
    }

    /**
     * When a state starts a timer, its ScheduledFuture must be added by this method. All timers added by this method,
     * are canceled when the StateMachine leaves this State.
     *
     * @param timer the new timer
     */
    void addTimer(ScheduledFuture<?> timer) {
        usedTimers.add(timer);
    }

    /**
     * Sets a new State. The current state is torn down gracefully.
     *
     * @param newStateClass the class of the new State
     */
    synchronized void nextState(Class<? extends AbstractConnectionState> newStateClass) {
        if (context.isStateActive(this)) {
            context.setState(newStateClass);
        }
    }
}
