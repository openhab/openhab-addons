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
package org.openhab.automation.pythonscripting.internal;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.automation.pythonscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable;
import org.slf4j.Logger;

/**
 * Wraps ScriptEngines provided by Graal to provide error messages and stack traces for scripts.
 *
 * @author Holger Hees - Initial contribution (Reused from jsscripting)
 */
class DebuggingPythonScriptEngine<T extends ScriptEngine & Invocable & AutoCloseable & Compilable & Lock>
        extends InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable<T> implements Lock {

    private static final int STACK_TRACE_LENGTH = 5;

    private @Nullable Logger logger;

    public DebuggingPythonScriptEngine(T delegate) {
        super(delegate);
    }

    @Override
    protected void beforeInvocation() {
        super.beforeInvocation();
        // PythonScriptEngine::beforeInvocation will be executed after
        // DebuggingPythonScriptEngine::beforeInvocation, because PythonScriptEngine::createScriptEngine returns
        // a DebuggingGraalScriptEngine instance.
        // We therefore need to synchronize logger setup here and cannot rely on the synchronization in
        // PythonScriptEngine.
        delegate.lock();
        try {
            if (logger == null) {
                logger = PythonScriptEngine.initScriptLogger(delegate);

            }
        } finally { // Make sure that Lock is unlocked regardless of an exception being thrown or not to avoid deadlocks
            delegate.unlock();
        }
    }

    @Override
    public Exception afterThrowsInvocation(Exception e) {
        // OPS4J Pax Logging holds a reference to the exception, which causes the OpenhabGraalJSScriptEngine to not be
        // removed from heap by garbage collection and causing a memory leak.
        // Therefore, don't pass the exceptions itself to the logger, but only their message!
        if (e instanceof ScriptException) {
            // PolyglotException will always be wrapped into ScriptException and they will be visualized in
            // org.openhab.core.automation.module.script.internal.ScriptEngineManagerImpl
            if (logger.isDebugEnabled()) {
                logger.debug("Failed to execute script (PolyglotException): {}", stringifyThrowable(e.getCause()));
            }
        } else if (e.getCause() instanceof IllegalArgumentException) {
            logger.error("Failed to execute script (IllegalArgumentException): {}", stringifyThrowable(e.getCause()));
        }

        return e;
    }

    private String stringifyThrowable(Throwable throwable) {
        String message = throwable.getMessage();
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        String stackTrace = Arrays.stream(stackTraceElements).limit(STACK_TRACE_LENGTH)
                .map(t -> "        at " + t.toString()).collect(Collectors.joining(System.lineSeparator()))
                + System.lineSeparator() + "        ... " + stackTraceElements.length + " more";
        return (message != null) ? message + System.lineSeparator() + stackTrace : stackTrace;
    }

    @Override
    public void lock() {
        delegate.lock();
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        delegate.lockInterruptibly();
    }

    @Override
    public boolean tryLock() {
        return delegate.tryLock();
    }

    @Override
    public boolean tryLock(long l, TimeUnit timeUnit) throws InterruptedException {
        return delegate.tryLock(l, timeUnit);
    }

    @Override
    public void unlock() {
        delegate.unlock();
    }

    @Override
    public Condition newCondition() {
        return delegate.newCondition();
    }
}
