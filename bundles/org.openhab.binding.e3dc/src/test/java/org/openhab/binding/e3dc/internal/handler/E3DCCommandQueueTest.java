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

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests that the commands of a thing run one after the other and in the order they arrived.
 *
 * <p>
 * The executor of these tests keeps the work it is given until the test runs it, so the order of events is
 * deterministic and no test waits for a thread.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class E3DCCommandQueueTest {

    /** Collects the work handed to the executor and runs it on demand. */
    private static class ManualExecutor implements Executor {
        final List<Runnable> pending = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            pending.add(command);
        }

        void runAll() {
            while (!pending.isEmpty()) {
                pending.remove(0).run();
            }
        }
    }

    @Test
    void whenTasksAreSubmittedThenTheyRunInTheOrderOfSubmission() {
        ManualExecutor executor = new ManualExecutor();
        E3DCCommandQueue queue = new E3DCCommandQueue(executor);
        List<Integer> order = new ArrayList<>();

        queue.submit(() -> order.add(1));
        queue.submit(() -> order.add(2));
        queue.submit(() -> order.add(3));
        executor.runAll();

        assertEquals(List.of(1, 2, 3), order);
    }

    @Test
    void whenTasksAreWaitingThenTheExecutorIsAskedOnlyOnce() {
        ManualExecutor executor = new ManualExecutor();
        E3DCCommandQueue queue = new E3DCCommandQueue(executor);

        queue.submit(() -> {
        });
        queue.submit(() -> {
        });

        assertEquals(1, executor.pending.size());
    }

    @Test
    void whenTheQueueRanDryThenTheNextTaskAsksTheExecutorAgain() {
        ManualExecutor executor = new ManualExecutor();
        E3DCCommandQueue queue = new E3DCCommandQueue(executor);
        AtomicInteger runs = new AtomicInteger();
        queue.submit(runs::incrementAndGet);
        executor.runAll();

        queue.submit(runs::incrementAndGet);
        executor.runAll();

        assertEquals(2, runs.get());
    }

    @Test
    void whenATaskFailsThenTheNextOneStillRuns() {
        ManualExecutor executor = new ManualExecutor();
        E3DCCommandQueue queue = new E3DCCommandQueue(executor);
        AtomicInteger runs = new AtomicInteger();

        queue.submit(() -> {
            throw new IllegalStateException("expected by the test");
        });
        queue.submit(runs::incrementAndGet);
        executor.runAll();

        assertEquals(1, runs.get());
    }

    @Test
    void whenTheQueueIsClearedThenWaitingTasksDoNotRun() {
        ManualExecutor executor = new ManualExecutor();
        E3DCCommandQueue queue = new E3DCCommandQueue(executor);
        AtomicInteger runs = new AtomicInteger();
        queue.submit(runs::incrementAndGet);
        queue.submit(runs::incrementAndGet);

        queue.clear();
        executor.runAll();

        assertEquals(0, runs.get());
    }

    @Test
    void whenTheQueueIsFullThenFurtherTasksAreDropped() {
        ManualExecutor executor = new ManualExecutor();
        E3DCCommandQueue queue = new E3DCCommandQueue(executor);
        AtomicInteger runs = new AtomicInteger();

        for (int submitted = 0; submitted < E3DCCommandQueue.MAX_PENDING_TASKS + 1; submitted++) {
            queue.submit(runs::incrementAndGet);
        }
        executor.runAll();

        assertEquals(E3DCCommandQueue.MAX_PENDING_TASKS, runs.get(),
                "the queue must not grow without bound, so the excess command is dropped");
    }

    @Test
    void whenTheExecutorRejectsWorkThenTheQueueStartsAgainOnTheNextTask() {
        AtomicInteger executions = new AtomicInteger();
        List<Runnable> pending = new ArrayList<>();
        Executor rejectingOnce = command -> {
            if (executions.incrementAndGet() == 1) {
                throw new IllegalStateException("shut down by the test");
            }
            pending.add(command);
        };
        E3DCCommandQueue queue = new E3DCCommandQueue(rejectingOnce);
        AtomicInteger runs = new AtomicInteger();

        queue.submit(runs::incrementAndGet);
        queue.submit(runs::incrementAndGet);
        while (!pending.isEmpty()) {
            pending.remove(0).run();
        }

        assertEquals(1, runs.get(), "the task that came after the rejection must still run");
    }
}
