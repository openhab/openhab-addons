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
package org.openhab.binding.bluetooth.bluegiga.internal;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple reschedulable Timer.
 *
 * @author Pauli Anttila - Initial contribution
 */
@NonNullByDefault
public class BlueGigaReschedulableTimer extends Timer {
    private final Logger logger = LoggerFactory.getLogger(BlueGigaReschedulableTimer.class);

    private @NonNullByDefault({}) TimerTask timerTask;
    private @NonNullByDefault({}) Timer timer;
    private @NonNullByDefault({}) Runnable task;

    public BlueGigaReschedulableTimer(String name, Runnable runnable) {
        timer = new Timer(name);
        task = runnable;
    }

    /**
     * Schedules the specified task for execution after the specified delay.
     *
     * If timer is already scheduled, timer will be canceled and scheduled again.
     *
     * @param delay delay in milliseconds before task is to be executed.
     *
     * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
     *             <tt>delay + System.currentTimeMillis()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *             cancelled, timer was cancelled, or timer thread terminated.
     * @throws NullPointerException if {@code task} is null
     */
    public void schedule(long delay) {
        logger.trace("Schedule task to be executed after {} milliseconds", delay);
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                task.run();
            }
        };
        timer.schedule(timerTask, delay);
    }
}
