/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.handler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs the commands of one thing one after the other on an executor, in the order they arrived.
 *
 * <p>
 * A command writes to the device and waits for the answer, which can take as long as the configured timeout. The
 * framework calls a handler on a thread it expects back quickly, so the handler hands the write over to this queue
 * instead. At most one task runs at a time and no thread is held while the queue is empty.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class E3DCCommandQueue {

    private final Logger logger = LoggerFactory.getLogger(E3DCCommandQueue.class);

    private final Executor executor;
    private final Deque<Runnable> tasks = new ArrayDeque<>();
    private boolean draining;

    public E3DCCommandQueue(Executor executor) {
        this.executor = executor;
    }

    /** Queues a task and starts the executor if no other task is being worked off. */
    public void submit(Runnable task) {
        synchronized (this) {
            tasks.add(task);
            if (draining) {
                return;
            }
            draining = true;
        }
        executor.execute(this::drain);
    }

    /** Drops the tasks that have not started yet, which a disposed handler must not run any more. */
    public synchronized void clear() {
        tasks.clear();
    }

    private void drain() {
        while (true) {
            Runnable next;
            synchronized (this) {
                if (tasks.isEmpty()) {
                    draining = false;
                    return;
                }
                next = tasks.remove();
            }
            try {
                next.run();
            } catch (RuntimeException e) {
                // one failing command must not stop the ones behind it
                logger.warn("A command failed unexpectedly", e);
            }
        }
    }
}
