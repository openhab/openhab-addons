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
package org.openhab.binding.jellyfin.internal.handler.tasks;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Abstract base class for all tasks used in the Jellyfin binding.
 * Tasks are runnable operations that can be scheduled for execution.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractTask implements Runnable {

    private final String id;
    private final int startupDelay;
    private final int interval;

    /**
     * Creates a new task with the specified parameters.
     *
     * @param id The unique identifier of the task
     * @param startupDelay The initial delay in seconds before the first execution
     * @param interval The interval in seconds between successive executions
     */
    protected AbstractTask(String id, int startupDelay, int interval) {
        this.id = id;
        this.startupDelay = startupDelay;
        this.interval = interval;
    }

    /**
     * Gets the unique identifier of the task.
     *
     * @return The task identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the initial delay in seconds before the first execution of this task.
     *
     * @return The startup delay in seconds
     */
    public int getStartupDelay() {
        return startupDelay;
    }

    /**
     * Gets the interval in seconds between successive executions of this task.
     *
     * @return The interval in seconds
     */
    public int getInterval() {
        return interval;
    }
}
