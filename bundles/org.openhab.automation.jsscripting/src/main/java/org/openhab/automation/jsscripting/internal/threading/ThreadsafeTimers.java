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
package org.openhab.automation.jsscripting.internal.threading;

import java.time.ZonedDateTime;
import java.util.HashMap;

import org.openhab.core.model.script.ScriptServiceUtil;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.scheduler.Scheduler;

/**
 * A replacement for the timer functionality of {@link org.openhab.core.model.script.actions.ScriptExecution
 * ScriptExecution} which controls multithreaded execution access to the single-threaded GraalJS contexts.
 *
 * @author Florian Hotze - Initial contribution
 * @author Florian Hotze - Reimplementation to conform standard JS setTimeout and setInterval
 */
public class ThreadsafeTimers {
    private final Object lock;
    private final Scheduler scheduler;
    // Mapping of positive, non-zero integer values (used as timeoutID or intervalID) and the Scheduler
    private final HashMap<Integer, ScheduledCompletableFuture<Object>> idSchedulerMapping = new HashMap<>();
    private String identifier = "noIdentifier.timerId.";

    public ThreadsafeTimers(Object lock) {
        this.lock = lock;
        this.scheduler = ScriptServiceUtil.getScheduler();
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier + ".timerId.";
    }

    private ScheduledCompletableFuture<Object> createFuture(Integer id, ZonedDateTime zdt, Runnable callback) {
        return scheduler.schedule(() -> {
            synchronized (lock) {
                callback.run();
            }
        }, identifier + id, zdt.toInstant());
    }

    private Integer createNewId() {
        Integer id = idSchedulerMapping.size() + 1;
        while (idSchedulerMapping.get(id) != null)
            id++;
        return id;
    }

    public Integer setTimeout(Runnable callback, Long delay) {
        return setTimeout(callback, delay, null);
    }

    public Integer setTimeout(Runnable callback, Long delay, Object... args) {
        Integer id = createNewId();
        ScheduledCompletableFuture<Object> future = createFuture(id, ZonedDateTime.now().plusNanos(delay * 1000000),
                callback);
        idSchedulerMapping.put(id, future);
        return id;
    }

    public void clearTimeout(Integer id) {
        ScheduledCompletableFuture<Object> scheduled = idSchedulerMapping.get(id);
        scheduled.cancel(true);
        idSchedulerMapping.remove(id);
    }

    private void createLoopingFuture(Integer id, Long delay, Runnable callback) {
        ScheduledCompletableFuture<Object> future = scheduler.schedule(() -> {
            synchronized (lock) {
                callback.run();
                if (idSchedulerMapping.get(id) != null)
                    createLoopingFuture(id, delay, callback);
            }
        }, identifier + id, ZonedDateTime.now().plusNanos(delay * 1000000).toInstant());
        idSchedulerMapping.put(id, future);
    }

    public Integer setInterval(Runnable callback, Long delay) {
        return setInterval(callback, delay, null);
    }

    public Integer setInterval(Runnable callback, Long delay, Object... args) {
        Integer id = createNewId();
        createLoopingFuture(id, delay, callback);
        return id;
    }

    public void clearInterval(Integer id) {
        clearTimeout(id);
    }

    public void cancelAll() {
        idSchedulerMapping.forEach((id, future) -> {
            future.cancel(true);
        });
        idSchedulerMapping.clear();
    }
}
