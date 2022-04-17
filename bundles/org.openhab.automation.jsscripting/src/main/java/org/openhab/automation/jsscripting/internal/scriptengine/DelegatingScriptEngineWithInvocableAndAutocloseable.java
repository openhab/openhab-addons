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

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * {@link ScriptEngine} implementation that delegates to a supplied ScriptEngine instance. Allows overriding specific
 * methods.
 *
 * @author Jonathan Gilbert - Initial contribution
 */
public abstract class DelegatingScriptEngineWithInvocableAndAutocloseable<T extends ScriptEngine & Invocable & AutoCloseable>
        implements ScriptEngine, Invocable, AutoCloseable {
    protected T delegate;

    public DelegatingScriptEngineWithInvocableAndAutocloseable(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object eval(String s, ScriptContext scriptContext) throws ScriptException {
        return delegate.eval(s, scriptContext);
    }

    @Override
    public Object eval(Reader reader, ScriptContext scriptContext) throws ScriptException {
        return delegate.eval(reader, scriptContext);
    }

    @Override
    public Object eval(String s) throws ScriptException {
        return delegate.eval(s);
    }

    @Override
    public Object eval(Reader reader) throws ScriptException {
        return delegate.eval(reader);
    }

    @Override
    public Object eval(String s, Bindings bindings) throws ScriptException {
        return delegate.eval(s, bindings);
    }

    @Override
    public Object eval(Reader reader, Bindings bindings) throws ScriptException {
        return delegate.eval(reader, bindings);
    }

    @Override
    public void put(String s, Object o) {
        delegate.put(s, o);
    }

    @Override
    public Object get(String s) {
        return delegate.get(s);
    }

    @Override
    public Bindings getBindings(int i) {
        return delegate.getBindings(i);
    }

    @Override
    public void setBindings(Bindings bindings, int i) {
        delegate.setBindings(bindings, i);
    }

    @Override
    public Bindings createBindings() {
        return delegate.createBindings();
    }

    @Override
    public ScriptContext getContext() {
        return delegate.getContext();
    }

    @Override
    public void setContext(ScriptContext scriptContext) {
        delegate.setContext(scriptContext);
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return delegate.getFactory();
    }

    @Override
    public Object invokeMethod(Object o, String s, Object... objects) throws ScriptException, NoSuchMethodException {
        return delegate.invokeMethod(o, s, objects);
    }

    @Override
    public Object invokeFunction(String s, Object... objects) throws ScriptException, NoSuchMethodException {
        return delegate.invokeFunction(s, objects);
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
        delegate.close();
    }
}
