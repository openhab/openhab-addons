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
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
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

    public Timer createTimer(ZonedDateTime instant, Procedure0 closure) {
        return createTimer((String) null, instant, closure);
    }

    public Timer createTimer(@Nullable String identifier, ZonedDateTime instant, Procedure0 closure) {
        Scheduler scheduler = ScriptServiceUtil.getScheduler();

        return new TimerImpl(lock, scheduler, instant, () -> {
            synchronized (lock) {
                closure.apply();
            }

        }, identifier);
    }

    public Timer createTimerWithArgument(ZonedDateTime instant, Object arg1, Procedure1<Object> closure) {
        return createTimerWithArgument((String) null, instant, arg1, closure);
    }

    public Timer createTimerWithArgument(@Nullable String identifier, ZonedDateTime instant, Object arg1,
            Procedure1<Object> closure) {
        Scheduler scheduler = ScriptServiceUtil.getScheduler();
        return new TimerImpl(lock, scheduler, instant, () -> {
            synchronized (lock) {
                closure.apply(arg1);
            }

        }, identifier);
    }
}
