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

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.jsscripting.internal.TimerImpl;
import org.openhab.core.model.script.ScriptServiceUtil;
import org.openhab.core.model.script.actions.Timer;
import org.openhab.core.scheduler.Scheduler;

/**
 * A replacement for the timer functionality of {@link org.openhab.core.model.script.actions.ScriptExecution
 * ScriptExecution} which controls multithreaded execution access to the single-threaded GraalJS Contexts.
 *
 * @author Florian Hotze
 */
public class ThreadsafeTimers {
    private final Object lock;

    public ThreadsafeTimers(Object lock) {
        this.lock = lock;
    }

    public Timer createTimer(ZonedDateTime instant, Runnable callable) {
        return createTimer((String) null, instant, callable);
    }

    public Timer createTimer(@Nullable String identifier, ZonedDateTime instant, Runnable callable) {
        Scheduler scheduler = ScriptServiceUtil.getScheduler();

        return new TimerImpl(lock, scheduler, instant, () -> {
            synchronized (lock) {
                callable.run();
            }

        }, identifier);
    }

    public Timer createTimerWithArgument(ZonedDateTime instant, Object arg1, Runnable callable) {
        return createTimerWithArgument((String) null, instant, arg1, callable);
    }

    public Timer createTimerWithArgument(@Nullable String identifier, ZonedDateTime instant, Object arg1,
            Runnable callable) {
        Scheduler scheduler = ScriptServiceUtil.getScheduler();
        return new TimerImpl(lock, scheduler, instant, () -> {
            synchronized (lock) {
                callable.run();
            }

        }, identifier);
    }
}
