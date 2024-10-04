/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.automation.jsscripting.internal;

import static org.openhab.core.automation.module.script.ScriptTransformationService.OPENHAB_TRANSFORMATION_SCRIPT;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.eclipse.jdt.annotation.Nullable;
import org.graalvm.polyglot.PolyglotException;
import org.openhab.automation.jsscripting.internal.scriptengine.InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps ScriptEngines provided by Graal to provide error messages and stack traces for scripts.
 *
 * @author Jonathan Gilbert - Initial contribution
 * @author Florian Hotze - Improve logger name, Fix memory leak caused by exception logging
 */
class DebuggingGraalScriptEngine<T extends ScriptEngine & Invocable & AutoCloseable & Compilable & Lock>
        extends InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable<T> implements Lock {

    private static final int STACK_TRACE_LENGTH = 5;

    private @Nullable Logger logger;

    public DebuggingGraalScriptEngine(T delegate) {
        super(delegate);
    }

    @Override
    protected void beforeInvocation() {
        super.beforeInvocation();
        // OpenhabGraalJSScriptEngine::beforeInvocation will be executed after
        // DebuggingGraalScriptEngine::beforeInvocation, because GraalJSScriptEngineFactory::createScriptEngine returns
        // a DebuggingGraalScriptEngine instance.
        // We therefore need to synchronize logger setup here and cannot rely on the synchronization in
        // OpenhabGraalJSScriptEngine.
        delegate.lock();
        try {
            if (logger == null) {
                initializeLogger();
            }
        } finally { // Make sure that Lock is unlocked regardless of an exception being thrown or not to avoid deadlocks
            delegate.unlock();
        }
    }

    @Override
    public Exception afterThrowsInvocation(Exception e) {
        Throwable cause = e.getCause();
        // OPS4J Pax Logging holds a reference to the exception, which causes the OpenhabGraalJSScriptEngine to not be
        // removed from heap by garbage collection and causing a memory leak.
        // Therefore, don't pass the exceptions itself to the logger, but only their message!
        if (cause instanceof IllegalArgumentException) {
            logger.error("Failed to execute script: {}", stringifyThrowable(cause));
        } else if (cause instanceof PolyglotException) {
            logger.error("Failed to execute script: {}", stringifyThrowable(cause));
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

    /**
     * Initializes the logger.
     * This cannot be done on script engine creation because the context variables are not yet initialized.
     * Therefore, the logger needs to be initialized on the first use after script engine creation.
     */
    private void initializeLogger() {
        ScriptContext ctx = delegate.getContext();
        Object fileName = ctx.getAttribute("javax.script.filename");
        Object ruleUID = ctx.getAttribute("ruleUID");
        Object ohEngineIdentifier = ctx.getAttribute("oh.engine-identifier");

        String identifier = "stack";
        if (fileName != null) {
            identifier = fileName.toString().replaceAll("^.*[/\\\\]", "");
        } else if (ruleUID != null) {
            identifier = ruleUID.toString();
        } else if (ohEngineIdentifier != null) {
            if (ohEngineIdentifier.toString().startsWith(OPENHAB_TRANSFORMATION_SCRIPT)) {
                identifier = ohEngineIdentifier.toString().replaceAll(OPENHAB_TRANSFORMATION_SCRIPT, "transformation.");
            }
        }

        logger = LoggerFactory.getLogger("org.openhab.automation.script.javascript." + identifier);
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
