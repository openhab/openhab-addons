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
package org.openhab.binding.lcn.internal.connection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Base class for all states used with {@link AbstractStateMachine}.
 *
 * @param <T> type of the state machine implementation
 * @param <U> type of the state implementation
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractState<T extends AbstractStateMachine<T, U>, U extends AbstractState<T, U>> {
    private final List<ScheduledFuture<?>> usedTimers = Collections.synchronizedList(new ArrayList<>());
    protected final T context;

    public AbstractState(T context) {
        this.context = context;
    }

    /**
     * Invoked when the State shall start its operation.
     */
    protected abstract void startWorking();

    /**
     * Stops all timers, the State has been started.
     */
    protected void cancelAllTimers() {
        synchronized (usedTimers) {
            usedTimers.forEach(t -> t.cancel(true));
        }
    }

    /**
     * When a state starts a timer, its ScheduledFuture must be registered by this method. All timers added by this
     * method, are canceled when the StateMachine leaves this State.
     *
     * @param timer the new timer
     */
    protected void addTimer(ScheduledFuture<?> timer) {
        usedTimers.add(timer);
    }

    /**
     * Sets a new State. The current state is torn down gracefully.
     *
     * @param newStateFactory the lambda returning the new State
     */
    protected void nextState(Function<T, U> newStateFactory) {
        synchronized (context) {
            if (context.isStateActive(this)) {
                context.setState(newStateFactory);
            }
        }
    }
}
