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
package org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;

/**
 * Abstract class for all BLE commands sent to the controller
 *
 * @author Benjamin Lafois - Initial contribution
 *
 */
@NonNullByDefault
public abstract class BRC1HCommand {

    public enum State {
        NEW,
        ENQUEUED,
        SENT,
        SUCCEEDED,
        FAILED
    }

    private volatile State state = State.NEW;

    private final Lock stateLock = new ReentrantLock();

    private final Condition stateCondition = stateLock.newCondition();

    public abstract void handleResponse(Executor executor, ResponseListener listener, MadokaMessage mm)
            throws MadokaParsingException;

    /**
     * THis command returns the message to be sent
     *
     * @return
     */
    public abstract byte[][] getRequest();

    /**
     * This is the command number, in the protocol
     *
     * @return
     */
    public abstract int getCommandId();

    /**
     * Returns current state of the command.
     *
     * @return current state
     */
    public State getState() {
        return state;
    }

    /**
     * Sets state of the command.
     *
     * @param state new state
     */
    public void setState(State state) {
        stateLock.lock();
        try {
            this.state = state;
            stateCondition.signalAll();
        } finally {
            stateLock.unlock();
        }
    }

    public boolean awaitStateChange(long timeout, TimeUnit unit, State... expectedStates) throws InterruptedException {
        stateLock.lock();
        try {
            long nanosTimeout = unit.toNanos(timeout);
            while (!isInAnyState(expectedStates)) {
                if (nanosTimeout <= 0L) {
                    return false;
                }
                nanosTimeout = stateCondition.awaitNanos(nanosTimeout);
            }
        } finally {
            stateLock.unlock();
        }
        return true;
    }

    private boolean isInAnyState(State[] acceptedStates) {
        for (State acceptedState : acceptedStates) {
            if (acceptedState == state) {
                return true;
            }
        }
        return false;
    }
}
