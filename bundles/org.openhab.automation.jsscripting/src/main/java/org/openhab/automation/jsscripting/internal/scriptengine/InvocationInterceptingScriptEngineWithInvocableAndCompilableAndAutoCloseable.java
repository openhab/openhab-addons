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
package org.openhab.automation.jsscripting.internal.scriptengine;

import java.io.Reader;
import java.lang.reflect.UndeclaredThrowableException;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Delegate allowing AOP-style interception of calls, either before Invocation, or upon a {@link ScriptException} being
 * thrown.
 *
 * @param <T> The delegate class
 * @author Jonathan Gilbert - Initial contribution
 */
public abstract class InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable<T extends ScriptEngine & Invocable & Compilable & AutoCloseable>
        extends DelegatingScriptEngineWithInvocableAndCompilableAndAutocloseable<T> {

    protected InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable(T delegate) {
        super(delegate);
    }

    /**
     * Hook method to be called before the invocation of any method on the script engine.
     */
    protected void beforeInvocation() {
    }

    /**
     * Hook method to be called when a string script is about to be evaluated.
     * 
     * @param script the script to be evaluated
     * @return the modified script to be evaluated instead, or the original script
     */
    protected String onScript(String script) {
        return script;
    }

    /**
     * Hook method to be called after the invocation of any method on the script engine.
     * 
     * @param obj the result of the invocation
     * @return the result to be returned instead, or the original result
     */
    protected Object afterInvocation(Object obj) {
        return obj;
    }

    /**
     * Hook method to be called after a {@link ScriptException} or other exception is thrown during invocation.
     * 
     * @param e the exception that was thrown
     * @return the exception to be thrown instead, or the original exception
     */
    protected Exception afterThrowsInvocation(Exception e) {
        return e;
    }

    @Override
    public Object eval(String script, ScriptContext scriptContext) throws ScriptException {
        try {
            beforeInvocation();
            return afterInvocation(super.eval(onScript(script), scriptContext));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        try {
            beforeInvocation();
            return afterInvocation(super.eval(reader, scriptContext));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(String script) throws ScriptException {
        try {
            beforeInvocation();
            return afterInvocation(super.eval(onScript(script)));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            beforeInvocation();
            return afterInvocation(super.eval(reader));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(String script, Bindings bindings) throws ScriptException {
        try {
            beforeInvocation();
            return afterInvocation(super.eval(onScript(script), bindings));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        try {
            beforeInvocation();
            return afterInvocation(super.eval(reader, bindings));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object invokeMethod(Object thiz, String name, Object... args)
            throws ScriptException, NoSuchMethodException, NullPointerException, IllegalArgumentException {
        try {
            beforeInvocation();
            return afterInvocation(super.invokeMethod(thiz, name, args));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (NoSuchMethodException e) { // Make sure to unlock on exceptions from Invocable.invokeMethod to avoid
                                            // deadlocks
            throw (NoSuchMethodException) afterThrowsInvocation(e);
        } catch (NullPointerException e) {
            throw (NullPointerException) afterThrowsInvocation(e);
        } catch (IllegalArgumentException e) {
            throw (IllegalArgumentException) afterThrowsInvocation(e);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object invokeFunction(String name, Object... args)
            throws ScriptException, NoSuchMethodException, NullPointerException {
        try {
            beforeInvocation();
            return afterInvocation(super.invokeFunction(name, args));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (NoSuchMethodException e) { // Make sure to unlock on exceptions from Invocable.invokeFunction to avoid
                                            // deadlocks
            throw (NoSuchMethodException) afterThrowsInvocation(e);
        } catch (NullPointerException e) {
            throw (NullPointerException) afterThrowsInvocation(e);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public CompiledScript compile(String script) throws ScriptException {
        try {
            beforeInvocation();
            return (CompiledScript) afterInvocation(super.compile(onScript(script)));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        try {
            beforeInvocation();
            return (CompiledScript) afterInvocation(super.compile(reader));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }
}
