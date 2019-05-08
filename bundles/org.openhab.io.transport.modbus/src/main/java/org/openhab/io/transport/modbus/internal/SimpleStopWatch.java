/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.transport.modbus.internal;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.modbus.internal.ModbusManagerImpl.PollTaskUnregistered;

import net.wimpi.modbus.ModbusException;

/**
 * Implementation of simple stop watch.
 *
 * @author Sami Salonen - initial contribution
 *
 */
@NonNullByDefault
public class SimpleStopWatch {

    private volatile long totalMillis;
    private volatile long resumed;

    @FunctionalInterface
    public abstract interface SupplierWithPollTaskUnregisteredException<T> {
        public abstract T get() throws ModbusManagerImpl.PollTaskUnregistered;
    }

    @FunctionalInterface
    public abstract interface RunnableWithModbusException {
        public abstract void run() throws ModbusException;
    }

    /**
     * Resume or start the stop watch
     *
     * @throws IllegalStateException if stop watch is running already
     */
    public synchronized void resume() {
        if (isRunning()) {
            throw new IllegalStateException("Cannot suspend a running StopWatch");
        }
        resumed = System.currentTimeMillis();
    }

    /**
     * Suspend the stop watch
     *
     * @throws IllegalStateException if stop watch has not been resumed
     */
    public synchronized void suspend() {
        if (!isRunning()) {
            throw new IllegalStateException("Cannot suspend non-running StopWatch");
        }
        totalMillis += System.currentTimeMillis() - resumed;
        resumed = 0;
    }

    /**
     * Get total running time of this StopWatch in milliseconds
     *
     * @return total running time in milliseconds
     */
    public synchronized long getTotalTimeMillis() {
        return totalMillis;
    }

    /**
     * Tells whether this StopWatch is now running
     *
     * @return boolean telling whether this StopWatch is running
     */
    public synchronized boolean isRunning() {
        return resumed > 0;
    }

    /**
     * Time single action using this StopWatch
     *
     * First StopWatch is resumed, then action is applied. Finally the StopWatch is suspended.
     *
     * @param supplier action to time
     * @return return value from supplier
     * @throws PollTaskUnregistered when original supplier throws the exception
     */
    public <R> R timeSupplierWithPollTaskUnregisteredException(SupplierWithPollTaskUnregisteredException<R> supplier)
            throws PollTaskUnregistered {
        try {
            this.resume();
            return supplier.get();
        } finally {
            this.suspend();
        }
    }

    /**
     * Time single action using this StopWatch
     *
     * First StopWatch is resumed, then action is applied. Finally the StopWatch is suspended.
     *
     * @param supplier action to time
     * @return return value from supplier
     */
    public <R> R timeSupplier(Supplier<R> supplier) {
        try {
            this.resume();
            return supplier.get();
        } finally {
            this.suspend();
        }
    }

    /**
     * Time single action using this StopWatch
     *
     * First StopWatch is resumed, then action is applied. Finally the StopWatch is suspended.
     *
     * @param action action to time
     * @throws ModbusException when original action throws the exception
     */
    public void timeRunnableWithModbusException(RunnableWithModbusException action) throws ModbusException {
        try {
            this.resume();
            action.run();
        } finally {
            this.suspend();
        }
    }

    /**
     * Time single action using this StopWatch
     *
     * First StopWatch is resumed, then action is applied. Finally the StopWatch is suspended.
     *
     * @param supplier action to time
     * @return return value from supplier
     */
    public void timeRunnable(Runnable runnable) {
        try {
            this.resume();
            runnable.run();
        } finally {
            this.suspend();
        }
    }

    /**
     * Time single action using this StopWatch
     *
     * First StopWatch is resumed, then action is applied. Finally the StopWatch is suspended.
     *
     * @param consumer action to time
     * @return return value from supplier
     */
    public <T> void timeConsumer(Consumer<T> consumer, T parameter) {
        try {
            this.resume();
            consumer.accept(parameter);
        } finally {
            this.suspend();
        }
    }

}
