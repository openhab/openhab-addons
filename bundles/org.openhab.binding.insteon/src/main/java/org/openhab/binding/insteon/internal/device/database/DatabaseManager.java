/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.device.database;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.Device;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.InsteonModem;

/**
 * The {@link DatabaseManager} manages database read/write operations
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class DatabaseManager {
    public static final int MESSAGE_TIMEOUT = 6000; // in milliseconds

    private static enum OperationType {
        READ,
        WRITE
    }

    private InsteonModem modem;
    private LinkDBReader ldbr;
    private LinkDBWriter ldbw;
    private ModemDBReader mdbr;
    private ModemDBWriter mdbw;
    private ScheduledExecutorService scheduler;
    private @Nullable ScheduledFuture<?> job;
    private Queue<DatabaseOperation> operationQueue = new LinkedList<>();
    private boolean terminated = false;

    public DatabaseManager(InsteonModem modem, ScheduledExecutorService scheduler) {
        this.modem = modem;
        this.scheduler = scheduler;
        this.ldbr = new LinkDBReader(modem, scheduler);
        this.ldbw = new LinkDBWriter(modem, scheduler);
        this.mdbr = new ModemDBReader(modem, scheduler);
        this.mdbw = new ModemDBWriter(modem, scheduler);
    }

    public void read(Device device, long delay) {
        addOperation(device, OperationType.READ, delay);
    }

    public void write(Device device, long delay) {
        addOperation(device, OperationType.WRITE, delay);
    }

    public void stop() {
        terminated = true;

        ScheduledFuture<?> job = this.job;
        if (job != null) {
            job.cancel(true);
            this.job = null;
        }

        ldbr.stop();
        ldbw.stop();
        mdbr.stop();
        mdbw.stop();
    }

    /**
     * Adds a database operation
     *
     * @param device database device
     * @param type operation type
     * @param delay scheduling delay (in milliseconds)
     */
    private synchronized void addOperation(Device device, OperationType type, long delay) {
        DatabaseOperation operation = new DatabaseOperation(device, type);
        if (!operationQueue.contains(operation)) {
            operationQueue.add(operation);
        }

        if (job == null && !terminated) {
            job = scheduler.schedule(() -> {
                modem.getRequestManager().pause();
                modem.getPollManager().pause();

                handleNextOperation();
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Handles the next database operation
     */
    private synchronized void handleNextOperation() {
        DatabaseOperation operation = operationQueue.poll();
        if (operation == null || terminated) {
            modem.getRequestManager().resume();
            modem.getPollManager().resume();
            job = null;
            return;
        }

        Device device = operation.getDevice();
        switch (operation.getType()) {
            case READ:
                if (device instanceof InsteonModem) {
                    mdbr.read();
                } else if (device instanceof InsteonDevice insteonDevice) {
                    ldbr.read(insteonDevice);
                }
                break;
            case WRITE:
                if (device instanceof InsteonModem) {
                    mdbw.write();
                } else if (device instanceof InsteonDevice insteonDevice) {
                    ldbw.write(insteonDevice);
                }
                break;
        }
    }

    /**
     * Notifies that the last database operation has completed
     */
    public void operationCompleted() {
        handleNextOperation();
    }

    /**
     * Class that reflects a database operation
     */
    private static class DatabaseOperation {
        private Device device;
        private OperationType type;

        public DatabaseOperation(Device device, OperationType type) {
            this.device = device;
            this.type = type;
        }

        public Device getDevice() {
            return device;
        }

        public OperationType getType() {
            return type;
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            DatabaseOperation other = (DatabaseOperation) obj;
            return device.equals(other.device) && type == other.type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + device.hashCode();
            result = prime * result + type.hashCode();
            return result;
        }
    }
}
