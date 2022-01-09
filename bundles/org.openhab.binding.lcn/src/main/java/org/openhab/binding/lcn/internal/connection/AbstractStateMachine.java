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
package org.openhab.binding.lcn.internal.connection;

import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for state machines.
 *
 * @param <T> type of the state machine implementation
 * @param <U> type of the state implementation
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractStateMachine<T extends AbstractStateMachine<T, U>, U extends AbstractState<T, U>> {
    private final Logger logger = LoggerFactory.getLogger(AbstractStateMachine.class);
    /** The StateMachine's current state */
    protected @Nullable volatile U state;

    /**
     * Sets the current state.
     *
     * @param newStateFactory the new state's factory
     */
    protected synchronized void setState(Function<T, U> newStateFactory) {
        @Nullable
        U localState = state;
        if (localState != null) {
            localState.cancelAllTimers();
        }

        @SuppressWarnings("unchecked")
        U newState = newStateFactory.apply((T) this);

        if (localState != null) {
            logger.debug("Changing state {} -> {}", localState.getClass().getSimpleName(),
                    newState.getClass().getSimpleName());
        }

        state = newState;

        newState.startWorking();
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected boolean isStateActive(AbstractState<?, ?> otherState) {
        return state == otherState; // compare by identity
    }
}
