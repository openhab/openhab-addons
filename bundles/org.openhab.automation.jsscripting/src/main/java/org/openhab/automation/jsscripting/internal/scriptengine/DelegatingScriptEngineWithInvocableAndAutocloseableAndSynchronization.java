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

package org.openhab.automation.jsscripting.internal.scriptengine;

import java.io.Reader;
import java.util.Objects;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.eclipse.jdt.annotation.Nullable;

/**
 * {@link ScriptEngine} implementation that delegates to a supplied ScriptEngine instance and provides multi-thread
 * synchronization. Allows overriding specific methods.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public abstract class DelegatingScriptEngineWithInvocableAndAutocloseableAndSynchronization<T extends ScriptEngine & Invocable & AutoCloseable>
        implements ScriptEngine, Invocable, AutoCloseable {
    protected T delegate;
    /** Shared lock object for synchronization of multi-thread access */
    protected final Object lock = new Object();

    public DelegatingScriptEngineWithInvocableAndAutocloseableAndSynchronization(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public @Nullable Object eval(String s, ScriptContext scriptContext) throws ScriptException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.eval(s, scriptContext);
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.eval(reader, scriptContext);
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Object eval(String s) throws ScriptException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.eval(s);
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Object eval(Reader reader) throws ScriptException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.eval(reader);
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Object eval(String s, Bindings bindings) throws ScriptException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.eval(s, bindings);
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Object eval(Reader reader, Bindings bindings) throws ScriptException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.eval(reader, bindings);
            }
        } else {
            return null;
        }
    }

    @Override
    public void put(String s, Object o) {
        if (Objects.nonNull(delegate))
            delegate.put(s, o);
    }

    @Override
    public @Nullable Object get(String s) {
        return Objects.nonNull(delegate) ? delegate.get(s) : null;
    }

    @Override
    public @Nullable Bindings getBindings(int i) {
        return Objects.nonNull(delegate) ? delegate.getBindings(i) : null;
    }

    @Override
    public void setBindings(Bindings bindings, int i) {
        if (Objects.nonNull(delegate))
            delegate.setBindings(bindings, i);
    }

    @Override
    public @Nullable Bindings createBindings() {
        return Objects.nonNull(delegate) ? delegate.createBindings() : null;
    }

    @Override
    public @Nullable ScriptContext getContext() {
        return Objects.nonNull(delegate) ? delegate.getContext() : null;
    }

    @Override
    public void setContext(ScriptContext scriptContext) {
        if (Objects.nonNull(delegate))
            delegate.setContext(scriptContext);
    }

    @Override
    public @Nullable ScriptEngineFactory getFactory() {
        return Objects.nonNull(delegate) ? delegate.getFactory() : null;
    }

    @Override
    public @Nullable Object invokeMethod(Object o, String s, Object... objects)
            throws ScriptException, NoSuchMethodException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.invokeMethod(o, s, objects);
            }
        } else {
            return null;
        }
    }

    @Override
    public @Nullable Object invokeFunction(String s, Object... objects) throws ScriptException, NoSuchMethodException {
        if (Objects.nonNull(delegate)) {
            synchronized (lock) {
                return delegate.invokeFunction(s, objects);
            }
        } else {
            return null;
        }
    }

    @Override
    public <T> T getInterface(Class<T> aClass) {
        return delegate.getInterface(aClass);
    }

    @Override
    public <T> T getInterface(Object o, Class<T> aClass) {
        return delegate.getInterface(o, aClass);
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(delegate))
            delegate.close();
    }
}
