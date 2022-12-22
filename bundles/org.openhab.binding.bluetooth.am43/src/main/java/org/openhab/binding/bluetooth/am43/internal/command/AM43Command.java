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
package org.openhab.binding.bluetooth.am43.internal.command;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link AM43Command} provides basic functionality that all commands for the AM43 share.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public abstract class AM43Command {

    private static final byte[] REQUEST_PREFIX = { 0, (byte) 0xFF, 0, 0 };
    private static final byte HEADER_PREFIX = (byte) 0x9a;

    private final Lock stateLock = new ReentrantLock();

    private final Condition stateCondition = stateLock.newCondition();

    private volatile State state;

    private byte header;

    private byte[] data;

    private byte @Nullable [] response;

    public AM43Command(byte commandHeader, byte... data) {
        this.header = commandHeader;
        this.data = data;
        this.state = State.NEW;
    }

    public enum State {
        NEW,
        ENQUEUED,
        SENT,
        SUCCEEDED,
        FAILED
    }

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

    public byte getHeader() {
        return header;
    }

    public static byte getRequestHeader(byte[] request) {
        return request[REQUEST_PREFIX.length + 1];
    }

    public static byte getResponseHeader(byte[] response) {
        return response[1];
    }

    public byte[] getRequest() {
        byte[] value = new byte[4 + data.length + REQUEST_PREFIX.length];
        System.arraycopy(REQUEST_PREFIX, 0, value, 0, REQUEST_PREFIX.length);
        value[REQUEST_PREFIX.length] = HEADER_PREFIX;
        value[REQUEST_PREFIX.length + 1] = header;
        value[REQUEST_PREFIX.length + 2] = (byte) data.length;
        System.arraycopy(data, 0, value, REQUEST_PREFIX.length + 3, data.length);
        value[value.length - 1] = createChecksum(value, REQUEST_PREFIX.length, 3 + data.length);
        return value;
    }

    /**
     * A basic method to calculate the checksum
     * 
     * @param data source for the checksum calculation
     * @param startIndex the zero-based start index to include in the calculation
     * @param length the length of the range to include in the calculation
     * @return the CRC-checksum result in {@link byte}
     */
    protected byte createChecksum(byte[] data, int startIndex, int length) {
        byte crc = data[startIndex];
        for (int i = startIndex + 1; i < startIndex + length; i++) {
            crc ^= data[i];
        }
        return crc;
    }

    public boolean handleResponse(Executor executor, ResponseListener listener, byte @Nullable [] response) {
        if (response == null || response.length < minResponseSize()) {
            return false;
        }
        if (getResponseHeader(response) != header) {
            return false;
        }
        this.response = response;
        setState(State.SUCCEEDED);
        return true;
    }

    public int minResponseSize() {
        return 2;
    }

    public byte[] getResponse() {
        byte[] ret = response;
        if (ret == null) {
            throw new IllegalStateException("Response has yet to be received");
        }
        return ret;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
