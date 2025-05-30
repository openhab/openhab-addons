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
package org.openhab.automation.pythonscripting.internal.scriptengine;

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
 * @author Holger Hees - Initial contribution (Reused from jsscripting)
 */
public abstract class InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable<T extends ScriptEngine & Invocable & Compilable & AutoCloseable>
        extends DelegatingScriptEngineWithInvocableAndCompilableAndAutocloseable<T> {

    protected String beforeInvocation(String source) {
        beforeInvocation();
        return source;
    }

    protected Reader beforeInvocation(Reader reader) {
        beforeInvocation();
        return reader;
    }

    protected void beforeInvocation() {
    }

    protected Object afterInvocation(Object obj) {
        return obj;
    }

    protected Exception afterThrowsInvocation(Exception e) {
        return e;
    }

    @Override
    public Object eval(String s, ScriptContext scriptContext) throws ScriptException {
        try {
            return afterInvocation(super.eval(beforeInvocation(s), scriptContext));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        try {
            return afterInvocation(super.eval(beforeInvocation(reader), scriptContext));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(String s) throws ScriptException {
        try {
            return afterInvocation(super.eval(beforeInvocation(s)));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        try {
            return afterInvocation(super.eval(beforeInvocation(reader)));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(String s, Bindings bindings) throws ScriptException {
        try {
            return afterInvocation(super.eval(beforeInvocation(s), bindings));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        try {
            return afterInvocation(super.eval(beforeInvocation(reader), bindings));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object invokeMethod(Object o, String s, Object... objects)
            throws ScriptException, NoSuchMethodException, NullPointerException, IllegalArgumentException {
        try {
            beforeInvocation();
            return afterInvocation(super.invokeMethod(o, s, objects));
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
    public Object invokeFunction(String s, Object... objects)
            throws ScriptException, NoSuchMethodException, NullPointerException {
        try {
            beforeInvocation();
            return afterInvocation(super.invokeFunction(s, objects));
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
    public CompiledScript compile(String s) throws ScriptException {
        try {
            return wrapCompiledScript((CompiledScript) afterInvocation(super.compile(beforeInvocation(s))));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public CompiledScript compile(Reader reader) throws ScriptException {
        try {
            return wrapCompiledScript((CompiledScript) afterInvocation(super.compile(beforeInvocation(reader))));
        } catch (ScriptException se) {
            throw (ScriptException) afterThrowsInvocation(se);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    private CompiledScript wrapCompiledScript(CompiledScript script) {
        return new CompiledScript() {
            @Override
            public ScriptEngine getEngine() {
                return InvocationInterceptingScriptEngineWithInvocableAndCompilableAndAutoCloseable.this;
            }

            @Override
            public Object eval(ScriptContext context) throws ScriptException {
                return script.eval(context);
            }
        };
    }
}
