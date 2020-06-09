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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements a state machine for managing the connection to the LCN-PCK gateway. Setting states is thread-safe.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class StateMachine implements StateContext {
    private final Logger logger = LoggerFactory.getLogger(StateMachine.class);
    /** The StateMachine's current state */
    protected volatile AbstractConnectionState state;
    private final Connection connection;
    private final ScheduledExecutorService scheduler;

    public StateMachine(Connection connection, ScheduledExecutorService scheduler) {
        this.connection = connection;
        this.scheduler = scheduler;
        this.state = new ConnectionStateInit(this, scheduler);
    }

    @Override
    public synchronized void setState(Class<? extends AbstractConnectionState> newStateClass) {
        logger.debug("Changing state {} -> {}", state.getClass().getSimpleName(), newStateClass.getSimpleName());

        state.cancelAllTimers();

        try {
            state = newStateClass.getDeclaredConstructor(StateContext.class, ScheduledExecutorService.class)
                    .newInstance(this, scheduler);
            state.startWorking();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            logger.warn("Could not change state: {}", e.getMessage());
        }
    }

    /**
     * Starts the operation of the StateMachine by starting the initial State.
     */
    public void startWorking() {
        state.startWorking();
    }

    @Override
    public void queue(LcnAddr addr, boolean wantsAck, byte[] data) {
        state.queue(addr, wantsAck, data);
    }

    @Override
    public boolean isStateActive(AbstractState otherState) {
        return state == otherState; // compare by identity
    }

    @Override
    public void handleConnectionFailed(@Nullable Throwable e) {
        if (!(state instanceof ConnectionStateShutdown)) {
            if (e != null) {
                connection.getCallback().onOffline(e.getMessage());
            } else {
                connection.getCallback().onOffline("");
            }
            setState(ConnectionStateGracePeriodBeforeReconnect.class);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * Processes a received PCK message by passing it to the current State.
     *
     * @param data the PCK message
     */
    public void onInputReceived(String data) {
        state.onPckMessageReceived(data);
    }

    /**
     * Shuts the StateMachine down finally. A shut-down StateMachine cannot be re-used.
     */
    public void shutdownFinally() {
        state.shutdownFinally();
    }
}
