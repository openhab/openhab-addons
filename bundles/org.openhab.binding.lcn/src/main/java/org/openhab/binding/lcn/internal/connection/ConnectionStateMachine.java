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

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.lcn.internal.common.LcnAddr;

/**
 * Implements a state machine for managing the connection to the LCN-PCK gateway. Setting states is thread-safe.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class ConnectionStateMachine extends AbstractStateMachine<ConnectionStateMachine, AbstractConnectionState> {
    private final Connection connection;
    final ScheduledExecutorService scheduler;

    public ConnectionStateMachine(Connection connection, ScheduledExecutorService scheduler) {
        this.connection = connection;
        this.scheduler = scheduler;

        setState(ConnectionStateInit::new);
    }

    /**
     * Gets the framework's scheduler.
     *
     * @return the scheduler
     */
    protected ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * Gets the PCHK Connection object.
     *
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Enqueues a PCK command. Implementation is state dependent.
     *
     * @param addr the destination address
     * @param wantsAck true, if the module shall respond with an Ack
     * @param data the data
     */
    public void queue(LcnAddr addr, boolean wantsAck, byte[] data) {
        AbstractConnectionState localState = state;
        if (localState != null) {
            localState.queue(addr, wantsAck, data);
        }
    }

    /**
     * Invoked by any state, if the connection fails.
     *
     * @param e the cause
     */
    public synchronized void handleConnectionFailed(@Nullable Throwable e) {
        AbstractConnectionState localState = state;
        if (localState != null) {
            localState.handleConnectionFailed(e);
        }
    }

    /**
     * Processes a received PCK message by passing it to the current State.
     *
     * @param data the PCK message
     */
    public void onInputReceived(String data) {
        AbstractConnectionState localState = state;
        if (localState != null) {
            localState.onPckMessageReceived(data);
        }
    }

    /**
     * Shuts the StateMachine down finally. A shut-down StateMachine cannot be re-used.
     */
    public void shutdownFinally() {
        AbstractConnectionState localState = state;
        if (localState != null) {
            localState.shutdownFinally();
        }
    }
}
