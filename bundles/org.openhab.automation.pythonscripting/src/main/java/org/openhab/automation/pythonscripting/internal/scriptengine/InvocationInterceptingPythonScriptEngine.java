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

import java.lang.reflect.UndeclaredThrowableException;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.NonNull;
import org.graalvm.polyglot.PolyglotException;
import org.openhab.automation.pythonscripting.internal.scriptengine.graal.GraalPythonScriptEngine;

/**
 * Interception of calls, either before Invocation, or upon a {@link ScriptException} being
 * thrown.
 *
 * @author Holger Hees - Initial contribution
 */
public abstract class InvocationInterceptingPythonScriptEngine extends GraalPythonScriptEngine {
    protected abstract void beforeInvocation() throws PolyglotException;

    protected abstract String beforeInvocation(String source);

    protected abstract Object afterInvocation(Object obj);

    protected abstract <E extends Exception> E afterThrowsInvocation(@NonNull E e);

    @Override
    public Object eval(String s, ScriptContext scriptContext) throws ScriptException {
        try {
            beforeInvocation();
            return afterInvocation(super.eval(beforeInvocation(s), scriptContext));
        } catch (ScriptException e) {
            throw afterThrowsInvocation(e);
        } catch (PolyglotException e) {
            throw afterThrowsInvocation(toScriptException(e));
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object invokeMethod(Object o, String s, Object... objects) throws ScriptException, NoSuchMethodException {
        try {
            beforeInvocation();
            return afterInvocation(super.invokeMethod(o, s, objects));
        } catch (ScriptException e) {
            throw afterThrowsInvocation(e);
        } catch (PolyglotException e) {
            throw afterThrowsInvocation(toScriptException(e));
        } catch (NoSuchMethodException e) {
            throw afterThrowsInvocation(e);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public Object invokeFunction(String s, Object... objects) throws ScriptException, NoSuchMethodException {
        try {
            beforeInvocation();
            return afterInvocation(super.invokeFunction(s, objects));
        } catch (ScriptException e) {
            throw afterThrowsInvocation(e);
        } catch (PolyglotException e) {
            throw afterThrowsInvocation(toScriptException(e));
        } catch (NoSuchMethodException e) {
            throw afterThrowsInvocation(e);
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }

    @Override
    public CompiledScript compile(String s) throws ScriptException {
        try {
            beforeInvocation();
            return (CompiledScript) afterInvocation(super.compile(beforeInvocation(s)));
        } catch (ScriptException e) {
            throw afterThrowsInvocation(e);
        } catch (PolyglotException e) {
            throw afterThrowsInvocation(toScriptException(e));
        } catch (Exception e) {
            throw new UndeclaredThrowableException(afterThrowsInvocation(e)); // Wrap and rethrow other exceptions
        }
    }
}
